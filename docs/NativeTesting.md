# Native regression tests

Configuration overrides are merged and compiled by the Rust library. Its tests
cover CORS list modifiers, forced replacements, explicit `false` values, nested
fields, ordered override files, and preview/output consistency:

```sh
cargo test --manifest-path lib/native/rust/Cargo.toml --locked
```

Go consumes the compiled YAML. Its configuration tests cover field preservation,
group ordering and filtering, proxy types, subtitle extraction, and invalid input.
Run these on Linux after syncing the configured Mihomo source:

```sh
python3 scripts/ci-release.py sync-kernel
cd lib/native/go
go test -race -tags=foss,with_gvisor,cmfa ./native/config ./native/tunnel
```

The Verify workflow runs both suites before building the Android native runtime.

On macOS, `native/config` imports the runtime's Linux-only socket-owner code.
Run the Rust suite locally and execute the Go configuration suite on Linux or
cross-compile it for an Android emulator. For example, from the repository root:

```sh
task_ndk="$ANDROID_HOME/ndk/29.0.14206865/toolchains/llvm/prebuilt/darwin-x86_64"
cd lib/native/go
GOOS=android GOARCH=arm64 CGO_ENABLED=1 \
  CC="$task_ndk/bin/aarch64-linux-android26-clang" \
  go test -c -tags=foss,with_gvisor,cmfa \
  -o /tmp/kokorobox-config.test ./native/config
adb push /tmp/kokorobox-config.test /data/local/tmp/kokorobox-config.test
adb shell chmod 700 /data/local/tmp/kokorobox-config.test
adb shell 'TMPDIR=/data/local/tmp /data/local/tmp/kokorobox-config.test -test.v'
adb shell rm /data/local/tmp/kokorobox-config.test
```

The tests use local configuration fixtures and do not require a VPN or a live
subscription.
