# Pixel Gesture

[简体中文](README.zh-CN.md)

Pixel Gesture is a local-only gesture layer for Google Pixel phones. It uses a narrow Android accessibility overlay to map edge, corner, shake, and volume-key gestures to system actions without replacing the launcher.

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

## Requirements

- Android 11 or later (`minSdk 30`)
- A device that permits manually enabled accessibility services
- Designed for Pixel phones; behavior on other Android devices is not guaranteed

The project currently compiles and targets API 33 for stable backward compatibility. Android 17 compatibility must be verified on a physical device because preview and OEM behavior can differ.

## Install a release

1. Download the APK from the repository's **Releases** page.
2. Verify its SHA-256 checksum against the release notes.
3. Install or update the APK. Updates must use the same signing certificate.
4. Open Pixel Gesture and select **Open accessibility settings**.
5. Enable Pixel Gesture under installed accessibility services.

On Android 13 and later, a sideloaded app may show **Restricted settings**. Open the app details page, allow restricted settings from the top-right menu, then enable the service.

## Build

Prerequisites: JDK 17 and Android SDK Platform 33 with Build Tools 33.0.2.

```bash
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Debug APKs are written under `app/build/outputs/apk/debug/`. Release APKs built from this repository are unsigned; signing keys are intentionally never stored in the project.

## Privacy and accessibility

Pixel Gesture uses an accessibility service only to create narrow gesture zones, receive opted-in hardware-key events, and execute configured global actions. Its accessibility configuration sets `canRetrieveWindowContent=false`. The app has no Internet permission and does not collect or transmit user data. See [Privacy](docs/PRIVACY.md) for the complete statement.

## Project documents

- [Changelog](CHANGELOG.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Testing](docs/TESTING.md)
- [Release process](docs/RELEASING.md)
- [Commercial use](docs/COMMERCIAL_USE.md)
- [Contributing](CONTRIBUTING.md)
- [Security policy](SECURITY.md)

## License

Pixel Gesture is licensed under the GNU General Public License v3.0 only. Commercial use is allowed, but distributed modified versions must comply with GPLv3, including corresponding-source obligations. See [LICENSE](LICENSE).
