# Release process

Release signing is intentionally separate from source control and CI pull-request builds.

1. Confirm `versionCode`, `versionName`, and `CHANGELOG.md`.
2. Run the complete verification suite from a clean checkout.
3. Build the release APK.
4. Sign locally with the protected production keystore, or use a protected GitHub environment whose secrets are unavailable to pull requests.
5. Verify APK alignment, signature, package name, version, permissions, and launchable activity.
6. Install over the previous production-signed release and confirm preferences remain intact.
7. Compute SHA-256 for the final signed APK.
8. Create an annotated tag such as `v0.6.0` from the exact release commit.
9. Create a GitHub Release and attach the signed APK plus checksum. Do not commit APKs to the repository.

The production keystore must be backed up securely outside the repository. Losing it prevents compatible direct updates; exposing it allows unauthorized builds to impersonate official releases.
