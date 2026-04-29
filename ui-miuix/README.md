# YumeBox Miuix compatibility library

This module is the dedicated legacy Miuix boundary for YumeBox UI code.

Goals:

- Keep direct `top.yukonga.miuix` dependencies out of normal Material 3 UI modules over time.
- Provide one project dependency for screens that still need Miuix, especially the ACG home page.
- Allow other screens to migrate directly to Material Design 3 without carrying their own Miuix artifacts.

Current phase:

- This module centralizes the Miuix artifacts and exposes them through `api` so existing modules can be moved gradually.
- Wrappers/adapters should be added here only for parts that are not worth rewriting immediately, such as blur, popup/window, and legacy preference components.
- Simple components such as text, icon, button, surface, and scaffold should usually be migrated directly to Material 3 instead of being wrapped here.
