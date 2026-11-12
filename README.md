# JARVIS Android starter — phone-only build route

## What this version currently contains
- Kotlin Android app with a blue JARVIS-inspired screen
- Tap-to-speak using Android's speech recognition service
- Spoken replies using Android Text-to-Speech
- Basic local commands: time, date, open Google/YouTube/Instagram, and web search

## Not implemented yet
This is **not yet JARVIS 2.0**. It does not currently include an AI model/backend, durable conversation memory, or a true always-on "Hey JARVIS" wake-word service. Those features need additional code and, for AI, a secure backend. Do not put an AI API key directly inside the APK.

## Build an APK using only your Android phone
This project includes a GitHub Actions workflow that builds a debug APK in the cloud.

1. In your phone browser, sign in to GitHub and create a new **public or private repository**.
2. Download and extract `JARVIS_Android_Starter.zip` using your phone's file manager.
3. Open the new GitHub repository in your browser. Choose **Add file → Upload files**.
4. Upload the *contents* of the extracted `JARVIS_Android` folder, including the hidden `.github` folder and its workflow file. If your phone's file picker hides `.github`, use GitHub's web editor to create `.github/workflows/build-apk.yml` and paste the workflow from this project.
5. Commit the files to the `main` branch.
6. Open the repository's **Actions** tab. Select **Build JARVIS Android APK**, then **Run workflow** if it has not started automatically.
7. When the workflow completes successfully, open that run, scroll to **Artifacts**, and download **JARVIS-debug-APK**.
8. Extract the downloaded artifact. Tap `app-debug.apk` to install it. If Android asks, allow your browser or file manager to **Install unknown apps** for this installation, then continue.
9. After installation, open JARVIS and allow microphone permission.

## Notes
- The APK produced by this workflow is a debug build for testing, not a Play Store release.
- Android may display a security warning for APKs installed outside the Play Store. Only install an APK you built from source you trust.
- Voice recognition depends on the speech service installed on your Vivo/Oppo phone and internet availability.
- The app listens only after you tap its microphone button. Background wake-word detection is not enabled.
- This environment does not have Android SDK, Gradle, ADB, or a connected physical device, so no APK has been compiled or physically tested here.
