<div align="center">

**English** | [简体中文](README_ZH_HANS.md)

<img src="logo.webp" width="96" alt="YumeBox logo">

# YumeBox MD3

[![Latest release](https://img.shields.io/github/v/release/Yizuka17/YumeBox-MaterialDesign?logo=android)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases/latest)
[![GitHub License](https://img.shields.io/github/license/Yizuka17/YumeBox-MaterialDesign?logo=gnu)](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/LICENSE)
[![Downloads](https://img.shields.io/github/downloads/Yizuka17/YumeBox-MaterialDesign/total?logo=github)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

YumeBox MD3 is a Material Design 3 / Material You fork based on [YumeBox](https://github.com/YumeRiMoe/YumeBox), an open-source Android client powered by [mihomo](https://github.com/MetaCubeX/mihomo).

</div>

## Use

YumeBox MD3 currently only supports **Android 8.0 (API 26) and above systems**.

Please go to the Release page to download the arm64-v8a release APK: [Release](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

For more information, please visit the website: [YumeBox](https://yumebox.oom-wg.dev/)

Override configuration syntax reference: [override document](https://yumebox.oom-wg.dev/override). Daily Quote API and custom JSON format reference: [Daily Quote format guide](DailyQuote.md). If this project is helpful to you, please click Star. This is the motivation for continuous updates.

## Improvements over the original project

YumeBox MD3 keeps the core capabilities of the original project while continuously improving the UI experience, configuration workflow, runtime modes, and stability:

- **Material Design 3 / Material You experience**: redesigned and optimized multiple pages; the main UI is now mostly migrated to MD3, with dynamic colors, better dark-theme adaptation, navigation bar and Topbar blur effects, smoother page transitions, and animated list sorting; theme settings now include Monet style, color intensity/vibrancy, and contrast controls, defaulting to a readability-first Tonal Spot experience.
- **More powerful override and configuration workflow**: introduced a new override system with multi-configuration switching, stacked applying, visual editing, preset routing templates, runtime configuration preview, and suffix modifiers such as `start`, `end`, `merge`, and `force`; it also adds Rust-based override parsing plus syntax checking and partial completion in the configuration editor.
- **Enhanced proxy runtime and node management**: supports Root Tun (no VPN required, but Root permission is needed), Rule / Global / Direct mode selection on the proxy page, internal dispatcher-backed configuration separation between Rule and Global modes, single-node and proxy-group delay tests, current-node delay testing from the home screens, node sorting animations, node tags and icon/flag display, persistent node selection, per-card loading feedback and cancel/restore for subscription updates, faster proxy startup, and improved bridge implementations.
- **ACG home and quote experience**: the ACG home supports wallpaper, sidebar controls, launch button, runtime information, and quote display; quotes can use the API and/or a user-defined JSON list, with manual refresh, daily refresh, refresh loading feedback, mixed API/custom selection, and an in-app documentation entry in the quote configuration page.
- **Better subscription, import, and external-control experience**: supports editing subscription URLs, link preview, configuration sorting, opening external links in the app for quick import, and improves the external controller, Web panel, and notification quick node-switching workflow.
- **Statistics, privacy, and stability**: adds a connection page and richer traffic statistics, including per-app traffic statistics when process lookup is enabled; adds language switching and privacy settings, and removes the Sentry tracker and EMAS push update service.
- **Stability and performance optimization**: improves startup blocking, runtime state freezes, dialog stutters, notification content, package size, background UI release, and many interaction details for a smoother and more fault-tolerant daily experience.

## Changelog

### v0.5.3

Summarized from commits after `v0.5.2` (`12c3cda`):

- **Release and signing**: v0.5.3 release APKs are now signed with the author's private signing key (~~definitely not because the author forgot what the signing key was~~); the arm64-v8a release workflow, JDK 24 build environment, signing store path, and certificate SHA-256 configuration were also updated.
- **ACG home and Daily Quote**: improved quote refresh, current-node delay testing, and refresh animation feedback on the ACG home; added Daily Quote API / custom JSON format guides and an in-app documentation entry in the quote configuration page.
- **Configuration, editor, and localization**: restored and improved lightweight editor syntax highlighting, expanded JSON diagnostics, editor UI text, settings text, proxy mode UI text, and Traditional Chinese localization.
- **Connection, proxy, and subscription experience**: continued MD3 polishing for connection and proxy screens, including connection cards, detail sheets, node cards, and proxy-group display; the proxy page now provides Rule / Global / Direct mode selection, with the internal dispatcher separating Rule-mode and Global-mode generated configurations; subscription updates now support per-card loading feedback plus cancel/restore behavior, with stable profile ordering.
- **Version display**: bumped the version to `v0.5.3`; the About page no longer shows the `Material You Build` suffix and now keeps the displayed version clean.

## Design and reference notes

v0.5.3 continues to narrow the legacy UI boundary: normal screens now prefer Jetpack Compose Material 3 components and in-project MD3 components, while Miuix remains only as a small compatibility layer for legacy surfaces that have not been rewritten yet. This version also removes the extra build label from the About page and keeps the displayed app version as a clean version number.

This theme refactor follows the Monet / dynamic-color setting recommendations summarized in `chat-export-2026-04-28_22-27-47.md`: use wallpaper or system colors as inspiration, keep readability as the baseline, map colors through Material roles, and give users enough but not excessive control. No proxy core/runtime kernel code was changed; the changes are limited to UI, settings, and theme derivation:

- ACG home: preserves the wallpaper, sidebar, launch button, and runtime-information layout while adding quote refresh, current-node delay testing, refresh-state feedback, and a Daily Quote configuration guide entry; it continues to follow the global theme tokens in a restrained way.

References include Android / Material documentation and related open-source implementations:

- [Material You design - Android Open Source Project](https://source.android.com/docs/core/display/material)
- [Dynamic color - Android Open Source Project](https://source.android.com/docs/core/display/dynamic-color)
- [Enable users to personalize their color experience in your app - Android Developers](https://developer.android.com/develop/ui/views/theming/dynamic-colors)
- [Material 3 color system: how the system works](https://m3.material.io/styles/color/system/how-the-system-works)
- [Material 3 color roles](https://m3.material.io/styles/color/roles)
- [material-foundation/material-color-utilities](https://github.com/material-foundation/material-color-utilities)
- [Jetpack Compose Material 3](https://developer.android.com/jetpack/androidx/releases/compose-material3)
- [Haze](https://github.com/chrisbanes/haze)
- [Compose Reorderable](https://github.com/Calvin-LL/Reorderable)
- [AboutLibraries](https://github.com/mikepenz/AboutLibraries)

## Roadmap

The focus is now shifting from "migrating to MD3" to polishing a stable MD3 experience. Future updates will continue to clean up the remaining legacy UI boundaries, reduce direct dependency on compatibility layers, and make regular screens more consistent with Material Design 3 / Material You in components, motion, spacing, color, and accessibility.

Personalization and ACG-driven experiences will also keep evolving: theme extraction from selected wallpapers will be refined, with more flexible image sources and caching strategies. The goal is to let the home page, cards, navigation bars, and other UI elements adapt naturally to user content while preserving readability. Configuration editing, runtime status display, traffic statistics, and overall stability will continue to be improved so YumeBox MD3 can keep evolving toward being more usable, polished, and fun.

## Feedback and Suggestions

If you encounter a bug, please submit it on the Issues page:
[Issues](https://github.com/Yizuka17/YumeBox-MaterialDesign/issues)

If you have ideas or suggestions for improvements, you can also submit them there.

For more discussion and feedback, please join the original project's group: [@OOM_WG](https://t.me/OOM_Group)

## Participate and contribute

If you want to make YumeBox MD3 better, please refer to [CONTRIBUTING](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/CONTRIBUTING.md). If you want to translate YumeBox into more languages, or improve the existing translation, please fork this project and create or update the corresponding translation file in the `locale/lang` directory.

## Special

This project is developed based on [YumeBox](https://github.com/YumeRiMoe/YumeBox); **~~the author knows nothing about the code in this project, and the code is either available or unavailable, with no third case~~**; AI assistance is used during development for part of the code implementation, documentation, and troubleshooting, and thanks go to the original project and related open-source projects.

And the [third-party](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/docs/ThirdParty.md) libraries used in this project.
