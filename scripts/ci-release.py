#!/usr/bin/env python3
"""Release CI helpers. Never print signing material or put passwords in argv/files."""

import base64
import binascii
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import zipfile


SIGNING_NAMES = (
    "SIGNING_KEYSTORE_BASE64", "SIGNING_STORE_PASSWORD",
    "SIGNING_KEY_ALIAS", "SIGNING_KEY_PASSWORD",
)
NATIVE_LIBS = ("libclash.so", "liboverride.so", "libbridge.so")


def properties(path):
    return dict(line.strip().split("=", 1) for line in Path(path).read_text().splitlines()
                if "=" in line and not line.lstrip().startswith(("#", "!")))


def validate_tag(tag):
    if not re.fullmatch(r"v[0-9]+\.[0-9]+\.[0-9]+", tag):
        raise ValueError("Release tag must be vMAJOR.MINOR.PATCH, for example v0.5.5")
    return tag


def output(name, value):
    if "\n" in str(value) or "\r" in str(value):
        raise ValueError("Invalid workflow output")
    with open(os.environ["GITHUB_OUTPUT"], "a") as stream:
        stream.write(f"{name}={value}\n")


def metadata(tag):
    validate_tag(tag)
    config = properties("gradle.properties")
    if config["project.version.name"] != tag[1:]:
        raise ValueError("Tag does not match project.version.name in the selected source")
    head = subprocess.check_output(["git", "rev-parse", "HEAD"], text=True).strip()
    tagged = subprocess.check_output(["git", "rev-parse", f"refs/tags/{tag}^{{commit}}"], text=True).strip()
    if head != tagged:
        raise ValueError("Checkout does not match the release tag")
    sdk_major = int(config["android.compileSdk"])
    sdk_minor = int(config.get("android.compileSdkMinor", "0"))
    # API 37 uses a dotted package name even for minor version zero.
    sdk = f"{sdk_major}.{sdk_minor}" if sdk_major >= 37 or sdk_minor else str(sdk_major)
    for name, value in {
        "tag": tag, "sha": head, "version": config["project.version.name"],
        "ndk": config["android.ndkVersion"], "sdk": sdk,
    }.items():
        output(name, value)


def check_secrets(env):
    missing = [name for name in SIGNING_NAMES if not env.get(name)]
    if missing:
        raise ValueError("Missing GitHub Actions Secrets: " + ", ".join(missing))


def keystore_path():
    return Path(os.environ["RUNNER_TEMP"]) / "kokorobox-release.keystore"


def restore_keystore(env, destination):
    check_secrets(env)
    try:
        data = base64.b64decode("".join(env["SIGNING_KEYSTORE_BASE64"].split()), validate=True)
    except (ValueError, binascii.Error):
        raise ValueError("SIGNING_KEYSTORE_BASE64 is not valid Base64") from None
    if not data:
        raise ValueError("Signing keystore is empty")
    descriptor = os.open(destination, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(descriptor, "wb") as stream:
        stream.write(data)


def signing_environment(env, store):
    for name in SIGNING_NAMES[1:]:
        if not env.get(name):
            raise ValueError(f"Missing GitHub Actions Secret: {name}")
    if not store.is_file():
        raise ValueError("Release keystore was not restored")
    result = dict(env)
    result.pop("SIGNING_KEYSTORE_BASE64", None)
    for prop, value in {
        "signing.store.file": str(store),
        "signing.store.password": env["SIGNING_STORE_PASSWORD"],
        "signing.key.alias": env["SIGNING_KEY_ALIAS"],
        "signing.key.password": env["SIGNING_KEY_PASSWORD"],
    }.items():
        result[f"ORG_GRADLE_PROJECT_{prop}"] = value
    return result


def build():
    # Environment properties work with existing Gradle signing config, including old tags.
    env = signing_environment(os.environ, keystore_path())
    subprocess.run([
        "./gradlew", "--no-daemon", "--no-configuration-cache", "--no-build-cache",
        "--console=plain", "-Pandroid.injected.build.abi=arm64-v8a", ":app:assembleRelease",
    ], env=env, check=True)


def sync_kernel():
    config = properties("kernel.properties")
    destination = Path(config["external.mihomo.dir"])
    if destination.exists():
        raise ValueError("Kernel destination already exists; use a clean checkout")
    subprocess.run([
        "git", "clone", "--depth", "1", "--single-branch", "--branch",
        config["external.mihomo.branch"], config["external.mihomo.repo"], str(destination),
    ], check=True)


def verify_and_stage(apksigner, expected_fingerprint=""):
    config = properties("gradle.properties")
    directory = Path("app/build/outputs/apk/release")
    manifest = json.loads((directory / "output-metadata.json").read_text())
    elements = manifest["elements"]
    if manifest["variantName"] != "release" or len(elements) != 1:
        raise ValueError("Expected exactly one Release APK")
    element = elements[0]
    if (element["versionName"] != config["project.version.name"] or
            manifest["applicationId"] != config["project.applicationId"]):
        raise ValueError("APK version or application ID does not match the release source")
    name = element["outputFile"]
    if Path(name).name != name or not name.endswith(".apk"):
        raise ValueError("Invalid APK output filename")
    apk = directory / name
    # Non-zero exit prevents publishing unsigned or invalid APKs.
    verification = subprocess.check_output(
        [apksigner, "verify", "--verbose", "--print-certs", str(apk)], text=True,
    )
    fingerprints = re.findall(r"Signer #[0-9]+ certificate SHA-256 digest: ([a-fA-F0-9]+)", verification)
    if len(fingerprints) != 1:
        raise ValueError("Expected one APK signing certificate")
    if "CN=Android Debug" in verification:
        raise ValueError("Refusing to publish an APK signed with an Android Debug certificate")
    expected = expected_fingerprint.replace(":", "").strip().lower()
    if expected and expected != fingerprints[0].lower():
        raise ValueError("APK signing certificate does not match SIGNING_CERT_SHA256")
    with zipfile.ZipFile(apk) as archive:
        names = set(archive.namelist())
        if any(f"lib/arm64-v8a/{lib}" not in names for lib in NATIVE_LIBS):
            raise ValueError("APK is missing required arm64 native libraries")
        if any(entry.startswith(("lib/armeabi", "lib/x86")) for entry in names):
            raise ValueError("Expected an arm64-only APK")
    publish = Path("publish")
    publish.mkdir(exist_ok=True)
    # Preserve Gradle's versioned APK filename; no stable-prefix renaming.
    shutil.copyfile(apk, publish / name)
    digest = hashlib.sha256()
    with apk.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    (publish / "SHA256SUMS").write_text(f"{digest.hexdigest()}  {name}\n")
    (publish / "SIGNING-CERT-SHA256.txt").write_text(fingerprints[0].lower() + "\n")


def main():
    command = sys.argv[1]
    if command == "select-tag":
        output("tag", validate_tag(os.environ["RELEASE_TAG"]))
    elif command == "metadata":
        metadata(os.environ["RELEASE_TAG"])
    elif command == "check-secrets":
        check_secrets(os.environ)
    elif command == "restore":
        restore_keystore(os.environ, keystore_path())
    elif command == "build":
        build()
    elif command == "sync-kernel":
        sync_kernel()
    elif command == "verify":
        verify_and_stage(os.environ["APKSIGNER"], os.environ.get("SIGNING_CERT_SHA256", ""))
    elif command == "cleanup":
        keystore_path().unlink(missing_ok=True)
    else:
        raise ValueError("Unknown release command")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, KeyError, subprocess.CalledProcessError) as error:
        print(f"Release step failed: {error}" if isinstance(error, ValueError)
              else "Release step failed; see preceding tool output", file=sys.stderr)
        sys.exit(1)
