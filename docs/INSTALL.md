# Install and recover

Use the signed APK and checksum from this repository's [Releases](https://github.com/sundaysebasidian-byte/PixelGesture/releases). Source builds may include unreleased changes. Verify the downloaded artifact, for example on macOS:

```bash
shasum -a 256 Pixel-Gesture-0.6.0.apk
```

Compare with the checksum for that exact release, not a different version. Allow installation for the chosen installer only if you trust the artifact. Installation and accessibility authorization are separate steps, both controlled by the user. Open Pixel Gesture, read the accessibility explanation, then use its button to open system accessibility settings and manually select the service.

If Android 13+ blocks the service with restricted settings, follow [Android's restricted-settings instructions](https://support.google.com/android/answer/12623953) only after verifying the app source. Device or work-profile policy may prevent access. Pixel Gesture cannot bypass it. The app does not require a separate “display over other apps” permission, camera permission or broad storage permission for its current implementation.

Start with the default left/right inward Back gestures. Enable extra families individually. Volume double/long bindings intercept their configured volume key, so normal volume adjustment for that key may be affected; chord-only configuration leaves individual volume events available to Android. Overlays can compete with system navigation and other accessibility services.

If navigation is inconvenient, turn off the app's master switch to pause overlays, shake and volume actions. For a complete stop, manually disable its service in system accessibility settings. Resetting defaults preserves the language but resets bindings and app targets and turns the master switch on; use it deliberately.

An update requires the same package and signing certificate. A locally built debug APK is signed with a debug certificate; release builds here are unsigned. Neither is automatically compatible with the official signed installation. If Android rejects an update for a signature mismatch, do not uninstall your current copy to force it: uninstalling deletes local settings. Official 0.6.0 has no manual export; use a separate test device for development builds until a compatible signed upgrade is available.

## 简体中文

从本仓库 Releases 下载签名 APK，核对该版本的 SHA-256。安装授权和无障碍授权是两个独立步骤，均由用户手动完成。Android 13+ 如提示受限设置，确认来源后参照上方官方说明；设备管理策略限制无法由应用绕过。当前实现不需要另授悬浮窗、相机或广泛存储权限。

先使用默认左右内滑返回，再逐项开启其他触发方式。音量双击/长按会拦截对应按键，可能影响普通音量调节；仅开启组合键时保留单键事件给系统。暂停总开关可暂停所有手势；彻底停止请手动关闭系统无障碍服务。安全重置保留语言，但重置绑定及应用目标并开启总开关。

覆盖更新必须同包名、同签名。本地 debug 与官方签名不同，源码 release 默认未签名。签名不匹配时不要通过卸载强行更新：卸载会清除配置，官方 0.6.0 尚无手动导出功能。开发版本宜使用独立测试设备。

### App appearance (NextGen0.6.2)

Home → Settings & permissions → Appearance offers Follow system, Light and Dark. Selection is local and survives app restart and in-place APK updates. It changes only this app, not Android system theme or gesture bindings. The default follows system.
