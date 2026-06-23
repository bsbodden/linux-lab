# Linux Commands — KMPilot demo (Kotlin Multiplatform)

A searchable, offline Linux-command reference, built as a **Kotlin Multiplatform + Compose Multiplatform** app
by [KMPilot](https://kmpilot.dev). One shared `commonMain` UI runs natively on **Android** and **iOS**; the
**wasm** build is the in-browser preview. (Port of SimonSchubert/LinuxCommandLibrary.)

## Build each target
- **Android** — `./gradlew assembleDebug` → `build/outputs/apk/debug/*.apk` (needs the Android SDK).
- **iOS** — on a Mac via `iosApp/`, or **free on CI** (below).
- **Web (wasm preview)** — `./gradlew wasmJsBrowserDevelopmentExecutableDistribution`.
- **Tests** — `./gradlew jvmTest` (acceptance criteria run on the JVM).

## iOS without a Mac (free)
The **Actions → "iOS (free simulator build)"** workflow compiles an iOS **Simulator** `.app` on a GitHub-hosted
macOS runner (no signing, no Apple account) and uploads it as an artifact. Add an `APPETIZE_TOKEN` repo secret
to also stream it to [Appetize.io](https://appetize.io) — the run summary prints a browser link you open on your
iPhone. macOS runners are **unlimited-free on public repos**. Installing on a *physical* iPhone still requires
Apple's \$99/yr program (TestFlight).

## Layout
- `src/commonMain` — shared domain, statecharts (KStateMachine), and the Compose UI + `App()`.
- `src/{androidMain,iosMain,wasmJsMain,jvmMain}` — thin per-platform entrypoints + `expect/actual` shims.
- `iosApp/` — the SwiftUI host (an XcodeGen `project.yml`; the `.xcodeproj` is generated in CI).
