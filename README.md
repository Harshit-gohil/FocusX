# FocusX 0.4 – Android native source project

Package: `com.focusx.app`  
Stack: Kotlin, Jetpack Compose, Room, Android AccessibilityService, foreground timer service.  
Minimum Android: 14 (API 34). Compile SDK: 36. Target SDK: 35.

## Implemented in this source snapshot
- Compose dashboard and navigation
- Ten feature entry points
- Room persistence for focus sessions, blocked website entries and schedule drafts
- Foreground timer notification service (user-started focus timer)
- Focus session history and basic daily analytics
- Installed launchable app enumeration and local block-rule persistence
- AccessibilityService that checks foreground package names (does not inspect screen text)
- Block screen and Accessibility settings shortcut
- Whole-app fallback toggles for Instagram, YouTube, Facebook and Snapchat
- Offline focus coach tips
- Local website blocklist configuration
- Local schedule draft storage
- Daily goal control UI

## Not yet implemented / not to be represented as complete
- DNS/VPN network filtering: website entries are stored only; they are not enforced.
- Fine-grained Reels/Shorts detection inside third-party apps: not implemented; whole-app blocking is the fallback.
- Recurring schedule alarm delivery and reboot recovery: not implemented.
- Ambient audio playback: not implemented; no audio assets are bundled.
- Multiplayer: requires backend, accounts and network; not in offline build.
- AI API: only deterministic offline tips are included.
- Full Android usage analytics: not implemented; analytics currently cover FocusX timer sessions.
- PIN/biometric lock, widgets, backup/export and strict-mode bypass resistance: not implemented.

## Build requirements
Open this folder in Android Studio with JDK 17, Android SDK Platform 36 and a compatible Gradle 8.13+ installation. Sync and run the `app` configuration on an Android 14+ phone.

The source has not been compiled in this environment. Treat the first Android Studio sync/build as a required validation step. If it fails, share the complete first error for correction.

## App blocking setup
1. Install FocusX.
2. Open FocusX > App Blocking.
3. Select a non-critical test app.
4. Tap Enable Accessibility service.
5. In Android Settings > Accessibility, enable FocusX.
6. Test app switching. Android OEM restrictions and background launch policy may affect reliability.
7. The user can always disable Accessibility access. This is not tamper-proof.

## Privacy and distribution notes
- The app has no INTERNET permission and no cloud connection.
- `QUERY_ALL_PACKAGES` is declared for this sideload-oriented build so the app can enumerate installed launchable apps. Google Play distribution requires a separate policy review.
- Accessibility access is optional, disclosed, and user-controlled.
- This is a development build, not a verified production release or a guarantee of complete Regain parity.


## Cloud build
A GitHub Actions workflow is included at `.github/workflows/android-build.yml`. See `PHONE_CLOUD_BUILD_GUIDE.md` for phone-only setup. A successful workflow produces a debug APK artifact; the first run is the required compile validation.
