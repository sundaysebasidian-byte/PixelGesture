> **NextGen development branch:** separate package `com.pixelgesture.nextgen`; debug uses RevenueCat 9.9.0 with an approved Test Store key; official Test Store purchase flows have been verified on an isolated API 36 ARM64 emulator; physical-device gestures remain unverified. See [current status and run instructions](docs/NEXTGEN.md). This branch is not the original offline release.

# Pixel Gesture

[简体中文](README.zh-CN.md)

Pixel Gesture is an Android gesture project using platform accessibility APIs, with no manufacturer allowlist. It uses a narrow Android accessibility overlay to map edge, corner, shake, and volume-key gestures to system actions without replacing the launcher.

> Pixel Gesture is an independent project. It is not affiliated with, endorsed by, or sponsored by Google LLC. Google Pixel and Android are trademarks of Google LLC.

## Highlights

- 36 configurable gesture triggers across the left, right, bottom, top corners, accelerometer, and volume keys
- 12 actions: Back, Home, Recents, notifications, Quick Settings, screenshot, lock, power menu, camera, app launch, system settings, or no action
- Every gesture can launch any installed launcher app
- Complete English and Simplified Chinese interface; English is the default
- Master pause switch, safe reset, adjustable 12–40 dp edge width, and optional haptics
- Lazy-loaded advanced settings and asynchronously cached app discovery
- No Internet permission, analytics, ads, account, or screen-content retrieval
- Safe defaults enable only left/right inward swipes for Back
- Development source: local settings export/restore with validation and paused restoration (not in the published 0.6.0 APK)

## Requirements

- Android 11 or later (`minSdk 30`)
- A device that permits manually enabled accessibility services
- Long-term target: Android phones across brands; current runtime evidence is one API36 emulator, not multi-brand certification

The frozen candidate compiles/targets API33 and requires API30+. That does not prove newer Android/OEM compatibility or current store compliance. See [Compatibility matrix](docs/ANDROID_COMPATIBILITY.md) for tested scope, API guards and the required device checks.

## Install a release

1. Download the APK from the repository's **Releases** page.
2. Verify its SHA-256 checksum against the release notes.
3. Install or update the APK. Updates must use the same signing certificate.
4. Open Pixel Gesture and select **Open accessibility settings**.
5. Enable Pixel Gesture under installed accessibility services.

On Android 13 and later, a sideloaded app may show **Restricted settings**. Open the app details page, allow restricted settings from the top-right menu, then enable the service.

For signature mismatches, pause/recovery steps and permission boundaries, see [Install and recover](docs/INSTALL.md).

## Build

Prerequisites: JDK 17, Android SDK Platform 33 and Build Tools 34.0.0, the minimum supported by AGP 8.6.1. See [AGP compatibility](https://developer.android.com/build/releases/agp-8-6-0-release-notes). Configure `ANDROID_HOME` or an untracked `local.properties` with `sdk.dir=/absolute/path/to/sdk`. The wrapper pins Gradle 8.10.2; its distribution and AGP 8.6.1 dependencies must already be cached for an offline build.

```bash
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Debug APKs are written under `app/build/outputs/apk/debug/`. Release APKs built from this repository are unsigned; signing keys are intentionally never stored in the project.

## Privacy and accessibility

Pixel Gesture uses an accessibility service only to create narrow gesture zones, receive opted-in hardware-key events, and execute configured global actions. Its accessibility configuration sets `canRetrieveWindowContent=false`. The original offline edition has no Internet permission. The separate NextGen debug edition uses RevenueCat Test Store networking as disclosed in [NextGen](docs/NEXTGEN.md). See [Privacy](docs/PRIVACY.md) for the complete statement.

## Project documents

- [Changelog](CHANGELOG.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Testing](docs/TESTING.md)
- [Configuration backup](docs/BACKUP.md)
- [Release process](docs/RELEASING.md)
- [Commercial use](docs/COMMERCIAL_USE.md)
- [Contributing](CONTRIBUTING.md)
- [Security policy](SECURITY.md)

## License

Pixel Gesture is licensed under the GNU General Public License v3.0 only. Commercial use is allowed, but distributed modified versions must comply with GPLv3, including corresponding-source obligations. See [LICENSE](LICENSE).
