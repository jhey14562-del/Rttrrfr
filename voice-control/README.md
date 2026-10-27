# Local Voice Control (Android)

A standalone native Android Java app for explicit, user-triggered voice and typed accessibility commands. It has no backend, account, analytics, or website.

## Features
- Status screen for microphone permission, Accessibility enabled state, and live service connection.
- Tap-to-speak Android speech recognition, plus a typed-command fallback.
- Launch installed apps by their launcher label (for example `open Camera`, `open WhatsApp`, `open Settings`).
- Browse/filter installed launchable apps and open them from the list.
- System navigation: Home, Back, Recents, Notifications, Quick Settings, and Power Dialog (typed/spoken command `power dialog`).
- Swipe left/right and scroll up/down gestures.
- Tap/click accessible on-screen text, long-press accessible text, and type text into a focused text field.
- GitHub Actions workflow builds a debug APK and uploads it as an artifact.

## Setup on Android
1. Install the APK from the GitHub Actions artifact.
2. Open Local Voice Control.
3. Grant microphone permission.
4. Tap **Enable Accessibility Service**, then enable **Local Voice Control** in Android Accessibility settings. Return to the app and verify that the service shows Enabled and Connected.
5. Press **Speak a command** or type a command and tap **Run typed command**.

## Commands
- `open Camera`, `open Settings`, `open WhatsApp`, or another installed app's displayed name
- `tap Search`, `click Allow`, `long press a word`
- `type hello world` (first focus a text field yourself)
- `go home`, `go back`, `recent apps`, `notifications`, `quick settings`, `power dialog`
- `scroll down`, `scroll up`, `swipe left`, `swipe right`

## Important limits
- Speech recognition is supplied by the device's Android recognition provider. It may need internet and is not guaranteed offline.
- This is not an always-listening assistant: microphone use starts only after you tap Speak. No wake word or background recording.
- Accessibility actions depend on the target app exposing accessible UI nodes. Secure/system permission dialogs, banking apps, DRM screens, and some custom-drawn interfaces may block actions. It cannot bypass Android security or permissions.
- Debug APKs built by GitHub Actions are unsigned for Play Store release purposes; Android can install a debug-signed APK. A release build requires a signing key.

## Build
Push the repository contents to GitHub. The workflow at `.github/workflows/build-local-voice-control.yml` runs on push or manual dispatch. Download the `local-voice-control-debug-apk` artifact from the workflow run.


## Version 3.0 additions
- Custom vector launcher icon and themed status/navigation bars.
- Expanded quick-action buttons, including Settings, tap Allow, and long-press Search.
- Recent command activity list with clear-history control.
- App list filtering and launch-by-name remain available.
- Updated app version to 3.0.

## Important Android limitations
The app does not silently grant permissions or bypass secure screens. Accessibility commands are attempted only after the user enables the service. Whether a target app exposes tappable labels or permits text setting depends on that app and Android version. Speech recognition is supplied by the installed Android recognition provider and may use the network.


## Version 4.0 — GitHub-ready package
- Includes a local vector header illustration and launcher icon; the app does not need internet to load these graphics.
- GitHub Actions workflow is at `.github/workflows/android-apk.yml`, similar to a standard native Android APK workflow.
- Upload the contents of this ZIP to the repository root (keep `.github/` and `voice-control/` at the root), then open **Actions → Build Local Voice Control APK → Run workflow**.
- Expected artifact: `local-voice-control-debug-apk`.
- The ZIP was integrity-checked. An Android build has not been verified in this environment; use the Actions result to confirm compilation and retrieve the APK.
