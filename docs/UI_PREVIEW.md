# Native UI preview

This isolated review candidate is `contest/nextgen-ui-candidate-20261001`.
Debug installs as `com.pixelgesture.nextgen` with the original NextGen debug
signature in the owned QA environment. It updates that test edition; the original
offline app and frozen contest baseline are separate. No publication is implied.

Use existing JDK17, Gradle8.10.2, AGP8.6.1, SDK33 and Build Tools34.0.0. Supply
your local `sdk.dir` through an untracked `local.properties`. This source archive
does not include machine paths, credentials or a RevenueCat configuration file.

```sh
gradle --offline verifyTranslations testDebugUnitTest lintDebug assembleDebug
```

Install only in an explicitly authorized disposable emulator; if using the
existing task QA environment, target its server and serial explicitly:

```sh
adb -P 5039 -s emulator-5582 install -r -t app/build/outputs/apk/debug/app-debug.apk
adb -P 5039 -s emulator-5582 shell am start -n com.pixelgesture.nextgen/com.pixelgesture.MainActivity
```

Do not enable accessibility for UI inspection. Home, Gestures/editor and Profiles
work without that grant. Home contains a real, interactive **App-only practice**
view: swipe inward from the illustrated phone's left edge to show an abstract
Recent apps example. It does not dispatch a system action or read a screen.
Short, reverse and cancelled inputs are rejected. Show preview is an equivalent
native button with a distinct result label; Try again resets the illustration.
This example does not change or represent the current configured edge binding.
Actual service status and the persistent master switch remain separate controls.
Settings & permissions expands the real permission information and language
picker. Opening this panel does not grant accessibility.

The illustration uses native Canvas and one 300ms ValueAnimator after successful
input or an explicit preview. It has no idle animation; system disabled animator
scale skips the transition. It reuses drawing objects per frame. Text wraps and
content scrolls at large font sizes. Practice result and settings expansion
survive navigation and theme recreation, while transient trails are cleared.
Practice completion is intentionally not stored as a permanent preference.
Native buttons and a live result label provide an alternative to the decorative
touch illustration; full TalkBack/keyboard testing remains pending.

The private reviewed debug APK reuses the approved RevenueCat Test Store key.
The source archive contains no key or signing material. For independent builds,
follow `NEXTGEN.md` and supply your own public Test Store key locally; without
configuration, purchase and save remain disabled. No local Premium override
exists. Official SDK9.9.0 Test Store QA exercised cancellation, failure, verified
success, profile save/switch, restore and cold restart with a synthetic user.
This is simulated purchase behavior, not a real store release or a real charge.

The app follows system light/dark mode. Light retains the reviewed green palette;
dark retains the original color resources. The actual review film uses only
this candidate's UI, SDK and gesture recordings, plus labelled CG presentation.
Use Home's Settings & permissions language control for English/Chinese.
Gestures/editor preserves selection across
theme recreation and saves bindings through the existing local preferences.

The source includes the prior isolated compatibility changes. Their patch
applied without conflicts, but the service changes still need device regression.
Keep this review candidate separate from the stable contest baseline until accepted.
