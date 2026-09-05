# Signed Release APKs with GitHub Actions

The **Release APK** workflow (`.github/workflows/release.yml`) builds an arm64
Release APK from an existing `vMAJOR.MINOR.PATCH` tag. Signing credentials belong
in **GitHub Actions Secrets**, never in Git, workflow YAML or an issue/comment.
This workflow replaces the stable release chain; other legacy preview/PR workflows
are separate and are not the recommended route for signing a public release.

## One-time setup

On the repository, open **Settings → Environments**, create an environment named
**release-signing**, and add these **Environment secrets** (case-sensitive):

| Secret | Value |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | Base64 of your existing release `.keystore` / `.jks` file |
| `SIGNING_STORE_PASSWORD` | Keystore password |
| `SIGNING_KEY_ALIAS` | Alias of the existing release signing key |
| `SIGNING_KEY_PASSWORD` | Password for that key |

Use the same key that signed the APK already distributed to users and registered
with Android developer verification. Do not generate a new key for CI. These are
not Google Play upload credentials, OAuth secrets, or a personal GitHub token.

You can upload the keystore directly from macOS using GitHub CLI without printing
its content or creating a Base64 file in the repository:

```sh
base64 -i release.keystore | gh secret set SIGNING_KEYSTORE_BASE64 --env release-signing --repo amamiyakokoro/KokoroBox-Android
gh secret set SIGNING_STORE_PASSWORD --env release-signing --repo amamiyakokoro/KokoroBox-Android
gh secret set SIGNING_KEY_ALIAS --env release-signing --repo amamiyakokoro/KokoroBox-Android
gh secret set SIGNING_KEY_PASSWORD --env release-signing --repo amamiyakokoro/KokoroBox-Android
```

The last three commands prompt for values; do not put passwords directly in shell
commands/history. On Linux, use `base64 release.keystore` for the first command.
Base64 is encoding, **not encryption**; treat the encoded value like the private
key itself. GitHub secrets have a 48 KB size limit, including Base64 expansion.

Optionally add an environment **variable** named `SIGNING_CERT_SHA256` containing the
SHA-256 fingerprint of your registered release signing certificate. Colons are
accepted. The workflow then refuses an APK signed with a different certificate.
This is the **certificate** fingerprint, not the APK file hash. Only the public
certificate fingerprint is uploaded alongside the APK, never the private key.

See [GitHub's secrets guide](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-secrets).

## Build an existing tag, including v0.5.5

1. Push the new workflow and helper to `dev` (the repository's default branch).
2. Open **Actions → Release APK → Run workflow**.
3. Select the updated `dev` branch for the workflow definition.
4. Enter `v0.5.5` in **release_tag**.
5. Leave **publish_release** off for an artifact-only build, or turn it on to
   upload the verified APK to a GitHub Release.

Automation is taken from the selected workflow branch, but **all app/native sources
are checked out from the entered tag**. The workflow checks that the tag exists,
the checkout commit matches it and `project.version.name` matches the version.
It will not label the latest `dev` source as an older release.

The existing `v0.5.5` tag points to `38ad867a`, before the About-screen and ACG
spacing fixes. Rebuilding it does not include those later fixes. Use a new version
and tag to release newer changes; do not move an already distributed tag.

Re-running an old failed workflow run uses that run's old definition. Use a **new
manual run** from the updated branch to pick up this signing workflow. GitHub must
have the workflow on the default branch for manual dispatch to be available.

## Build future releases automatically

Update `project.version.name` and `project.version.code` in `gradle.properties`,
commit them together with the intended changes, then push the branch and tag:

```sh
git push origin dev
git tag -a v0.5.6 -m "KokoroBox v0.5.6"
git push origin v0.5.6
```

Tag pushes automatically build and upload **Actions artifacts**. Publishing a
GitHub Release is deliberately opt-in via the manual-run checkbox; it is not a
Google Play upload and requires no Google Play service-account credentials.

After success, download the artifact named `KokoroBox-v0.5.5-arm64-release` from the
run summary. It contains:

```text
KokoroBox-v0.5.5-arm64-v8a-release.apk
SHA256SUMS
SIGNING-CERT-SHA256.txt
```

Artifacts are retained for 14 days. Optional GitHub Release publishing uses the
built-in `GITHUB_TOKEN`, does not move tags, and does not overwrite existing assets.
If the release already contains the same filenames, upload fails instead of
silently replacing a distributed binary. Release publishing has a separate job
with `contents: write`; the signing/build job only has `contents: read`.

## Build and security checks

- Installs JDK 25, Kotlin, Go (from `go.mod`), Rust, Android SDK/NDK and CMake.
- Checks out the Mihomo revision in the tag's `kernel.properties`, without changing
  it to the latest branch or relying on checked-in/prebuilt `.so` files.
- Builds Go/Rust/JNI libraries, Geo assets and generated locale sources before Gradle.
- Requires all four signing secrets; there is no unsigned/debug-signing fallback.
- Restores the keystore under `RUNNER_TEMP` with mode `0600`, immediately before signing.
- Passes passwords through Gradle environment project properties, not command-line
  arguments or `signing.properties`; disables build/configuration caches for signing.
- Verifies the APK signature, application ID, version and required arm64 libraries.
- Removes the temporary keystore even after a failed build. Only the APK, file hash
  and public signing fingerprint are eligible for artifact upload.

Keep these values only in the **release-signing environment**, not duplicated as
repository-wide secrets: legacy scheduled/preview workflows use `secrets: inherit`.
Environment-scoped secrets are only provided to jobs using that environment.
Consider required reviewers and deployment rules allowing `dev` (manual runs) and
`v*` tags. Existing users of repository-wide signing secrets should remove those
copies after configuring the environment.

Keep branch/tag creation restricted to trusted maintainers. Any workflow or build
code with access to signing secrets is trusted code; review changes to it carefully.
This workflow does not run on pull requests or inherit signing secrets into the
legacy preview/PR chain. Signing does not guarantee Play Protect will suppress warnings.

## Local validation

```sh
PYTHONDONTWRITEBYTECODE=1 python3 -m unittest discover -s scripts/tests -p 'test_ci_release.py' -v
actionlint .github/workflows/release.yml
```

Unit tests use dummy signing material and mocked signature reports. They do not
prove a full Linux native build or GitHub-hosted release signing succeeded. The
first hosted run still needs the repository secrets and network/toolchain access.
