<div align="center">

**简体中文** | [English](README.md)

<img src="logo.webp" style="width: 96px;" alt="logo">

## YumeBox MD3

[![Latest release](https://img.shields.io/github/v/release/Yizuka17/YumeBox-MaterialDesign?label=Release&logo=github)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases/latest)
[![GitHub License](https://img.shields.io/github/license/Yizuka17/YumeBox-MaterialDesign?logo=gnu)](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/LICENSE)
[![Downloads](https://img.shields.io/github/downloads/Yizuka17/YumeBox-MaterialDesign/total?logo=github)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

**YumeBox MD3 是基于 [YumeBox](https://github.com/YumeRiMoe/YumeBox) 的 Material Design 3 / Material You 分支，一个基于 [mihomo](https://github.com/MetaCubeX/mihomo) 内核的开源 Android 客户端。**

</div>

## 使用

YumeBox MD3 目前仅支持 **Android 8.0（API 26）及以上系统**。

请前往 Release 页面下载 arm64-v8a release 安装包：[Release](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

更多内容可访问官网：[YumeBox](https://yumebox.oom-wg.dev/)

覆写配置语法参考：[override 文档](https://yumebox.oom-wg.dev/override)。每日一言 API 与用户自定义 JSON 格式参考：[每日一言格式说明](DailyQuote_ZH_HANS.md)。如果这个项目对你有帮助，请点下 Star，这是持续更新的动力。

## 相较原项目的主要改进

YumeBox MD3 在保留原项目核心能力的基础上，围绕界面体验、配置管理、运行方式和稳定性进行了持续改进：

- **Material Design 3 / Material You 体验**：重构并优化多个页面，当前主体界面已基本完成 MD3 化，加入动态取色、深色主题适配、导航栏与 Topbar 模糊效果、更顺滑的页面跳转和列表排序动画；主题设置中新增莫奈风格、彩度/活力与对比度调节，默认保持可读性优先的 Tonal Spot 体验。
- **更强的覆写与配置能力**：引入全新的覆写系统，支持多配置切换、叠加应用、可视化编辑、预设分流模板、运行时配置预览，以及 `start`、`end`、`merge`、`force` 等后缀修饰符；同时加入基于 Rust 的覆写解析与配置编辑器语法检查/部分补全。
- **代理运行与节点管理增强**：支持 Root Tun（无需 VPN，但需要 Root 权限）、单节点与代理组测速、首页当前节点测速、节点排序动画、节点标签与图标/旗帜显示、节点选择持久化，订阅更新支持单卡片加载反馈与取消恢复，并优化代理启动流程和部分 bridge 实现。
- **ACG 首页与一言体验**：ACG 首页支持壁纸、侧栏、启动按钮、运行信息与一言展示；一言可分别启用 API 与用户自定义 JSON 列表，支持手动刷新、每日自动刷新、刷新加载动画、自定义/接口混合选取，并在配置页内提供说明文档跳转。
- **订阅、导入与外部控制优化**：支持修改订阅链接、链接预览、配置排序、外部链接在 App 内打开并快速导入，优化外部控制器、Web 面板和通知栏快捷节点切换体验。
- **统计、隐私与轻量化**：新增连接页面和更完善的流量统计（包含 App 流量统计，需要开启查找进程），支持 YumeBox Lite，加入语言切换与隐私设置，并移除 Sentry 跟踪器和 EMAS 推送更新。
- **稳定性与性能优化**：优化启动阻塞、运行时状态卡死、弹窗卡顿、通知栏内容、打包体积、后台释放 UI 与多处交互细节，提升日常使用的流畅度与容错能力。

## 更新日志

### v0.5.3

基于 `v0.5.2`（`12c3cda`）之后的提交整理：

- **Release 与签名**：v0.5.3 Release 已改用作者私有签名（~~绝对不是因为作者忘记了签名密钥是什么~~）；同步整理 arm64-v8a Release 构建工作流、JDK 24 构建环境、签名文件路径与证书 SHA-256 配置。
- **ACG 首页与一言**：优化 ACG 首页的一言刷新、当前节点测速与刷新动画反馈；新增每日一言 API / 用户自定义 JSON 格式说明文档，并在一言配置页加入说明文档入口。
- **配置、编辑器与国际化**：恢复并完善编辑器轻量语法高亮，补充 JSON 诊断、编辑器界面、多处设置项与代理模式界面的国际化文本；新增繁体中文适配。
- **连接、代理与订阅体验**：继续 MD3 化连接与代理相关界面，优化连接卡片、详情弹窗、节点卡片和代理组显示；订阅更新加入单卡片加载反馈与取消恢复逻辑，配置排序保持稳定。
- **版本展示**：版本号更新为 `v0.5.3`，关于页移除 `Material You Build` 后缀，仅保留纯版本号展示。

## 设计与资料参考

v0.5.3 继续收敛旧 UI 边界，普通页面优先使用 Jetpack Compose Material 3 组件与项目内 MD3 组件；Miuix 仅作为少量遗留/兼容边界保留，后续会继续减少直接依赖。本版本同时移除了关于页版本名中的额外构建标签，仅保留纯版本号展示。

本次主题重构参考了 `chat-export-2026-04-28_22-27-47.md` 中关于莫奈动态取色的设置建议：以壁纸/系统源色为灵感、以可读性为底线、以 Material 色彩角色为结构，并为用户保留适度控制权。实现侧没有改动代理内核，仅在 UI / 设置 / 主题派生层增加：

- ACG 首页：在保留壁纸、侧栏、启动按钮与运行信息布局的基础上，补充一言刷新、当前节点测速、刷新状态反馈和一言配置说明文档入口，并继续随全局主题令牌做克制协调。

参考资料包括 Android / Material 官方文档与相关开源实现：

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

## 未来展望

当前阶段的重点已经从“迁移到 MD3”转向“把 MD3 体验打磨稳定”。后续会继续清理少量遗留 UI 边界，逐步减少对兼容层的直接依赖，让普通页面的组件、动效、间距、色彩和无障碍表现更接近完整的 Material Design 3 / Material You 体验。

在个性化方面，会继续完善动态取色与 ACG 内容联动：优化从用户选择壁纸中提取主题色的效果，补充更灵活的图片源与缓存策略，让首页、卡片、导航栏等界面元素能够在保证可读性的前提下随内容自然变化。同时也会持续改进配置编辑、运行状态展示、流量统计、轻量化构建与稳定性，让 YumeBox MD3 在可用、好看和好玩的方向上继续演进。

## 反馈与建议

如果遇到 Bug，请在 Issues 页面提交：
[Issues](https://github.com/Yizuka17/YumeBox-MaterialDesign/issues)

有想法或改进建议也可以在这里提出。

更多讨论与反馈可加入原项目群组：[@OOM_WG](https://t.me/OOM_Group)

## 参与贡献

如果您希望将 YumeBox MD3 变得更好，请参阅 [CONTRIBUTING](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/CONTRIBUTING.md)。如果希望将 YumeBox 翻译为更多语言，或改进现有翻译，请 Fork 本项目，并在 `locale/lang` 目录下创建或更新对应的翻译文件。

## 特别

本项目基于 [YumeBox](https://github.com/YumeRiMoe/YumeBox) 继续开发，~~作者对这个项目中的代码一无所知。代码处于可用或不可用状态，没有第三种情况。~~ 开发过程中采用 AI 辅助完成部分代码实现、文档整理与问题排查，感谢原项目及相关开源项目的贡献。

以及该项目中使用的 [第三方](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/docs/ThirdParty.md) 库。



