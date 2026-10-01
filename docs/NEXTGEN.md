# PixelGesture NextGen Test Store development branch

Status: real SDK-linked debug APK builds successfully; official Test Store purchase flows are verified on an isolated API 36 ARM64 emulator. Physical-device gesture validation remains pending.
The original offline project remains separate. Application ID: `com.pixelgesture.nextgen`.
Release uses an unconfigured gateway and excludes RevenueCat and the test key.

## Local profiles and billing

The current gesture setup and manual backup remain free. Eight named local profile
slots support save, switch and delete. Save/switch require active `profiles` access;
switching validates the complete backup before changes and pauses gestures.
Existing profiles remain visible and deletable after access loss. Profile names,
bindings, chosen app packages and exported files are never sent to RevenueCat.

Debug pins core `com.revenuecat.purchases:purchases:9.9.0`, uses `Store.TEST_STORE`,
and configures a randomly generated custom synthetic customer ID retained across restarts.
SDK 9.9.0 infers Test Store from the test key: explicitly passing TEST_STORE to
Builder.store crashes its attribution factory. The gateway validates the inferred
runtime store. Debug also supplies kotlinx-coroutines-android 1.6.4, matching the
SDK core version, for its actual successful-purchase callback.
`RevenueCatGateway` loads the current offering's lifetime package only when its
product is `profiles_lifetime_test`. Access derives solely from active `profiles`
in real SDK CustomerInfo, additionally requiring sandbox and Test Store flags.
Product availability or transaction completion alone cannot unlock profiles.

Purchasing uses the SDK purchase dialog and callback, with cancellation/error
states. Restore calls the official SDK, then freshly fetches CustomerInfo. SDK
9.9.0 Test Store restore only rechecks this customer's state: it does not restore
real store receipts or recover a lost synthetic ID after uninstall/data clearing.
No local premium switch, fabricated receipt or production billing path exists.
Only JVM tests inject fake gateway responses. Activity destruction removes the
listener; stale and duplicate callbacks cannot unlock access.

## Test project

Use a dedicated RevenueCat Test Store project and synthetic purchase states.
No secret server key is needed. Create the entitlement `profiles`.
Product `profiles_lifetime_test` is a non-consumable with nominal USD 1.99 display
price, not a real charge. The wizard also left an unused USD 99.99 product; it
must not be offered by the app. The default offering has one lifetime package
mapped to this product. Project/account identifiers are kept in private review
evidence. Email confirmation was pending at the last dashboard observation;
arrival of a confirmation email alone does not prove the account is verified.

## Build and run

Use existing JDK 17, Gradle 8.10.2, AGP 8.6.1, Android platform 33 and Build Tools
34.0.0. The actual SDK-linked build works without API 35 or stack upgrades.
Enable AndroidX as configured in this independent branch. Set SDK location in
ignored `local.properties` or `ANDROID_HOME`.

Create ignored root `rc-test.properties` with `sdkKey=` followed by your public
Test Store key. Only the `test_` key format is accepted. Keep its value out of
chat, screenshots, patches and public source. It is embedded only into debug APK;
public SDK keys are inspectable in APKs. Without it debug stays unconfigured.

```sh
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug assembleRelease \
  -Pandroid.builder.sdkDownload=false
```

Debug output: `app/build/outputs/apk/debug/app-debug.apk`. Release output is
unsigned, offline and not a RevenueCat contest release. Never distribute a
Test Store debug build as production. See [separate privacy](PRIVACY.md).
`scripts/test-jvm.sh` is a dependency-free runner using supplied Android/JUnit jars.

The Mac already has official Emulator 37.1.11 and an API 36 Google APIs ARM64
system image in the Homebrew SDK. This was missed by the initial limited path
check. No new emulator/image installation was needed. Hardware acceleration
passed; a task-local headless AVD was created with isolated ADB port 5039.
Android Studio and the default AVD directory were absent. Existing Android CLI
and older project documents confirm the earlier Android simulation workflow.
No APK was installed on the user's phone; no phone or emulator accessibility
permission was enabled.

## Device acceptance

On the task-owned API 36 ARM64 emulator, using synthetic customers/settings only:
1. Fresh customer: load offering and verify USD 1.99 Test Store label, locked slots.
2. SDK dialog Cancel and Error: no unlock, clear status, retry possible.
3. SDK Success: real sandbox CustomerInfo grants profiles; save two named setups.
4. Switch a profile: bindings restored atomically; master gestures paused.
5. Restart: same synthetic customer; fresh CustomerInfo rechecks access.
6. Restore: same customer's current entitlement returns; no claim of real store recovery.
7. Connection failure/revocation: controller unit tests pass; runtime revocation/network-failure acceptance remains pending.
8. Actual network capture and physical gesture/accessibility behavior remain
   pending. No accessibility permission was enabled, including on the test AVD.

Cancel/Error/Success, two local profiles, paused switching, same-customer restore,
app cold restart and AVD restart passed on the fixed APK. Two earlier SDK crashes
were reproduced before fixes: explicit Builder.store and missing Main dispatcher.
The final inspected crash buffer was clean. A host emulator process exited once
without a recorded cause and was restarted; this is not performance/stability
benchmark evidence.

For repeatable official dialog checks, use a fresh synthetic customer on the named
PixelGesture_NextGen_QA AVD, English UI and 1080x2400 Pixel 8 configuration.
From the repo root run scripts/nextgen_runtime_ui.py profiles, then
scripts/test-nextgen-store.py using Python 3. Set ADB_BIN, NEXTGEN_ADB_PORT
(default5039), NEXTGEN_TEST_SERIAL(defaultemulator-5582) as needed. The helper
refuses phones and any differently named AVD. A purchased customer is deliberately
not reset by the runner; use a new dedicated fixture. It does not fabricate SDK
callbacks, receipts, entitlements or a local premium flag.

## Contest and public deliverables

Organizer Manager Perttu Lähteenlahti confirmed that Test Store is sufficient for
Next Gen in the linked official forum. Registration was previously observed in
the signed-in Devpost tab. No final project submission or publication was done.
An actual 109.9-second Android emulator recording and required native-resolution
screenshots/icon are prepared for review. English cards identify Test Store,
synthetic profiles and the absence of accessibility/physical-device gesture
validation. Public source/video publication, academic verification and final
form review remain pending. No graphics replace Android runtime footage.
Disclose that this extends the existing GPL-3.0-only-licensed offline PixelGesture app.
Do not mark a store-release checkbox falsely if the form still presents one;
seek organizer clarification instead.

Absolute deadline: **2026-10-01 06:45 UTC / Shanghai 14:45**.

Sources checked 2026-09-30:
- https://revenuecat-shipaton-2026.devpost.com/forum_topics/44695-next-gen-eligibility-is-a-test-store-only-purchase-sufficient
- https://revenuecat-shipaton-2026.devpost.com/rules
- https://www.revenuecat.com/docs/test-and-launch/sandbox/test-store
- https://github.com/RevenueCat/purchases-android/tree/9.9.0
