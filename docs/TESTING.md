# Testing

## Automated checks

Run the complete local verification suite:

```bash
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug assembleRelease
```

`verifyTranslations` requires identical resource keys in English and Simplified Chinese and rejects hard-coded Chinese text in Java. Unit tests verify catalog uniqueness, action coverage, safe defaults, and preference-key mapping. CI runs the same checks for pushes and pull requests.

## Physical-device matrix

Gesture and accessibility behavior cannot be fully validated by JVM tests. For any release, test at minimum:

- Service enable, disable, process restart, and master pause
- Gesture navigation and three-button navigation where available
- Portrait, landscape, lock/unlock, and configuration changes
- All enabled edge families, top corners, double/triple conflict, and long press
- Volume double press, long press, chord, and interruption cleanup
- Shake registration, trigger cooldown, and disabled-state battery behavior
- App picker loading, duplicate labels, app launch, and uninstalled-app fallback
- English default, switching both directions, process restart, and all expanded sections
- Upgrade over the previous signed release without losing bindings

Record device model, Android build, navigation mode, test result, and battery observations in release notes. Android 17 must be validated on a physical Pixel 8 before claiming full compatibility.
