<div align="center">

**简体中文** | [English](README.md)

<img src="logo.webp" style="width: 96px;" alt="logo">

## YumeBox-MaterialDesign

[![Latest release](https://img.shields.io/github/v/release/Yizuka17/YumeBox-MaterialDesign?label=Release&logo=github)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases/latest)
[![GitHub License](https://img.shields.io/github/license/Yizuka17/YumeBox-MaterialDesign?logo=gnu)](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/LICENSE)
[![Downloads](https://img.shields.io/github/downloads/Yizuka17/YumeBox-MaterialDesign/total?logo=github)](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

**基于 [YumeBox](https://github.com/YumeRiMoe/YumeBox) 的 Material Design 分支，一个基于 [mihomo](https://github.com/MetaCubeX/mihomo) 内核的开源 Android 客户端。**

</div>

## 使用

YumeBox-MaterialDesign 目前仅支持 **Android 8.0（API 26）及以上系统**。

请前往 Release 页面下载对应架构的安装包：[Release](https://github.com/Yizuka17/YumeBox-MaterialDesign/releases)

更多内容可访问官网：[YumeBox](https://yumebox.oom-wg.dev/)

覆写配置语法参考：[override 文档](https://yumebox.oom-wg.dev/override)。如果这个项目对你有帮助，请点下 Star，这是持续更新的动力。

## 相较原项目的主要改进

YumeBox-MaterialDesign 在保留原项目核心能力的基础上，围绕界面体验、配置管理、运行方式和稳定性进行了持续改进：

- **Material Design / Material You 体验**：重构并优化多个页面，加入动态取色、深色主题适配、导航栏与 Topbar 模糊效果、更顺滑的页面跳转和列表排序动画。
- **更强的覆写与配置能力**：引入全新的覆写系统，支持多配置切换、叠加应用、可视化编辑、预设分流模板、运行时配置预览，以及 `start`、`end`、`merge`、`force` 等后缀修饰符；同时加入基于 Rust 的覆写解析与配置编辑器语法检查/部分补全。
- **代理运行与节点管理增强**：支持 Root Tun（无需 VPN，但需要 Root 权限）、单节点与代理组测速、节点排序动画、节点标签与图标/旗帜显示、节点选择持久化，并优化代理启动流程和部分 bridge 实现。
- **订阅、导入与外部控制优化**：支持修改订阅链接、链接预览、配置排序、外部链接在 App 内打开并快速导入，优化外部控制器、Web 面板和通知栏快捷节点切换体验。
- **统计、隐私与轻量化**：新增连接页面和更完善的流量统计（包含 App 流量统计，需要开启查找进程），支持 YumeBox Lite，加入语言切换与隐私设置，并移除 Sentry 跟踪器和 EMAS 推送更新。
- **稳定性与性能优化**：优化启动阻塞、运行时状态卡死、弹窗卡顿、通知栏内容、打包体积、后台释放 UI 与多处交互细节，提升日常使用的流畅度与容错能力。

## 反馈与建议

如果遇到 Bug，请在 Issues 页面提交：
[Issues](https://github.com/Yizuka17/YumeBox-MaterialDesign/issues)

有想法或改进建议也可以在这里提出。

更多讨论与反馈可加入原项目群组：[@OOM_WG](https://t.me/OOM_Group)

## 参与贡献

如果您希望将 YumeBox-MaterialDesign 变得更好，请参阅 [CONTRIBUTING](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/CONTRIBUTING.md)。如果希望将 YumeBox 翻译为更多语言，或改进现有翻译，请 Fork 本项目，并在 `locale/lang` 目录下创建或更新对应的翻译文件。

## 特别

本项目基于 [YumeBox](https://github.com/YumeRiMoe/YumeBox) 继续开发，~~作者对这个项目中的代码一无所知。代码处于可用或不可用状态，没有第三种情况。~~ 开发过程中采用 AI 辅助完成部分代码实现、文档整理与问题排查，感谢原项目及相关开源项目的贡献。

以及该项目中使用的 [第三方](https://github.com/Yizuka17/YumeBox-MaterialDesign/blob/Yume/docs/ThirdParty.md) 库。



