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
    return result


def build_unsigned():
    subprocess.run([
        "./gradlew", "--no-daemon", "--no-configuration-cache", "--no-build-cache",
        "--console=plain", "-Pandroid.injected.build.abi=arm64-v8a", ":app:assembleRelease",
    ], check=True)


def sign_release_apk(apksigner):
    """Sign the packaged APK explicitly instead of relying on AGP signing wiring."""
    env = signing_environment(os.environ, keystore_path())
    apk, _ = find_release_apk(properties("gradle.properties"))
    signed = Path(os.environ["RUNNER_TEMP"]) / "kokorobox-signed.apk"
    signed.unlink(missing_ok=True)
    try:
        subprocess.run([
            apksigner, "sign",
            "--ks", str(keystore_path()),
            "--ks-key-alias", env["SIGNING_KEY_ALIAS"],
            "--ks-pass", "env:SIGNING_STORE_PASSWORD",
            "--key-pass", "env:SIGNING_KEY_PASSWORD",
            "--debuggable-apk-permitted", "false",
            "--v4-signing-enabled", "false",
            "--out", str(signed),
            str(apk),
        ], env=env, check=True)
        if not signed.is_file() or signed.stat().st_size == 0:
            raise ValueError("apksigner did not produce a signed APK")
        os.replace(signed, apk)
    finally:
        signed.unlink(missing_ok=True)


def build(apksigner):
    # The selected tag may use a different AGP signing configuration. Always create
    # the package first, then explicitly replace it with apksigner's signed output.
    build_unsigned()
    sign_release_apk(apksigner)


def sync_kernel():
    config = properties("kernel.properties")
    destination = Path(config["external.mihomo.dir"])
    if destination.exists():
        raise ValueError("Kernel destination already exists; use a clean checkout")
    subprocess.run([
        "git", "clone", "--depth", "1", "--single-branch", "--branch",
        config["external.mihomo.branch"], config["external.mihomo.repo"], str(destination),
    ], check=True)


def find_release_apk(config):
    root = Path(".")
    expected_name = (f"{config['project.name']}-v{config['project.version.name']}-"
                     "arm64-v8a-release.apk")
    # AGP 9 may place packaged artifacts outside the historical app/build/outputs
    # tree. The checkout is clean and this job builds exactly one APK; the manifest,
    # signature and ABI checks below establish that it is the intended artifact.
    apks = sorted(path for path in root.rglob("*.apk")
                  if ".gradle" not in path.parts and path.is_file())
    exact = [apk for apk in apks if apk.name == expected_name]
    candidates = exact if exact else apks
    if len(candidates) != 1:
        raise ValueError(f"Expected one Release APK, found {len(candidates)}")
    apk = candidates[0]
    if not apk.is_file() or apk.stat().st_size == 0:
        raise ValueError("Release APK is missing or empty")
    return apk, expected_name


def verify_apk_manifest(aapt2, apk, config):
    process = subprocess.run([aapt2, "dump", "badging", str(apk)], text=True, capture_output=True)
    if process.returncode != 0:
        raise ValueError("Unable to read the built APK manifest with aapt2")
    package = re.search(
        r"^package: name='([^']+)' versionCode='([^']+)' versionName='([^']+)'",
        process.stdout, re.MULTILINE,
    )
    if package is None:
        raise ValueError("Built APK manifest does not contain package version information")
    expected = (config["project.applicationId"], config["project.version.code"],
                config["project.version.name"])
    if package.groups() != expected:
        raise ValueError("APK version or application ID does not match the release source")


def verify_and_stage(apksigner, aapt2, expected_fingerprint=""):
    config = properties("gradle.properties")
    apk, name = find_release_apk(config)
    verify_apk_manifest(aapt2, apk, config)
    # Non-zero exit prevents publishing unsigned or invalid APKs.
    verification_process = subprocess.run(
        [apksigner, "verify", "--verbose", "--print-certs", str(apk)],
        text=True, capture_output=True,
    )
    verification = verification_process.stdout + verification_process.stderr
    if verification_process.returncode != 0:
        # apksigner output contains only public APK/certificate diagnostics. Limit and
        # normalize it so hosted failures remain actionable without dumping environment data.
        diagnostic = re.sub(r"[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]", "?", verification).strip()
        diagnostic = diagnostic[-4_000:] if diagnostic else "apksigner returned no diagnostic output"
        raise ValueError(f"APK signature verification failed: {diagnostic}")
    fingerprints = re.findall(
        r"(?:Signer #[0-9]+ certificate|V[0-9.]+ Signer: certificate) SHA-256 digest: ([a-fA-F0-9]+)",
        verification,
    )
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
        build(os.environ["APKSIGNER"])
    elif command == "sync-kernel":
        sync_kernel()
    elif command == "verify":
        verify_and_stage(os.environ["APKSIGNER"], os.environ["AAPT2"],
                         os.environ.get("SIGNING_CERT_SHA256", ""))
    elif command == "cleanup":
        keystore_path().unlink(missing_ok=True)
    else:
        raise ValueError("Unknown release command")


def failure_message(error):
    if isinstance(error, ValueError):
        return f"Release step failed: {error}"
    if isinstance(error, KeyError):
        return f"Release step failed: required metadata field {error} is missing"
    if isinstance(error, OSError):
        detail = error.strerror or type(error).__name__
        return f"Release step failed: operating system error {error.errno}: {detail}"
    if isinstance(error, subprocess.CalledProcessError):
        program = Path(error.cmd[0]).name if isinstance(error.cmd, (list, tuple)) else "build command"
        return f"Release step failed: {program} exited with status {error.returncode}"
    return "Release step failed"


if __name__ == "__main__":
    try:
        main()
    except (ValueError, KeyError, OSError, subprocess.CalledProcessError) as error:
        print(failure_message(error), file=sys.stderr)
        sys.exit(1)
