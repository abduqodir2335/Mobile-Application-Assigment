# KMP Advanced Project Structure: Practice 4 and 4.1

Kotlin Multiplatform practice project for **Lectures 10–11** (advanced project structure · choosing a project configuration). It continues [Practice 3](https://github.com/AsadDeving/MobileAPPsPR3), which covered Questions 1–13.

📄 **Practice 4, Sections 3–4 (Questions 14–25), is answered in [`ANSWERS.md`](ANSWERS.md).** Hands-on answers link to the code in this repository and show the real Gradle output.

📄 **Practice 4.1 (Tasks 1–30, Lectures 8–11) is answered in [`PRACTICE_4.1.md`](PRACTICE_4.1.md).** It comes with a small login app shared between Android, desktop and iOS.

## Modules

```
.
├── advanced-lab/       JVM + JS + Linux + iOS, custom jvmAndJsMain via manual dependsOn (Q16, Q17, Q21)
├── template-lab/       same targets, default hierarchy template + custom group (Q17)
├── dependencies-lab/   propagation, compatibility, version alignment (Q18, Q19, Q20)
├── shared/             Practice 4.1: KMP library (Android, desktop JVM, iOS), LoginValidator, XCFramework, maven-publish
├── androidApp/         Practice 4.1: Jetpack Compose login screen + LoginViewModel
├── desktopApp/         Practice 4.1: JVM desktop app using :shared
├── feature/auth, feature/profile, umbrella/   Practice 4.1: feature modules + umbrella "Shared" framework
├── iosApp/, swift-package/                    Practice 4.1: SwiftUI view model, Package.swift binaryTarget
├── build-logic/        convention plugin: printSourceSets / printCompilations report tasks (Lecture 7)
└── gradle/libs.versions.toml   single version catalog
```

### Source-set tree in `advanced-lab` (Q16)

```
                 commonMain
        ┌───────────┼──────────────┬───────────────────┐
  jvmAndJsMain  linuxX64Main  iosArm64Main  iosSimulatorArm64Main
    ┌────┴────┐
 jvmMain    jsMain
```

`jvmAndJsMain` is created with `by creating` and wired in with three `dependsOn` calls. Because of the manual wiring, the default hierarchy template is not applied, so there is no `iosMain`/`appleMain`/`nativeMain` (Q17). `template-lab` gets the same `jvmAndJsMain` by extending the template with `group("jvmAndJs")`, and keeps all of the template's intermediate source sets.

## Build and test

Requirements: JDK 17+ and the Android SDK (Android Studio sets it up). Building the iOS targets also needs macOS + Xcode.

```bash
./gradlew :advanced-lab:printSourceSets :template-lab:printSourceSets      # Q16 / Q17
./gradlew :advanced-lab:printCompilations :advanced-lab:printJvmMainCompilation   # Q21
./gradlew :dependencies-lab:printCoroutinesVersions                        # Q18 / Q20
./gradlew :dependencies-lab:compileCommonMainKotlinMetadata -Pq19.showError=true  # Q19 (fails on purpose)

./gradlew :advanced-lab:jvmTest :advanced-lab:compileKotlinJs :template-lab:jvmTest :dependencies-lab:jvmTest
./gradlew :advanced-lab:linuxX64Test :template-lab:linuxX64Test             # Linux
./gradlew :dependencies-lab:iosSimulatorArm64Test                          # macOS

# Practice 4.1
./gradlew :shared:jvmTest :shared:testDebugUnitTest :androidApp:testDebugUnitTest :androidApp:assembleDebug
./gradlew :desktopApp:run --args="student@ttpu.uz secret123"
./gradlew :shared:iosSimulatorArm64Test :shared:assembleSharedXCFramework :umbrella:linkDebugFrameworkIosSimulatorArm64   # macOS
./gradlew :shared:publishToMavenLocal
```

GitHub Actions ([`.github/workflows/build.yml`](.github/workflows/build.yml)) runs all of these on Ubuntu (JVM, JS, Linux) and macOS (iOS). It also checks that the Q19 build fails with exactly `Unresolved reference 'asExecutor'`.
