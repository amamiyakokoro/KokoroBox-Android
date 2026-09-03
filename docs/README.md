<div align="center">

<img src="../artwork/app-icon/profile-image.png" width="112" alt="KokoroBox icon">

# KokoroBox

An Android proxy client powered by Mihomo, designed around a modern Material 3 experience.

[![Latest release](https://img.shields.io/github/v/release/amamiyakokoro/KokoroBox?logo=android)](https://github.com/amamiyakokoro/KokoroBox/releases/latest)
[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/github/license/amamiyakokoro/KokoroBox?logo=gnu)](../LICENSE)

**English**

</div>

> [!IMPORTANT]
> KokoroBox is currently distributed for **arm64-v8a** devices and requires Android 8.0 (API 26) or newer.

## About

KokoroBox is an open-source Android client built on [Mihomo](https://github.com/MetaCubeX/mihomo). It continues the work of [YumeBox](https://github.com/YumeLira/YumeBox) with a Material 3 interface, profile management, runtime configuration tools, traffic statistics, and first-class integration with the Kokoro subscription service.

The Android application ID is `com.amamiyakokoro.box`. The optional Sub-Store extension uses `com.amamiyakokoro.box.extension`.

## Highlights

- **Material 3 interface** — dynamic color, dark theme, responsive navigation, polished motion, and consistent components.
- **Multiple profile sources** — subscription URLs, local files, QR codes, and authenticated Kokoro accounts.
- **Mihomo runtime controls** — Rule, Global, Direct, VPN, and optional Root TUN modes.
- **Subscription management** — configurable protocol, plan, ISP, relay/direct mode, rule source, final route, and update interval.
- **Overrides and routing** — visual editing, reusable presets, custom routing, configuration preview, and stacked overrides.
- **Proxy tools** — proxy-group selection, latency testing, sorting, node labels, flags, and persistent selections.
- **Sub-Store and Geo data** — integrated Sub-Store workflow plus GeoIP, GeoSite, country database, and ASN updates.
- **Diagnostics** — connection details, traffic statistics, service notifications, and exportable logs.

## Kokoro account

KokoroBox can obtain a Mihomo subscription without placing a subscription UUID in the profile URL.

1. Select **Kokoro account** when adding a profile.
2. Sign in with osu! using the system browser.
3. Return to the app through `kokoro://oauth/callback`.
4. Choose the subscription options and create the profile.

Authentication uses the public API at `https://amamiyakoko.ro/api`. Access and refresh tokens are stored as an AES-GCM encrypted credential blob whose key is protected by Android Keystore. OAuth state is verified before exchanging the one-time code, token refresh is single-flight, and credentials are not written to profile URLs or application logs.

KokoroBox does not contain an osu! client secret, API secret, or App authentication secret.

## Install

Download the newest APK from [GitHub Releases](https://github.com/amamiyakokoro/KokoroBox/releases/latest), then allow your browser or file manager to install packages when Android prompts you.

Changing the application ID made KokoroBox a separate Android application. An older YumeBox MD3 installation will not be upgraded in place, and its profiles, preferences, and login session are not migrated automatically.

## Build from source

### Requirements

- Android Studio with Android SDK 37
- JDK 25
- Android NDK `29.0.14206865`
- Kotlin command-line tools, Go, Rust, and `cargo-ndk` when rebuilding native libraries

Clone the repository:

```bash
git clone --recurse-submodules https://github.com/amamiyakokoro/KokoroBox.git
cd KokoroBox
```

The app requires `libclash.so`, `liboverride.so`, and `libbridge.so` under `jniLibs/<abi>/`. To rebuild the native runtime and Geo assets locally:

```bash
./scripts/sync-kernel.sh alpha
kotlin scripts/native-build.main.kts --all
```

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

Build the arm64 release APK or Android App Bundle:

```bash
./gradlew assembleReleaseArm64V8a
./gradlew :app:bundleRelease
```

Expected outputs include:

```text
app/build/outputs/apk/debug/KokoroBox-arm64-v8a-debug.apk
app/build/outputs/apk/release/KokoroBox-arm64-v8a-release.apk
app/build/outputs/bundle/release/KokoroBox-release.aab
```

Release signing is optional for local development. Keep signing properties and keystores outside version control.

## Project structure

| Path | Purpose |
| --- | --- |
| `app` | Android UI, navigation, profiles, and app integration |
| `core` | Mihomo bridge and shared core models |
| `data` | Persistent settings and secure Kokoro session storage |
| `runtime` | Client/service communication and proxy runtime |
| `feature` | Proxy, override, editor, Meta, and Sub-Store features |
| `extension` | Optional Sub-Store native extension APK |
| `locale` | English, Simplified Chinese, and Traditional Chinese strings |
| `scripts` | Native library, kernel, locale, and release tooling |

## Documentation

- [Daily Quote API and custom JSON](DailyQuote.md)
- [Third-party projects and libraries](ThirdParty.md)
- [Privacy policy](../PRIVACY_POLICY.md)
- [Contribution guide](../CONTRIBUTING.md)

## Contributing

Bug reports and focused pull requests are welcome. Please use [GitHub Issues](https://github.com/amamiyakokoro/KokoroBox/issues) for reproducible bugs and feature proposals. Translation changes belong under `locale/lang`.

Never commit access tokens, subscription UUIDs, signing keys, OAuth secrets, or generated private configuration files.

## Credits and license

KokoroBox is derived from [YumeBox](https://github.com/YumeLira/YumeBox) and depends on [Mihomo](https://github.com/MetaCubeX/mihomo) and other open-source projects listed in [ThirdParty.md](ThirdParty.md).

The project is distributed under the terms in [LICENSE](../LICENSE) and [LICENSE-F2DLPRL](../LICENSE-F2DLPRL). Individual dependencies remain subject to their respective licenses.
