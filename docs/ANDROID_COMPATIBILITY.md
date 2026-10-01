# Android compatibility — bounded audit, 2026-10-01

Long-term scope is Android phones across brands. There is no manufacturer/model
allowlist or Pixel-only API in the gesture engine. The name is not certification.
The original offline edition remains offline; NextGen debug billing networking
is separate. No USB phone, device install or permission operation occurred here.

This bounded review candidate is isolated on `contest/nextgen-ui-candidate-20261001`,
with the compatibility increment and native UI refresh. The original offline
branch and frozen stable contest package remain unchanged. No publication is implied.

## Runtime and build matrix

| Environment | Evidence | Boundary |
|---|---|---|
| API36 ARM64 Google APIs emulator, Pixel8 AVD dimensions, portrait, gesture navigation | Stable candidate previously verified left inward -> Recents in Settings/Clock, ordinary touch checks, master OFF, reboot, service revocation and revoked reboot | Not a physical Pixel8; only one action/navigation/geometry. This candidate additionally passed same-emulator cold restart, service revocation, and revoked restart; phone/OEM coverage is still absent |
| Android11/API30 | Declared minimum; API use statically checked and geometry JVM tests/build | No API30 runtime session |
| API31–32 | VibratorManager guarded at31; fallback vibrator retained | Static/build only |
| API33–34 | Platform locale APIs guarded at33; restricted settings documented | Static/build only; no device certification |
| API35–36 | Existing target33 can run subject to system restrictions; new target requirements not claimed | No target35+ upgrade, edge-to-edge migration or store compliance claim |
| Physical Google Pixel | None | All models untested |
| Samsung/One UI; Xiaomi/HyperOS | None | ROM background/navigation/accessibility behavior needs device tests |
| OPPO/ColorOS, OnePlus, vivo and other Android ROMs | None | Do not infer support from the platform API review |
| Tablets/foldables, landscape, cutouts, multi-window/desktop | Geometry regression cases; static layout review | Actual window transitions and fold behavior untested |
| Non-Android HarmonyOS, iOS | Not supported by this Android APK | Not an Android compatibility target |

## API and system-input review

`minSdk30`, `compileSdk33`, `targetSdk33`, AGP8.6.1, Gradle8.10.2, JDK17 and Build
Tools34.0.0 are retained. WindowMetrics and display-cutout insets are API30;
side gesture exclusion is API29, below the minimum. Screenshot global action is
guarded at30, lock at28, VibratorManager at31 and LocaleManager at33. Optional
accelerometer and missing vibrator checks are already present. Camera and app
launch failures are caught; device security/work policy is not bypassed.

System Back owns outer side edges. The stable fix requests only a middle200dp
exclusion zone; Android controls whether/where it is honored. Native Back outside
that zone and mandatory bottom Home gestures cannot be claimed overridden.
Three-button navigation and OEM edge sensitivity require separate tests.

## Small fixes in this branch

- Overlay sizing now combines system bars with display-cutout safe insets,
  honors left/right safe edges, and clamps to available dimensions. Removed the
  forced minimum240dp side height that could exceed a short window. Unusable
  tiny windows receive no overlay rather than an out-of-bounds touch region.
- Unbind and destroy share idempotent cleanup: disable dispatch, remove pending
  keys/edge taps/long presses, unregister preferences/sensor and remove overlays.
  Reconnection begins from a clean state. Interrupted edge touch sessions are
  canceled; an orphan ACTION_UP cannot complete the old gesture.
- Text-bearing buttons, spinners and section headings use content height with
  minimum touch sizing, improving long labels/font scaling. Fixed icon-only
  switches remain48dp. This is not a completed accessibility/RTL/font audit.

Geometry regression tests cover portrait bars, landscape cutout plus navigation
insets, short windows and invalid/exhausted bounds. They do not emulate Android
WindowManager, framework lifecycle callbacks, touch dispatch or physical screens.

## OEM background limits

The system manages accessibility binding; this app does not keep itself alive
with a hidden foreground service, wake lock or auto-grant. Android power modes
can defer background resources. Some ROMs have per-app sleep/auto-start controls;
for example Samsung documents sleeping/deep-sleeping apps. Menus and behavior
vary by version. If gestures stop, first record whether the service is still
bound, master is ON, navigation mode and screen geometry. The user may choose a
per-app battery setting after understanding its tradeoff. Do not disable global
security, promise permanent background execution, or request blanket exemptions.

## Necessary tests before a multi-brand claim

On each explicitly authorized disposable emulator/physical device, record exact
model, ROM/build/API, display density, font scale, navigation and cutout position.
First leave only one edge action enabled. Verify input -> actual Recents in two
stock apps, then normal taps/scroll/short edge movement and native Back outside
the middle zone. Repeat in three-button navigation if provided, rotate both
ways and check the notch/bar margins, keyboard and a short multi-window viewport.

Enable/disable/re-enable service; interrupt an edge long press or volume press,
revoke during pending input, and confirm no delayed action afterward. Test master
OFF, a process/system reboot and recovery without silently restoring a revoked
grant. Check200% font/display scaling, long profile/app names, English/Chinese,
all controls and landscape scroll. Then perform a separate30–60minute background
and lock/unlock test under the user's normal battery setting; compare vendor
sleep settings only if separately approved. Restore the original test-device
settings and revoke the service at completion.

Priority coverage: one API30/33 AOSP emulator, one current Samsung device and one
Xiaomi/HyperOS device, then another ROM and foldable/rotation cases. This is a
proposed matrix, not availability or authorization to use the connected phone.
No physical tests were run in this audit; all special device permissions require
separate approval. Release only the support matrix actually observed.

References:
- https://developer.android.com/reference/android/view/WindowInsets.Type
- https://developer.android.com/reference/android/accessibilityservice/AccessibilityService
- https://developer.android.com/develop/ui/views/touch-and-input/gestures/gesturenav
- https://developer.android.com/training/monitoring-device-state/doze-standby
- https://www.samsung.com/us/support/answer/ANS10003442/

## Candidate runtime qualification

The reviewed debug package is `com.pixelgesture.nextgen`, with the original debug
signature and the approved private Test Store setup. 50 JVM tests, translation
verification, debug/release builds and Lint passed (0 errors,33 warnings).
Actual API36 emulator QA covered light/dark mode, large fonts and short display
UI, Settings/Clock inward-to-Recents, repeated input, five ordinary-input negative
cases,10-second background, cold restart and revocation. Large/narrow navigation
labels may ellipsize; full TalkBack/keyboard/RTL and long endurance remain pending.

An initial QA failure was independently reproduced with native Android Recents
while the app service was OFF after a background system-theme change. Returning
Home allowed launcher recreation to settle, after which native and candidate
Recents worked. The separate QA protocol now settles Home before opening a stock
app. No unexplained engine patch or added capability was used to claim success.
Pending-input interruption during revocation and physical/OEM cases remain
unverified. Do not extrapolate this emulator result to all Android devices.
