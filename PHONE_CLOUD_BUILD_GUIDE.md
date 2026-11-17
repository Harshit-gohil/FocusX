# Build FocusX APK using only your Android phone

## What you need
- A GitHub account (free account is sufficient for a public repository; private repository Actions usage depends on current GitHub plan limits).
- Your Android phone browser.
- No PC is required for the cloud build steps.

## 1. Create a GitHub repository
1. Open https://github.com in your phone browser and sign in.
2. Create a new repository named `FocusX`.
3. For the easiest first build, choose Public. Do not upload personal data or secrets.
4. Create the repository.

## 2. Upload the project source
1. Extract `FocusX-v0.5-Cloud-Build.zip` using your phone's Files app.
2. Open the repository on GitHub.
3. Use Add file → Upload files.
4. Select the extracted project files and folders. Make sure `.github/workflows/android-build.yml` is included.
5. Commit the upload to the `main` branch.

Mobile browser file selection for many nested files can be awkward. If the GitHub mobile page cannot upload the extracted folder tree, use GitHub's web interface in desktop-site mode or a Git client app. Do not upload only the ZIP as a single file; the workflow needs the project files at repository root.

## 3. Run the build
1. Open the repository's Actions tab.
2. Select `Build FocusX APK`.
3. Tap Run workflow → Run workflow.
4. Wait for the job to finish. A green check means the cloud build step completed.

## 4. Download the APK
1. Open the completed workflow run.
2. Scroll to Artifacts.
3. Download `FocusX-debug-apk`.
4. Extract the downloaded artifact ZIP and open `app-debug.apk`.
5. Allow your browser or Files app to install unknown apps if Android asks.

## 5. If the build fails
Open the failed run, expand the first red error section, and share a screenshot or copy the error text here. Do not share GitHub passwords, access tokens, or private keys.

## Important
This workflow is a cloud build configuration, not proof that the source compiles. The first GitHub Actions run is the compile test. The app is still a development build; several listed modules remain incomplete as described in README.md.
