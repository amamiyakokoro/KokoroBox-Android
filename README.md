<div align="center">

<img src="artwork/app-icon/profile-image.png" width="112" alt="KokoroBox icon">

# KokoroBox

A Material 3 Android client for [Mihomo](https://github.com/MetaCubeX/mihomo), with optional Kokoro integration.

</div>

> [!IMPORTANT]
> KokoroBox supports **arm64-v8a** devices running Android 8.0 (API 26) or newer.

## Highlights

- Rule, Global, Direct, VPN, and optional Root TUN modes
- Local, remote, QR-code, and authenticated Kokoro subscription profiles
- Proxy selection, connections, traffic statistics, logs, and Geo data updates
- Kokoro Custom Rules, profile overrides, and configuration preview
- Manual and optional daily checks for stable GitHub releases

## Download

Download the latest signed APK from [GitHub Releases](https://github.com/amamiyakokoro/KokoroBox-Android/releases/latest).

The application ID is `com.amamiyakokoro.box`. Existing YumeBox installations and data are not replaced or migrated.

## Build

Requirements: JDK 24+, Android SDK 37, and NDK `29.0.14206865`.

```bash
git clone https://github.com/amamiyakokoro/KokoroBox-Android.git
cd KokoroBox-Android
./gradlew :app:assembleDebug
```

To rebuild native components, install Kotlin command-line tools, Go, Rust, and `cargo-ndk`, then run:

```bash
./scripts/sync-kernel.sh meta
kotlin scripts/native-build.main.kts --all
```

Build an arm64 release APK with:

```bash
./gradlew assembleReleaseArm64V8a
```

Never commit a keystore or `signing.properties`.

## Documentation

- [Kokoro OAuth and PKCE](docs/KokoroOAuth.md)
- [Kokoro Custom Rules](docs/KokoroCustomRules.md)
- [Release signing and CI](docs/ReleaseCI.md)
- [Third-party components](docs/ThirdParty.md)

## Project

KokoroBox is based on [YumeBox Material Design](https://github.com/Yizuka17/YumeBox-MaterialDesign), which continues [YumeBox](https://github.com/YumeLira/YumeBox).

Contributions are welcome; read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. The project is licensed under [GNU AGPL v3](LICENSE) with the additional terms in [LICENSE-F2DLPRL](LICENSE-F2DLPRL). See the [privacy policy](PRIVACY_POLICY.md).
