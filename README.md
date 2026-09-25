<div align="center">

<img src="artwork/app-icon/readme-icon.png" width="112" alt="KokoroBox icon">

# KokoroBox

A Material 3 Android client for [Mihomo](https://github.com/MetaCubeX/mihomo), with optional Kokoro integration.

[Privacy](PRIVACY_POLICY.md) · [License](LICENSE)

</div>

## Features

- Rule, Global, Direct, VPN, and optional Root TUN modes
- Local, remote, QR-code, and authenticated Kokoro subscription profiles
- Proxy selection, connections, traffic statistics, logs, and Geo data updates
- Kokoro Custom Rules, profile overrides, and configuration preview
- Manual and optional daily checks for stable GitHub releases

## Supported platforms

KokoroBox supports **arm64-v8a** devices running Android 8.0 (API 26) or newer. Its application ID is `com.amamiyakokoro.box`; it does not replace or migrate YumeBox data.

## Get started

Download the latest signed APK from [GitHub Releases](https://github.com/amamiyakokoro/KokoroBox-Android/releases/latest).

## Development

Requirements: JDK 25, Android SDK 37, NDK `29.0.14206865`, CMake, Kotlin CLI, Go, Rust, and `cargo-ndk`.

```bash
git clone https://github.com/amamiyakokoro/KokoroBox-Android.git
cd KokoroBox-Android
python3 scripts/ci-release.py sync-kernel
kotlin scripts/native-build.main.kts --all
./gradlew :app:assembleDebug
```

For an arm64 release APK, run `./gradlew assembleReleaseArm64V8a` after the same native build steps. See [release build instructions](docs/ReleaseCI.md) for signing and publishing.

Never commit a keystore or `signing.properties`.

## Documentation

- [Release build and publishing guide](docs/ReleaseCI.md)
- [Kokoro OAuth](docs/KokoroOAuth.md)
- [Kokoro Custom Rules](docs/KokoroCustomRules.md)
- [Privacy policy](PRIVACY_POLICY.md)

## License

KokoroBox is based on [YumeBox Material Design](https://github.com/Yizuka17/YumeBox-MaterialDesign), which continues [YumeBox](https://github.com/YumeLira/YumeBox).

The project is licensed under [GNU AGPL v3](LICENSE) with the additional terms in [LICENSE-F2DLPRL](LICENSE-F2DLPRL).
