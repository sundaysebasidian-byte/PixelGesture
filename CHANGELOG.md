# Changelog

## Unreleased

- Pin the effective Build Tools 34.0.0 requirement in the app and CI instead of the ignored 33.0.2 declaration.

- Add local configuration export/restore with bounded version/field validation, confirmation and paused restoration.
- Ignore canceled, orphan and repeated volume events; clear pending timing on interruption and binding changes.
- Add backup/migration and hardware-timing regression tests plus a download-free JVM test runner.
- Clarify installation signatures, permission boundaries and backup privacy.

All notable changes are documented here. The project follows semantic versioning from 0.6.0 onward.

## [Unreleased]

### Planned

- Physical-device validation on Android 17
- Gesture sensitivity and timing profiles
- Import and export of gesture configuration

## [0.6.0] - 2026-08-20

### Changed

- Prepared a clean, reproducible GitHub repository under GPL-3.0-only
- Added English-first and Simplified Chinese project documentation
- Added CI for builds, Android Lint, unit tests, and translation parity
- Enabled R8 code shrinking and resource shrinking for release builds
- Moved installed-app discovery off the main thread
- Switched gesture timing to a monotonic clock
- Reduced shake detection from game-rate to UI-rate sensor sampling
- Added Gradle distribution checksum verification

### Preserved

- All 36 gesture triggers and 12 actions
- English-default, complete English/Simplified Chinese switching
- Safe default bindings and upgrade-compatible application ID
- No network permission and no accessibility window-content retrieval

## [0.5.0] - 2026-08-20

- Added complete manual English/Simplified Chinese switching with English default
- Added a master gesture-engine switch and safe reset
- Added lazy settings groups, app-list caching, differential overlay updates, and sensor lifecycle cleanup

## [0.4.0]

- Expanded the trigger catalog to 36 gestures

## [0.3.0]

- Added long-press, diagonal, and bottom horizontal gestures

## [0.2.0]

- Added app launching to every gesture

## [0.1.0]

- Initial prototype
