> **NextGen development branch:** separate package `com.pixelgesture.nextgen`; debug uses RevenueCat 9.9.0 with an approved Test Store key; official Test Store purchase flows have been verified on an isolated API 36 ARM64 emulator; physical-device gestures remain unverified. See [current status and run instructions](docs/NEXTGEN.md). This branch is not the original offline release.

# Pixel Gesture

[English](README.md)

Pixel Gesture 是基于 Android 平台接口的手势项目，目标覆盖多个品牌的 Android 手机。原版为纯本地应用；本 NextGen 开发分支的测试购买流程使用独立联网配置。它通过 Android 无障碍覆盖层建立窄热区，将边缘、角落、摇一摇及音量键手势映射为系统动作，无需更换启动器。当前尚未完成多品牌真机兼容验证。

> Pixel Gesture 是独立项目，与 Google LLC 不存在隶属、赞助或官方认可关系。Google Pixel 与 Android 是 Google LLC 的商标。

## 主要功能

- 左右边缘、底部、顶部两角、摇一摇及音量键共 36 种可配置触发方式
- 12 种动作：返回、主页、最近任务、通知栏、快捷设置、截图、锁屏、电源菜单、相机、打开应用、系统设置或不执行
- 每种手势均可启动任意已安装桌面应用
- 英语/简体中文完整切换，默认英语
- 手势总开关、安全重置、12–40 dp 热区宽度与可选震动反馈
- 进阶设置延迟创建，应用列表在后台加载并缓存
- 无网络权限、无统计、无广告、无账号，也不读取屏幕内容
- 安全默认仅启用左右向内滑返回
- 开发源码新增本地配置导出/恢复，严格校验并暂停恢复后的手势（官方 0.6.0 APK 尚不包含）

## 系统要求

- Android 11 或更高版本（`minSdk 30`）
- 系统允许用户手动启用无障碍服务
- 长期目标是跨品牌 Android 手机；目前运行证据仅限一个 API36 模拟器，不能当作多品牌认证

当前工程为稳定兼容性使用 API 33 编译并以 API 33 为目标。Android 17 的最终兼容性必须在真机上验证，因为预览版和不同系统实现可能存在差异。

## 安装 Release

1. 从仓库的 **Releases** 页面下载 APK。
2. 对照 Release 说明验证 APK 的 SHA-256。
3. 安装或覆盖更新；覆盖更新必须使用相同签名证书。
4. 打开 Pixel Gesture，点击“打开无障碍设置”。
5. 在已安装的无障碍服务中启用 Pixel Gesture。

Android 13 及以上可能限制侧载应用启用无障碍服务。若显示“受限设置”，请进入应用详情，从右上角菜单允许受限设置，再返回启用服务。

签名不匹配、暂停与恢复操作及授权边界见[安装与恢复](docs/INSTALL.md)。

## 构建

需要 JDK 17、Android SDK Platform 33 和 Build Tools 34.0.0（AGP 8.6.1 的最低要求），见[官方兼容表](https://developer.android.com/build/releases/agp-8-6-0-release-notes)。通过 `ANDROID_HOME` 或不提交的 `local.properties` 配置 `sdk.dir=/absolute/path/to/sdk`。Wrapper 固定 Gradle 8.10.2；离线构建还需预先缓存对应发行包及 AGP 8.6.1 依赖。

```bash
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Debug APK 位于 `app/build/outputs/apk/debug/`。仓库生成的 Release APK 默认不签名；任何签名密钥都不应进入项目。

## 隐私与无障碍说明

Pixel Gesture 的无障碍服务仅用于建立窄手势热区、接收用户主动启用的硬件按键事件并执行所配置的系统动作。配置明确设为 `canRetrieveWindowContent=false`。原离线版没有网络权限；独立 NextGen 调试版使用 RevenueCat Test Store 网络，详见 [NextGen](docs/NEXTGEN.md)。完整说明见[隐私声明](docs/PRIVACY.md)。

## 项目文档

- [更新日志](CHANGELOG.md)
- [架构说明](docs/ARCHITECTURE.md)
- [测试指南](docs/TESTING.md)
- [配置备份](docs/BACKUP.md)
- [发布流程](docs/RELEASING.md)
- [商业使用](docs/COMMERCIAL_USE.md)
- [贡献指南](CONTRIBUTING.md)
- [安全政策](SECURITY.md)

## 许可证

Pixel Gesture 使用 GNU GPLv3-only 许可证。许可证允许商业使用，但公开分发修改版本时必须遵守 GPLv3，包括提供对应源代码。详见 [LICENSE](LICENSE)。

长期兼容方向与当前验证边界见 [Android 兼容矩阵](docs/ANDROID_COMPATIBILITY.md)。名称不会限制支持品牌，但品牌、系统版本和导航方式必须逐项验证。
