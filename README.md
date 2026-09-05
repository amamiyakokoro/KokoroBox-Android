<div align="center">

<img src="artwork/app-icon/profile-image.png" width="112" alt="KokoroBox icon">

# KokoroBox

A Material 3 Android proxy client powered by [Mihomo](https://github.com/MetaCubeX/mihomo).

</div>

> [!IMPORTANT]
> KokoroBox supports **arm64-v8a** devices running Android 8.0 (API 26) or newer.

## Features

- Material 3 interface with dynamic color, dark theme, and responsive navigation
- Local, remote, QR-code, and authenticated Kokoro subscription profiles
- Rule, Global, Direct, VPN, and optional Root TUN modes
- Proxy selection, connection inspection, traffic statistics, logs, and Geo data updates
- Server-synced Kokoro Custom Rules, profile overrides, configuration preview, and automatic updates
- In-app checks for stable releases published by this repository

## Install

Download the latest signed APK from [GitHub Releases](https://github.com/amamiyakokoro/KokoroBox-Android/releases/latest).

The application ID is `com.amamiyakokoro.box`. KokoroBox installs separately from older YumeBox variants, and their profiles or preferences are not migrated automatically.

## Kokoro Subscription

When adding a profile, select **Kokoro Subscription** and sign in with osu! through the system browser. Authentication returns through `kokoro://oauth/callback` and uses PKCE S256; no backend secret is embedded in the app.

Keep tokens, subscription URLs, UUIDs, signing keys, and private configurations out of logs and issue reports. See [Kokoro OAuth](docs/KokoroOAuth.md) for the client integration details.

Authenticated users can manage the same ordered Custom Rules used by the Kokoro website from **Settings → Kokoro Settings**. See [Kokoro Custom Rules](docs/KokoroCustomRules.md) for synchronization and conflict behavior.

## Build

Requirements:

- JDK 24 or newer
- Android SDK 37 and NDK `29.0.14206865`
- Kotlin command-line tools, Go, Rust, and `cargo-ndk` when rebuilding native components

```bash
git clone https://github.com/amamiyakokoro/KokoroBox-Android.git
cd KokoroBox-Android

./scripts/sync-kernel.sh meta
kotlin scripts/native-build.main.kts --all
./gradlew :app:assembleDebug
```

Build an arm64 release APK with:

```bash
./gradlew assembleReleaseArm64V8a
```

Local release signing is optional. Never commit a keystore or `signing.properties`. The repository workflow builds, signs, verifies, and publishes tagged releases using GitHub Actions Secrets; setup instructions are in [Release CI](docs/ReleaseCI.md).

## Contributing

Bug reports and focused pull requests are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting changes, and use [GitHub Issues](https://github.com/amamiyakokoro/KokoroBox-Android/issues) for reproducible bugs or feature proposals.

## Credits and license

KokoroBox is based on [YumeBox Material Design](https://github.com/Yizuka17/YumeBox-MaterialDesign), which continues the work of [YumeBox](https://github.com/YumeLira/YumeBox). Third-party components are listed in [docs/ThirdParty.md](docs/ThirdParty.md).

The project is distributed under [GNU AGPL v3](LICENSE) with the additional terms in [LICENSE-F2DLPRL](LICENSE-F2DLPRL). See the [privacy policy](PRIVACY_POLICY.md) for application data practices.
