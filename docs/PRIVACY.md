# Privacy / 隐私声明

## English

Pixel Gesture 0.6.0 is a local-only Android application.

- It does not request the Internet permission.
- It does not contain analytics, advertising, telemetry, accounts, or cloud synchronization.
- It does not collect, store, sell, or transmit personal data.
- Gesture bindings and selected app package names are stored only in Android local preferences.
- Its accessibility service creates narrow gesture overlays, optionally receives volume-key events, and requests configured system actions.
- The accessibility service declares `canRetrieveWindowContent=false`; it does not inspect app text, controls, notifications, passwords, or screen content.
- Shake detection uses the device accelerometer only while the master switch and shake gesture are enabled. Sensor samples are not stored.
- The app-launch picker queries launcher activities only to display locally installed apps. The list is cached in memory for the current settings session and is not transmitted.

Uninstalling the app removes its local preferences. Android system logs and backups are controlled by the operating system; the app declares `allowBackup=false`.

## 简体中文

Pixel Gesture 0.6.0 是纯本地 Android 应用。

- 不申请网络权限。
- 不包含统计、广告、遥测、账号或云同步功能。
- 不收集、保存、出售或传输个人数据。
- 手势绑定和所选应用包名仅保存在 Android 本地偏好设置中。
- 无障碍服务只建立窄手势热区、按用户选择接收音量键事件，并执行已配置的系统动作。
- 无障碍配置明确声明 `canRetrieveWindowContent=false`，不会检查应用文字、控件、通知、密码或屏幕内容。
- 仅在总开关与摇一摇手势均启用时读取加速度计；传感器数据不会保存。
- 应用选择器只查询本机可启动应用并显示结果，列表仅在当前设置页面会话中保存在内存，不会传输。

卸载应用会删除其本地偏好设置。Android 系统日志与备份由操作系统控制；应用已声明 `allowBackup=false`。
