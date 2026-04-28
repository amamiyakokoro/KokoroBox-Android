<div align="center">

**English** | [简体中文](README_ZH_HANS.md)

<img src="logo.webp" width="96" alt="YumeBox logo">

# YumeBox-MaterialDesign

[![Latest release](https://img.shields.io/github/v/release/Yizuka17/YumeBox-MaterialDesign?logo=android)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases/latest)
[![GitHub License](https://img.shields.io/github/license/Yizuka17/YumeBox-MaterialDesign?logo=gnu)](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/LICENSE)
[![Downloads](https://img.shields.io/github/downloads/Yizuka17/YumeBox-MaterialDesign/total?logo=github)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

A Material Design fork based on [YumeBox](https://github.com/YumeRiMoe/YumeBox), an open-source Android client powered by [mihomo](https://github.com/MetaCubeX/mihomo).

</div>

## Use

YumeBox-MaterialDesign currently only supports **Android 8.0 (API 26) and above systems**.

Please go to the Release page to download the installation package for the corresponding architecture: [Release](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

For more information, please visit the website: [YumeBox](https://yumebox.oom-wg.dev/)

Override configuration syntax reference: [override document](https://yumebox.oom-wg.dev/override). If this project is helpful to you, please click Star. This is the motivation for continuous updates.

## Improvements over the original project

YumeBox-MaterialDesign keeps the core capabilities of the original project while continuously improving the UI experience, configuration workflow, runtime modes, and stability:

- **Material Design / Material You experience**: redesigned and optimized multiple pages with dynamic colors, better dark-theme adaptation, navigation bar and Topbar blur effects, smoother page transitions, and animated list sorting.
- **More powerful override and configuration workflow**: introduced a new override system with multi-configuration switching, stacked applying, visual editing, preset routing templates, runtime configuration preview, and suffix modifiers such as `start`, `end`, `merge`, and `force`; it also adds Rust-based override parsing plus syntax checking and partial completion in the configuration editor.
- **Enhanced proxy runtime and node management**: supports Root Tun (no VPN required, but Root permission is needed), single-node and proxy-group delay tests, node sorting animations, node tags and icon/flag display, persistent node selection, faster proxy startup, and improved bridge implementations.
- **Better subscription, import, and external-control experience**: supports editing subscription URLs, link preview, configuration sorting, opening external links in the app for quick import, and improves the external controller, Web panel, and notification quick node-switching workflow.
- **Statistics, privacy, and lightweight build**: adds a connection page and richer traffic statistics, including per-app traffic statistics when process lookup is enabled; supports YumeBox Lite, adds language switching and privacy settings, and removes the Sentry tracker and EMAS push update service.
- **Stability and performance optimization**: improves startup blocking, runtime state freezes, dialog stutters, notification content, package size, background UI release, and many interaction details for a smoother and more fault-tolerant daily experience.

## Roadmap

Future updates will continue to improve personalization and content-driven experiences. Planned ideas include an option to extract theme colors from the ACG image selected by the user, allowing the home page, cards, navigation bar, and other UI elements to change dynamically with the artwork; daily web-based updates for ACG images and hitokoto-style quotes, with more flexible image sources, caching strategies, and manual refresh controls; and continuous improvements to the theme system, configuration editor, runtime status display, and lightweight builds, so YumeBox-MaterialDesign can keep evolving toward being more usable, polished, and fun.

## Feedback and Suggestions

If you encounter a bug, please submit it on the Issues page:
[Issues](https://github.com/Yizuka17/YumeBox-MaterialDesign/issues)

If you have ideas or suggestions for improvements, you can also submit them there.

For more discussion and feedback, please join the original project's group: [@OOM_WG](https://t.me/OOM_Group)

## Participate and contribute

If you want to make YumeBox-MaterialDesign better, please refer to [CONTRIBUTING](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/CONTRIBUTING.md). If you want to translate YumeBox into more languages, or improve the existing translation, please fork this project and create or update the corresponding translation file in the `locale/lang` directory.

## Special

This project is developed based on [YumeBox](https://github.com/YumeRiMoe/YumeBox); **~~the author knows nothing about the code in this project, and the code is either available or unavailable, with no third case~~**; AI assistance is used during development for part of the code implementation, documentation, and troubleshooting, and thanks go to the original project and related open-source projects.

And the [third-party](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/docs/ThirdParty.md) libraries used in this project.
