# Contributing to Pixel Gesture

Thank you for helping improve Pixel Gesture. Small, focused pull requests with clear test evidence are preferred.

## Before opening a pull request

1. Search existing issues and pull requests.
2. Open an issue before making a large UI, architecture, permission, or gesture-behavior change.
3. Preserve the privacy baseline: no Internet permission, analytics, advertising SDK, account system, or accessibility window-content retrieval without an explicit project decision.
4. Keep English as the default resource language and update Simplified Chinese in the same pull request.
5. Keep all signing material and release binaries out of Git.

## Development workflow

```bash
git switch -c feature/short-description
./gradlew verifyTranslations testDebugUnitTest lintDebug assembleDebug
```

Test gesture changes on a physical device. Include the Android version, device, navigation mode, and exact gestures tested in the pull request.

## Code expectations

- Use Java 8-compatible language features unless the project configuration changes first.
- Prefer isolated, testable gesture and action logic over additional code in `MainActivity` or the accessibility service.
- Use `SystemClock.uptimeMillis()` for elapsed-time decisions.
- Avoid work on the main thread that queries packages, reads large data, or performs blocking I/O.
- Put every user-visible string in both `values/strings.xml` and `values-zh-rCN/strings.xml`.
- New gestures must default to disabled and must not silently replace existing bindings.

## Pull request checklist

- [ ] The change has a focused purpose and no unrelated formatting churn.
- [ ] English and Simplified Chinese resources are complete.
- [ ] Unit tests cover pure logic where practical.
- [ ] Android Lint and all tests pass.
- [ ] Physical-device behavior is documented for gesture or overlay changes.
- [ ] No secrets, signing files, APKs, or build output are included.
- [ ] User-facing changes are added to `CHANGELOG.md`.

## Licensing of contributions

By submitting a contribution, you agree that it is your original work or that you have the right to submit it, and that it may be distributed under the repository's GPL-3.0-only license. Contributors retain copyright in their contributions.
