# Testing

## Automated checks

Run the complete local verification suite:

```bash
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug assembleRelease
```

`verifyTranslations` requires identical resource keys in English and Simplified Chinese and rejects hard-coded Chinese text in Java. Unit tests verify catalogs, safe defaults, preference mapping, legacy/current migration behavior, validated backup round trips, stale app-target removal, paused restores, malformed/oversized files, and volume double/long/chord timing and cancellation. CI runs the same checks for pushes and pull requests.

## Fixed-toolchain setup

AGP 8.6.1 requires Build Tools 34.0.0. The project and CI explicitly pin this version; the older 33.0.2 declaration was ignored by AGP. Installing only 33.0.2 cannot run the Gradle app tasks, and an offline environment must already contain 34.0.0. To diagnose without automatically downloading SDK packages, add `-Pandroid.builder.sdkDownload=false`. This flag does not disable ordinary Maven dependency downloads; use `--offline` for that. See [the official compatibility table](https://developer.android.com/build/releases/agp-8-6-0-release-notes).

## JVM tests with existing tools only

When the pinned Gradle distribution or Android build packages are unavailable, run the domain tests directly with existing JDK 17, Android platform stubs and the existing JUnit/Hamcrest jars:

```bash
ANDROID_JAR=/path/to/sdk/platforms/android-33/android.jar \
JUNIT_JAR=/path/to/junit-4.13.2.jar \
HAMCREST_JAR=/path/to/hamcrest-core-1.3.jar \
./scripts/test-jvm.sh
```

The script downloads nothing and compiles with Java 8 language/API compatibility. A newer existing Android stub jar can also run these domain tests; that does **not** validate the pinned API 33 app build. The in-memory preferences double does not simulate Android listeners or disk persistence. This runner does not replace Android Lint, APK assembly, R8 or device tests.

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
- Local file export/restore for all bindings, both languages and edge-width limits
- Cancel document picker and restore confirmation without preference changes
- Invalid/oversized/unsupported backups, unavailable storage and provider permission failures
- Restore while the service is active: overlays/sensor/keys pause, missing app targets give feedback, resume only on manual master-switch action
- Rotation/activity recreation during picker and I/O; exported file survives app uninstall
- Canceled volume events, orphan releases, binding changes mid-press and normal volume behavior when each mode is disabled

Record device model, Android build, navigation mode, test result, and battery observations in release notes. No Android version or phone brand is fully certified by the current emulator evidence. See [Android compatibility matrix](ANDROID_COMPATIBILITY.md); device testing requires explicit authorization.
