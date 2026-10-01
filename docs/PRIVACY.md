# NextGen development privacy / 独立版隐私

This document applies to `com.pixelgesture.nextgen`. The original offline
`com.pixelgesture` app remains offline with its own unchanged privacy policy.

## Test Store debug build

The approved debug build uses RevenueCat 9.9.0 Test Store. It sends a random
synthetic customer ID and simulated product/purchase/customer-entitlement state
to RevenueCat to load offerings, purchase and recheck access. No real payments
or store accounts are used. The nominal USD 1.99 price is a test display price.
The public SDK key is included in debug APK; it is not a secret server key.

SDK 9.9.0 request headers automatically include Android API version, device model
and brand, locale/preferred locales, SDK/Kotlin/platform versions, app package
and version, debug and foreground/background indicators, plus request metadata.
As with any HTTPS service, the receiving service can observe the network IP.
This is not a claim of zero data collection. Server-side retention/deletion is
controlled by RevenueCat; the app does not promise instant server deletion on
uninstall. Android runtime network capture remains pending.

Source audit identifies the primary endpoint `api.revenuecat.com` and SDK
fallback `api-production.8-lives-cat.io`. Diagnostics and paywall-event hosts
exist in the SDK source but this app disables diagnostics and does not use
RevenueCat paywall UI. Do not claim an enforced network host allowlist.

Automatic device identifier collection and diagnostics are explicitly disabled.
The app does not set email, phone, advertising ID, custom subscriber attributes,
location, attribution, analytics or ad integrations. SDK 9.9.0 internally
constructs a Block Store helper, but the custom synthetic customer ID is not an
SDK anonymous ID: its store/alias methods return before Block Store data reads
or writes. The app does not call its optional Block Store API. The merged
debug manifest contains INTERNET, ACCESS_NETWORK_STATE and VIBRATE only; real
store BILLING, AD_ID and location permissions are removed. These compile-time
facts do not replace runtime verification of transitive SDK behavior.

Gesture settings, selected local application package names, profile names,
profile contents, sensor samples and private files are not supplied to the SDK.
Profiles and the synthetic ID reside in separate private preferences. Manual
backup uses a configuration allowlist and never exports the synthetic ID, key,
purchase state or Android permissions. Backups are readable plaintext and can
contain chosen app package names. Choose on-device storage for offline export.

The accessibility service declares `canRetrieveWindowContent=false`. It does
not inspect app text, controls, notifications, passwords or screen content.
It creates narrow gesture overlays, optionally receives volume-key events, and
requests configured system actions. Shake samples are used only while enabled,
and are not stored. Switching profiles pauses gestures and never grants access.

Uninstall/data clearing removes this app's local data, including the synthetic
customer ID, but not manual exports or RevenueCat's server records. Test Store
restore rechecks the same customer; it cannot recover a deleted synthetic ID.
`allowBackup=false` requests exclusion from system cloud backup; Android 12+
manufacturer device transfers may differ. No phone permission has been enabled
by this development work.

## Release variant

Release excludes the SDK, SDK application class and key, uses an unconfigured
billing gateway, and has no Internet permission. It cannot buy or unlock profiles.
It is not a production monetized or contest-ready release. Original offline
functionality remains available separately.

## 简体中文

本说明适用于独立包 `com.pixelgesture.nextgen`。获准的 debug 版仅通过
RevenueCat Test Store 发送随机合成用户 ID、测试商品和模拟购买/权益状态；
USD 1.99 是展示价，不会真实扣款。SDK 请求还自动包含系统版本、设备型号、
品牌、语言、应用/SDK 版本、前后台状态等元数据，服务端可见网络 IP。
不能宣称零采集；服务端记录的保存与删除由 RevenueCat 管理。

已关闭自动设备标识采集与诊断；不传邮箱、手机号、广告标识、定位、文件、
配置档或传感器历史。debug 合并清单仅保留网络、网络状态和振动权限。
配置档只在本地，备份白名单不含测试用户 ID、密钥、购买状态或权限。
恢复测试购买只核验同一合成用户的权益，卸载丢失 ID 后不等同真实商店恢复。
无障碍服务不读取窗口内容；切换配置会暂停手势。真机和运行时网络仍未验收。
release 不含 SDK 或 key，也不能解锁配置档；原离线版保持独立。

Sources: official RevenueCat 9.9.0 source `common/HTTPClient.kt`,
`common/AppConfig.kt`, `PurchasesOrchestrator.kt`, and
https://www.revenuecat.com/docs/customers/customer-attributes .
Android backup: https://developer.android.com/identity/data/autobackup#enable_and_disable_backup .
