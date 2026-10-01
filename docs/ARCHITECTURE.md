# Architecture

Pixel Gesture intentionally has no network or third-party runtime dependencies.

## Components

- `MainActivity`: localized, programmatic settings UI; advanced sections are created only when expanded; installed-app discovery runs on a single background executor and is cached in memory.
- `GestureAccessibilityService`: owns accessibility overlays, hardware-key handling, shake registration, and action execution.
- `AppConfig`: defines stable preference keys, the 36-trigger catalog, the 12-action catalog, safe defaults, and preference migration.
- `ConfigBackup`: bounded, versioned properties-file codec and local preference restore; no runtime dependencies. Parsing precedes confirmation, and restoration pauses gestures.
- `VolumeGestureState`: JVM-testable key sequence timing, isolated from Android event delivery.
- `LanguageManager`: applies English or Simplified Chinese independently of the system default and synchronizes Android 13+ per-app locale state.
- Android resources: all user-visible text lives in default English and `zh-rCN` resource sets.

## Runtime principles

1. Only gesture families with at least one active binding receive an overlay.
2. Preference changes update only the affected overlay or sensor.
3. The accelerometer is registered only when shake detection is enabled.
4. Elapsed gesture time uses a monotonic clock.
5. New gestures are disabled by default.
6. The service never retrieves accessibility window content.

## Future refactoring boundary

Edge gesture classification, action dispatch, and preference storage should move into separate testable classes as their behavior grows. Keep Android framework adapters thin and avoid adding business logic directly to the Activity.
