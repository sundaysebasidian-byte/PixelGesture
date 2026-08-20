# Security Policy

## Supported versions

Security fixes are provided for the latest released version. Older builds should be upgraded before reporting behavior that may already be fixed.

## Reporting a vulnerability

Do not open a public issue for a vulnerability that could expose user data, enable unintended actions, bypass Android protections, or compromise signing and update integrity. Use GitHub's **Private vulnerability reporting** feature on the repository's Security page.

Include:

- Pixel Gesture version and APK source
- Device model, Android version, and navigation mode
- Required accessibility settings and gesture configuration
- Minimal reproduction steps
- Expected impact and any proof-of-concept material

Never include real signing keys, account credentials, or personal data. Acknowledgment and remediation timelines depend on severity and maintainer availability.

## Security baseline

- No Internet permission
- `canRetrieveWindowContent=false`
- Local-only preferences
- No embedded release signing key
- Release checksums published with each APK
