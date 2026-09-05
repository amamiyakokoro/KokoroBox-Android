import base64
import importlib.util
import json
import os
from pathlib import Path
import shutil
import stat
import subprocess
import tempfile
import unittest
from unittest.mock import patch
import zipfile

SPEC = importlib.util.spec_from_file_location("ci_release", Path(__file__).parents[1] / "ci-release.py")
ci = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ci)


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        original = Path.cwd()
        os.chdir(self.root)
        self.addCleanup(os.chdir, original)
        self.env = {
            "SIGNING_KEYSTORE_BASE64": base64.b64encode(b"test-keystore").decode(),
            "SIGNING_STORE_PASSWORD": "spaces $(`bad`) \\ = : \" \n password",
            "SIGNING_KEY_ALIAS": "ci-test",
            "SIGNING_KEY_PASSWORD": "another-secret",
            "RUNNER_TEMP": str(self.root),
        }
        Path("gradle.properties").write_text(
            "project.version.name=0.5.5\nproject.applicationId=com.amamiyakokoro.box\n"
            "android.ndkVersion=29.0.14206865\nandroid.compileSdk=37\n"
        )

    def test_tags_are_strict(self):
        self.assertEqual(ci.validate_tag("v0.5.5"), "v0.5.5")
        for tag in ("0.5.5", "dev", "v0.5.5-rc1", "v0.5.5\n", "v0.5.5;echo bad", "../v0.5.5"):
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                ci.validate_tag(tag)

    def test_missing_each_secret_fails(self):
        for name in ci.SIGNING_NAMES:
            with self.subTest(name=name), self.assertRaises(ValueError) as error:
                ci.check_secrets({**self.env, name: ""})
            self.assertIn(name, str(error.exception))
            self.assertNotIn("another-secret", str(error.exception))

    def test_restore_uses_private_permissions_and_never_overwrites(self):
        destination = self.root / "key"
        ci.restore_keystore(self.env, destination)
        self.assertEqual(destination.read_bytes(), b"test-keystore")
        self.assertEqual(stat.S_IMODE(destination.stat().st_mode), 0o600)
        with self.assertRaises(FileExistsError):
            ci.restore_keystore(self.env, destination)

    def test_bad_base64_and_empty_keystore_fail_without_leaking_input(self):
        for value in ("private-secret-not-base64", "   "):
            with self.subTest(value=value), self.assertRaises(ValueError) as error:
                ci.restore_keystore({**self.env, "SIGNING_KEYSTORE_BASE64": value}, self.root / "key")
            self.assertNotIn(value, str(error.exception))
            self.assertFalse((self.root / "key").exists())

    def test_signing_passwords_are_only_in_environment(self):
        with patch.dict(os.environ, self.env, clear=True), patch.object(ci.subprocess, "run") as run:
            ci.restore_keystore(self.env, ci.keystore_path())
            ci.build()
        args, options = run.call_args
        for name in ci.SIGNING_NAMES:
            self.assertNotIn(self.env[name], str(args))
        self.assertEqual(options["env"]["ORG_GRADLE_PROJECT_signing.store.password"], self.env["SIGNING_STORE_PASSWORD"])
        self.assertEqual(options["env"]["ORG_GRADLE_PROJECT_signing.key.password"], "another-secret")
        self.assertNotIn("SIGNING_KEYSTORE_BASE64", options["env"])
        self.assertIn("--no-configuration-cache", args[0])
        self.assertFalse(Path("signing.properties").exists())

    def test_no_restored_store_cannot_build(self):
        with self.assertRaises(ValueError):
            ci.signing_environment(self.env, self.root / "missing")

    def test_metadata_rejects_mismatched_version_or_commit(self):
        with self.assertRaises(ValueError):
            ci.metadata("v0.5.6")
        with patch.object(ci.subprocess, "check_output", side_effect=["head\n", "tag\n"]):
            with self.assertRaises(ValueError):
                ci.metadata("v0.5.5")

    def test_metadata_outputs_exact_selected_commit(self):
        with patch.dict(os.environ, {"GITHUB_OUTPUT": str(self.root / "outputs")}), \
                patch.object(ci.subprocess, "check_output", return_value="a" * 40 + "\n"):
            ci.metadata("v0.5.5")
        self.assertIn("tag=v0.5.5\n", Path("outputs").read_text())
        self.assertIn("sha=" + "a" * 40, Path("outputs").read_text())
        self.assertIn("sdk=37.0\n", Path("outputs").read_text())

    def test_metadata_sdk_package_includes_required_minor_version(self):
        original = Path("gradle.properties").read_text()
        for major, minor, expected in ((36, 0, "36"), (36, 1, "36.1"),
                                       (37, 0, "37.0"), (37, 2, "37.2")):
            with self.subTest(major=major, minor=minor):
                Path("gradle.properties").write_text(
                    original.replace("android.compileSdk=37", f"android.compileSdk={major}")
                    + f"android.compileSdkMinor={minor}\n"
                )
                with patch.object(ci.subprocess, "check_output", return_value="a" * 40), \
                        patch.object(ci, "output") as output:
                    ci.metadata("v0.5.5")
                output.assert_any_call("sdk", expected)

    def make_apk(self, missing_lib=False, extra_abi=False, nested=False):
        directory = Path("app/build/outputs/apk/release/arm64-v8a" if nested
                         else "app/build/outputs/apk/release")
        directory.mkdir(parents=True)
        name = "KokoroBox-v0.5.5-arm64-v8a-release.apk"
        with zipfile.ZipFile(directory / name, "w") as archive:
            for lib in ci.NATIVE_LIBS[1:] if missing_lib else ci.NATIVE_LIBS:
                archive.writestr(f"lib/arm64-v8a/{lib}", b"test")
            if extra_abi:
                archive.writestr("lib/armeabi-v7a/libclash.so", b"test")
        (directory / "output-metadata.json").write_text(json.dumps({
            "applicationId": "com.amamiyakokoro.box", "variantName": "release",
            "elements": [{"versionName": "0.5.5", "outputFile": name}],
        }))
        return name

    def certificate(self):
        return "Signer #1 certificate DN: CN=CI Release\nSigner #1 certificate SHA-256 digest: " + "ab" * 32 + "\n"

    def test_verified_versioned_apk_and_hash_are_staged(self):
        name = self.make_apk()
        with patch.object(ci.subprocess, "run", return_value=subprocess.CompletedProcess([], 0, self.certificate(), "")):
            ci.verify_and_stage("apksigner", ":".join(["AB"] * 32))
        self.assertTrue((Path("publish") / name).is_file())
        self.assertIn(name, Path("publish/SHA256SUMS").read_text())
        self.assertEqual(Path("publish/SIGNING-CERT-SHA256.txt").read_text(), "ab" * 32 + "\n")

    def test_unsigned_apk_fails_before_staging(self):
        self.make_apk()
        failure = subprocess.CompletedProcess([], 1, "DOES NOT VERIFY\n", "ERROR: malformed APK\n")
        with patch.object(ci.subprocess, "run", return_value=failure):
            with self.assertRaisesRegex(ValueError, "DOES NOT VERIFY") as error:
                ci.verify_and_stage("apksigner")
        self.assertIn("malformed APK", str(error.exception))
        self.assertFalse(Path("publish").exists())

    def test_wrong_certificate_and_debug_certificate_are_rejected(self):
        self.make_apk()
        for verification, expected in ((self.certificate(), "00" * 32),
                                       (self.certificate().replace("CI Release", "Android Debug"), "")):
            result = subprocess.CompletedProcess([], 0, verification, "")
            with patch.object(ci.subprocess, "run", return_value=result), self.assertRaises(ValueError):
                ci.verify_and_stage("apksigner", expected)
        self.assertFalse(Path("publish").exists())

    def test_missing_native_library_is_rejected(self):
        self.make_apk(missing_lib=True)
        result = subprocess.CompletedProcess([], 0, self.certificate(), "")
        with patch.object(ci.subprocess, "run", return_value=result), self.assertRaises(ValueError):
            ci.verify_and_stage("apksigner")

    def test_extra_32bit_library_is_rejected(self):
        self.make_apk(extra_abi=True)
        result = subprocess.CompletedProcess([], 0, self.certificate(), "")
        with patch.object(ci.subprocess, "run", return_value=result), self.assertRaises(ValueError):
            ci.verify_and_stage("apksigner")

    def test_accepts_build_tools_37_certificate_output(self):
        name = self.make_apk()
        certificate = (
            "V2 Signer: certificate DN: CN=CI Release\n"
            "V2 Signer: certificate SHA-256 digest: " + "ab" * 32 + "\n"
        )
        result = subprocess.CompletedProcess([], 0, certificate, "")
        with patch.object(ci.subprocess, "run", return_value=result):
            ci.verify_and_stage("apksigner")
        self.assertTrue((Path("publish") / name).is_file())

    def test_missing_apk_is_reported_before_running_apksigner(self):
        self.make_apk()
        Path("app/build/outputs/apk/release/KokoroBox-v0.5.5-arm64-v8a-release.apk").unlink()
        with patch.object(ci.subprocess, "run") as run, \
                self.assertRaisesRegex(ValueError, "missing or empty"):
            ci.verify_and_stage("apksigner")
        run.assert_not_called()

    def test_finds_agp_single_abi_nested_release_output(self):
        name = self.make_apk(nested=True)
        result = subprocess.CompletedProcess([], 0, self.certificate(), "")
        with patch.object(ci.subprocess, "run", return_value=result):
            ci.verify_and_stage("apksigner")
        self.assertTrue((Path("publish") / name).is_file())

    def test_rejects_missing_or_ambiguous_release_metadata(self):
        with self.assertRaisesRegex(ValueError, "found 0"):
            ci.find_release_output()
        self.make_apk()
        nested = Path("app/build/outputs/apk/other")
        nested.mkdir(parents=True)
        shutil.copyfile("app/build/outputs/apk/release/output-metadata.json",
                        nested / "output-metadata.json")
        with self.assertRaisesRegex(ValueError, "found 2"):
            ci.find_release_output()

    def test_cleanup_is_idempotent(self):
        with patch.dict(os.environ, self.env), patch.object(ci.sys, "argv", ["ci-release.py", "cleanup"]):
            ci.restore_keystore(self.env, ci.keystore_path())
            ci.main()
            ci.main()
            self.assertFalse(ci.keystore_path().exists())

    def test_main_reports_safe_os_error_without_path_or_secret(self):
        secret_path = "/private/keystore/private-secret.jks"
        error = FileNotFoundError(2, "No such file or directory", secret_path)
        rendered = ci.failure_message(error)
        self.assertIn("operating system error 2", rendered)
        self.assertNotIn(secret_path, rendered)


if __name__ == "__main__":
    unittest.main()
