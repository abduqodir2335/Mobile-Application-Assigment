# Practice 4.1: Kotlin Multiplatform, 30 Tasks (Answers)

Answers to *Practice 4.1: KMP & KMP project structure*. Part A follows Lectures 8–9, and Part B follows Lecture 11 (*Choosing a KMP Project Configuration*).

Every coding task is implemented in this repository, and the answer links to the file. CI ([`.github/workflows/build.yml`](.github/workflows/build.yml)) compiles and tests all of it on Ubuntu (Android, desktop) and macOS (iOS, Swift).

| Module | Tasks |
|---|---|
| [`shared/`](shared) | 4, 5, 7, 8, 9, 12, 14, 24, 25: KMP library for Android, desktop (JVM) and iOS |
| [`androidApp/`](androidApp) | 14, 17: Jetpack Compose login screen + `LoginViewModel` |
| [`desktopApp/`](desktopApp) | 17: JVM desktop app that uses `:shared` |
| [`iosApp/`](iosApp) | 14: SwiftUI `LoginViewModel` + `LoginView` (type-checked in CI against the real framework) |
| [`feature/auth`](feature/auth), [`feature/profile`](feature/profile), [`umbrella/`](umbrella) | 18, 19, 20, 30: feature modules + umbrella framework |
| [`swift-package/`](swift-package) | 25: `Package.swift` with a `.binaryTarget` |

Practice 4 (Questions 14–25) is in [`ANSWERS.md`](ANSWERS.md).

---

## Contents

- [Part A: What is Kotlin Multiplatform (1–15)](#part-a--what-is-kotlin-multiplatform-115)
- [Part B: KMP Project Configuration (16–30)](#part-b--kmp-project-configuration-1630)

---

## Part A – What is Kotlin Multiplatform (1–15)

### Task 1: KMP vs. Flutter vs. React Native

| | How it reaches the screen / platform |
|---|---|
| **Flutter** | Renders **its own widget tree through Skia/Impeller**. It does not use native platform views. |
| **React Native** | JavaScript **talks to native views across a JS bridge** (or the newer JSI). |
| **KMP** | No middle layer. Shared Kotlin code is **compiled by Kotlin/Native (LLVM) into a real ARM64 binary for iOS**, delivered as an Apple framework. No VM or interpreter runs it. The UI can stay native (SwiftUI) or be shared with Compose Multiplatform. |

### Task 2: How Kotlin reaches each target

| Target | How Kotlin reaches it | Gradle DSL |
|---|---|---|
| Android | Kotlin/JVM: JVM bytecode → DEX | `androidTarget()` |
| iOS | **Kotlin/Native**, an **LLVM**-based compiler producing native ARM64 binaries (framework) | `iosArm64()`, `iosSimulatorArm64()` |
| Browser, option 1 | **Kotlin/JS**: compiles to JavaScript (Stable) | `js { browser() }` |
| Browser, option 2 | **Kotlin/Wasm**: compiles to WebAssembly (Beta) | `wasmJs { browser() }` |
| Desktop (Windows/macOS/Linux) | Kotlin/JVM: runs on the JVM | `jvm()` |
| Server | Kotlin/JVM (e.g. with Ktor) | `jvm()` |

The compiler technology for iOS is **Kotlin/Native, built on LLVM**.

### Task 3: Shared or native?

| Item | Where | Why (one line) |
|---|---|---|
| Ktor API client | **shared (commonMain)** | Ktor is multiplatform; one HTTP client and one set of API models for both apps. |
| Biometric login | **kept native** | Face ID / BiometricPrompt need deep platform-hardware integration and system UI. |
| Input validation | **shared (commonMain)** | Pure logic that must behave identically on both platforms ([`LoginValidator`](shared/src/commonMain/kotlin/uz/ttpu/shared/LoginValidator.kt)). |
| Push notifications | **kept native** | Each platform has its own delivery service (FCM vs. APNs) and permission flow. |
| SQLDelight storage | **shared (commonMain)** | SQLDelight generates multiplatform Kotlin; only the small driver is per platform. |
| Deep-link navigation | **kept native** | Navigation, transitions and URL handling are platform-idiomatic (intent filters vs. universal links). |

### Task 4: `expect`/`actual` for Android, iOS and desktop

Common declaration: [`shared/src/commonMain/.../Platform.kt`](shared/src/commonMain/kotlin/uz/ttpu/shared/Platform.kt)

```kotlin
// commonMain
interface Platform { val name: String }
expect fun getPlatform(): Platform
```

```kotlin
// androidMain: Platform.android.kt
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}
actual fun getPlatform(): Platform = AndroidPlatform()
```

```kotlin
// iosMain: Platform.ios.kt (one actual for both iosArm64 and iosSimulatorArm64)
class IOSPlatform : Platform {
    override val name: String = "${UIDevice.currentDevice.systemName()} ${iosSystemVersion()}"
}
actual fun getPlatform(): Platform = IOSPlatform()
```

```kotlin
// jvmMain (desktop): Platform.jvm.kt
class DesktopPlatform : Platform {
    override val name: String = "Desktop (${System.getProperty("os.name")}, Java ${System.getProperty("java.version")})"
}
actual fun getPlatform(): Platform = DesktopPlatform()
```

Files: [`androidMain`](shared/src/androidMain/kotlin/uz/ttpu/shared/Platform.android.kt) · [`iosMain`](shared/src/iosMain/kotlin/uz/ttpu/shared/Platform.ios.kt) · [`jvmMain`](shared/src/jvmMain/kotlin/uz/ttpu/shared/Platform.jvm.kt). Running the desktop app prints `Hello from Desktop (Linux, Java 21…)`.

### Task 5: Find and fix the error

**Error:** an `expect` declaration may only declare a signature. It **cannot have a body**; the body belongs in each platform's `actual`. The compiler says:

```
e: Time.kt:3:1 Expected declaration cannot have a body.
```

**Fix** ([`Time.kt`](shared/src/commonMain/kotlin/uz/ttpu/shared/Time.kt) + actuals):

```kotlin
// commonMain
expect fun currentTimeMillis(): Long

// androidMain and jvmMain
actual fun currentTimeMillis(): Long = System.currentTimeMillis()

// iosMain
actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
```

(If the same body really works everywhere, drop `expect` and write a normal common function instead.)

### Task 6: Missing `actual` for iOS

**What fails:** every `expect` needs an `actual` for **every target** that `commonMain` compiles to. With actuals only in `androidMain` and `jvmMain`, the iOS compilations (`iosArm64`, `iosSimulatorArm64`) have no implementation, and the build fails. I reproduced it by deleting the iOS actual:

```
e: Time.kt:5:1 Expected currentTimeMillis has no actual declaration in module <commonMain> for Native
```

(The Android and JVM builds still compile. Only the native/iOS compilation is broken.)

**Two fixes:**

1. **Add the missing `actual` once in `iosMain`.** That intermediate source set is shared by `iosArm64Main` and `iosSimulatorArm64Main`, so one actual covers both iOS targets.
2. **Remove the need for `expect`/`actual`:** make it an `interface` in `commonMain` and pass in a platform implementation (constructor injection / DI), or write one common implementation if it can be expressed in pure Kotlin. Then there is no expect left that iOS could be missing.

### Task 7: Source sets merged for `iosArm64`

With the default hierarchy template, compiling for `iosArm64` merges:

**`commonMain` + `nativeMain` + `appleMain` + `iosMain` + `iosArm64Main`**

Verified with `./gradlew :shared:printSourceSets`:

```
commonMain               dependsOn: -                            targets: android, iosArm64, iosSimulatorArm64, jvm
nativeMain               dependsOn: commonMain                   targets: iosArm64, iosSimulatorArm64
appleMain                dependsOn: nativeMain                   targets: iosArm64, iosSimulatorArm64
iosMain                  dependsOn: appleMain                    targets: iosArm64, iosSimulatorArm64
iosArm64Main             dependsOn: iosMain                      targets: iosArm64
```

(The lecture's simplified picture is `commonMain + appleMain + iosArm64Main`; the template adds `nativeMain` and `iosMain` in between.)

**Why `appleMain` is visible to iOS but not to `androidMain`:** visibility follows `dependsOn` edges, and they only point **up** the tree, towards more general code. `iosArm64Main → iosMain → appleMain`, so iOS code sees `appleMain`. `androidMain` depends only on `commonMain`, and `appleMain` is on a different branch that compiles **only to Apple targets**. Android is not among its targets, so its code (which may use `platform.Foundation`, `NSUUID`, …) is never part of the Android compilation.

### Task 8: `UIDevice.currentDevice.systemVersion` in `iosMain`

[`shared/src/iosMain/.../IosSystemVersion.kt`](shared/src/iosMain/kotlin/uz/ttpu/shared/IosSystemVersion.kt)

```kotlin
import platform.UIKit.UIDevice

fun iosSystemVersion(): String = UIDevice.currentDevice.systemVersion
```

**Why not `commonMain`:** `platform.UIKit` is an Apple-only API that Kotlin/Native exposes only to source sets compiling **exclusively** to Apple targets. `commonMain` also compiles to Android and the JVM, where UIKit does not exist, so the import is unresolved there. This is the same rule as `java.io.File` in Lecture 9, in reverse.

### Task 9: Ktor `sourceSets` block

[`shared/build.gradle.kts`](shared/build.gradle.kts)

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:3.2.3")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.2.3")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.2.3")
        }
    }
}
```

(The repo uses the version catalog, `libs.ktor.client.core` etc., and also adds OkHttp to `jvmMain` for the desktop target.) Common code calls `HttpClient()` ([`HttpClientFactory.kt`](shared/src/commonMain/kotlin/uz/ttpu/shared/HttpClientFactory.kt)), and each platform finds its own engine on its classpath.

### Task 10: Kotlin Multiplatform vs. Compose Multiplatform

- **Kotlin Multiplatform (KMP)** shares **logic**: networking, data, business rules, state. The UI can stay 100% native.
- **Compose Multiplatform (CMP)** is JetBrains' declarative **UI** framework, built on KMP, that additionally shares the **UI layer**. It is Stable on Android, iOS and desktop; web (Wasm) is Beta.

**Layout for shared logic + native UI (this repository):**

```
shared/        KMP module: commonMain (logic) + androidMain/iosMain (platform bits)
androidApp/    Jetpack Compose UI  -> implementation(project(":shared"))
iosApp/        SwiftUI UI          -> import Shared  (framework built from :shared)
```

**KMP can be used without Compose Multiplatform, but not the other way round**: CMP is built on top of KMP.

### Task 11: Stability levels: ship it today?

| Feature | Stability | Ship in production today? |
|---|---|---|
| **iOS** target | **Stable** | **Yes.** Stable, with many production apps (Netflix, McDonald's, Cash App). |
| **Kotlin/Wasm** | **Beta** | **Only behind an abstraction, for non-critical web features.** It works, but APIs and tooling may still change, so budget for migrations. Use Kotlin/JS (Stable) for the browser when stability matters. |
| **`expect`/`actual` classes** | **Beta** | **Yes, carefully.** `expect`/`actual` *functions and properties* are stable. For classes, prefer an `interface` in common code + platform implementations, and use expect classes only where an interface doesn't fit. |
| **Compose Multiplatform for web** | **Beta** | **Not for a critical production web app yet.** Fine for internal tools or experiments. Keep the production web UI on a stable stack. |

### Task 12: `commonTest` test for `isValidEmail`

Function: [`EmailValidation.kt`](shared/src/commonMain/kotlin/uz/ttpu/shared/EmailValidation.kt) · Test: [`EmailValidationTest.kt`](shared/src/commonTest/kotlin/uz/ttpu/shared/EmailValidationTest.kt)

```kotlin
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidationTest {
    @Test
    fun acceptsValidEmail() {
        assertTrue(isValidEmail("student@ttpu.uz"))
    }

    @Test
    fun rejectsEmailWithoutDomain() {
        assertFalse(isValidEmail("student@"))
    }
}
```

**Gradle tasks:**

| Where | Task |
|---|---|
| JVM (desktop target) | `./gradlew :shared:jvmTest` |
| Android (local JVM unit tests) | `./gradlew :shared:testDebugUnitTest` |
| iOS simulator | `./gradlew :shared:iosSimulatorArm64Test` (needs macOS) |
| everything at once | `./gradlew :shared:allTests` |

### Task 13: Limitations of KMP in 2026 and mitigations

| Limitation (lecture) | Mitigation for a new team |
|---|---|
| **Swift interop.** Kotlin is exposed to Swift through an Objective-C header ("Swift export" is still experimental): no native Swift enums or default arguments, and `suspend`/`Flow` are awkward. | Design a small, Swift-friendly API facade (plain classes, callbacks or `async` wrappers), and consider **SKIE** to generate Swift-native `async`/`AsyncSequence` and enums. |
| **iOS binary size** overhead (the Kotlin runtime + libraries are embedded). | Ship **one static umbrella framework** (no duplicated runtime), measure release builds early, and only `export` the API Swift needs. Dead-code elimination then removes the rest. |
| **Team skills.** Few engineers are fluent in both Kotlin/Gradle and Swift/Xcode. | **Start small:** share one module (validation or networking), pair Android and iOS developers on it, and use remote distribution so iOS developers can start without Kotlin tooling. Add the hybrid workflow later. |

### Task 14: `LoginValidator` used from Android and SwiftUI

**Shared:** [`shared/src/commonMain/.../LoginValidator.kt`](shared/src/commonMain/kotlin/uz/ttpu/shared/LoginValidator.kt)

```kotlin
data class LoginValidationResult(val emailError: String?, val passwordError: String?) {
    val isValid: Boolean get() = emailError == null && passwordError == null
}

class LoginValidator {
    fun validate(email: String, password: String): LoginValidationResult =
        LoginValidationResult(validateEmail(email), validatePassword(password))

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !isValidEmail(email) -> "Enter a valid email address"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        password.none { it.isDigit() } -> "Password must contain a digit"
        password.none { it.isLetter() } -> "Password must contain a letter"
        else -> null
    }

    companion object { const val MIN_PASSWORD_LENGTH = 8 }
}
```

Tested in [`LoginValidatorTest`](shared/src/commonTest/kotlin/uz/ttpu/shared/LoginValidatorTest.kt) on every target.

**Android ViewModel:** [`androidApp/.../LoginViewModel.kt`](androidApp/src/main/kotlin/uz/ttpu/android/LoginViewModel.kt). It is used by the Compose [`LoginScreen`](androidApp/src/main/kotlin/uz/ttpu/android/LoginScreen.kt) and tested in [`LoginViewModelTest`](androidApp/src/test/kotlin/uz/ttpu/android/LoginViewModelTest.kt).

```kotlin
class LoginViewModel(private val validator: LoginValidator = LoginValidator()) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginClick() = _uiState.update { state ->
        val result = validator.validate(state.email, state.password)
        state.copy(emailError = result.emailError, passwordError = result.passwordError, isLoggedIn = result.isValid)
    }
}
```

**SwiftUI view model:** [`iosApp/iosApp/LoginViewModel.swift`](iosApp/iosApp/LoginViewModel.swift). CI type-checks it against the real `Shared` framework.

```swift
import Shared

@MainActor
final class LoginViewModel: ObservableObject {
    @Published var email = ""
    @Published var password = ""
    @Published private(set) var emailError: String?
    @Published private(set) var passwordError: String?
    @Published private(set) var isLoggedIn = false

    private let validator = LoginValidator()

    func logIn() {
        let result = validator.validate(email: email, password: password)
        emailError = result.emailError       // Kotlin String? -> Swift String?
        passwordError = result.passwordError
        isLoggedIn = result.isValid
    }
}
```

Both platforms keep their own UI state holder (`ViewModel` / `ObservableObject`). Only the rules are shared, so the error messages are identical on Android and iOS.

### Task 15: Migration order for an Android-only app

**Order:** 1. **data models** → 2. **networking** → 3. **local storage** → 4. **business rules** → 5. **UI** (last, or never: keep it native, or move to Compose Multiplatform later).

**Why data models first:** they are plain Kotlin data classes with no platform dependencies, so they move to `commonMain` almost unchanged and carry little risk. Every other layer depends on them: networking returns them, storage persists them, and business rules operate on them. Sharing them first means everything moved later already speaks the same types. Networking (Ktor) and storage (SQLDelight / Room KMP) come next because they have mature KMP libraries. Business rules follow once their inputs are shared. The UI is the most platform-specific layer, so it goes last.

---

## Part B – KMP Project Configuration (16–30)

### Task 16: Single vs. several shared modules

| | Advantage | Drawback |
|---|---|---|
| **Single shared module** | Simple design and low cognitive load: no deciding where code goes. A great starting point. | Compilation time grows with the module; there are no separate features, and no way to depend on only part of the shared code. |
| **Several shared modules** | Separation of concerns and better scalability as the app grows. Android can depend on just the features it needs. | More complicated setup: an umbrella module/framework for iOS, and more involved dependency management between modules. |

**Signal to split:** the single module has become slow to build (every change recompiles everything), or a consumer needs only part of it (e.g. only networking), or unrelated features and teams keep colliding in the same module.

### Task 17: `settings.gradle.kts` + consuming `:shared`

[`settings.gradle.kts`](settings.gradle.kts)

```kotlin
include(":shared")
include(":androidApp")
include(":desktopApp")
```

Each app consumes the module with the same dependency line. Gradle picks the right variant (Android AAR or desktop JVM jar) automatically:

```kotlin
// androidApp/build.gradle.kts  and  desktopApp/build.gradle.kts
dependencies {
    implementation(project(":shared"))   // or implementation(projects.shared) with type-safe accessors
}
```

Files: [`androidApp/build.gradle.kts`](androidApp/build.gradle.kts) · [`desktopApp/build.gradle.kts`](desktopApp/build.gradle.kts). Running `./gradlew :desktopApp:run --args="student@ttpu.uz secret123"` prints `Login OK for student@ttpu.uz`.

### Task 18: Two feature modules as two separate iOS frameworks

**What ends up inside the app:** each Kotlin/Native framework is self-contained and bundles **its own copy** of every dependency: the Kotlin runtime, kotlinx libraries and the shared library. With `AuthFramework` and `ProfileFramework` both depending on the same library, the app contains **two copies** of that library, compiled separately. Swift sees them as two unrelated sets of types (`AuthFramework.SessionStore` ≠ `ProfileFramework.SessionStore`), and the app gets bigger.

**Concrete failure with shared state** (this repo's example): `:feature:auth` writes the logged-in user into `SessionStore`, and `:feature:profile` reads it. As two frameworks, each has its **own** `SessionStore` singleton. The user logs in through auth's copy, profile reads its own empty copy and shows "Not logged in". Everything compiles and the Android build works. The bug appears **only on iOS**, because "the same" dependency is really two disconnected copies.

With one umbrella framework, the same flow works, and [`AppGraphTest`](umbrella/src/commonTest/kotlin/uz/ttpu/umbrella/AppGraphTest.kt) checks it.

### Task 19: Umbrella module `build.gradle.kts`

[`umbrella/build.gradle.kts`](umbrella/build.gradle.kts) (the repo version also has a `jvm()` target and a switch for Task 20):

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":feature:auth"))
            export(project(":feature:profile"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":feature:auth"))
            api(project(":feature:profile"))
        }
    }
}
```

`./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64` builds `Shared.framework`. CI checks that its header contains the feature types (`SessionStore`, `ProfileService`).

### Task 20: Swift cannot see the types from `:feature:auth`

**Cause:** two things are missing.

1. The dependency is `implementation(...)`. Its code is compiled into the framework, but it is **not part of the framework's public API**.
2. The framework has no `export(...)`. Only the module's own API (and types it exposes directly) go into the Objective-C/Swift header. A dependency's API appears in the header only if it is **exported**, and Kotlin only allows exporting **`api`** dependencies.

**Fix:**

```kotlin
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "Shared"
            export(project(":feature:auth"))        // 2. put auth's API into the header
        }
    }
    sourceSets.commonMain.dependencies {
        api(project(":feature:auth"))               // 1. api, not implementation
    }
}
```

CI demonstrates both sides. `./gradlew :umbrella:linkDebugFrameworkIosSimulatorArm64` (fixed) produces a header with `SessionStore`. The same build with `-Ptask20.broken=true` (the task's snippet: `implementation`, no `export`) produces a header **without** it.

### Task 21: Why one umbrella framework is the only way to get a single copy

Kotlin/Native compiles each framework in a **separate build**, and each binary is **trimmed to exactly what that one build needs** (dead-code elimination, everything linked in statically). A framework cannot assume anything about what else will be linked into the app, so it must embed its dependencies. The compiler building framework A has no visibility into framework B's build, so it cannot "share" a dependency with it. Two separately built frameworks therefore always mean two copies, each with its own types, singletons and state.

An **umbrella framework** turns the problem into one build: every shared module is linked into a **single** compiled artifact, so every dependency exists **exactly once**, with one consistent set of types. That is the only configuration that guarantees a single copy.

### Task 22: Monorepo vs. two vs. three repositories

| Layout | Advantage | Drawback | How the shared module is versioned |
|---|---|---|---|
| **Monorepo** | Easiest to set up (project wizards create it). iOS developers find and edit shared code without switching repositories. | iOS developers must install and learn Gradle and Android Studio/IntelliJ. It often doesn't fit apps already living in separate repositories. | **Not versioned separately.** Apps and shared code are always at the same commit; no publishing step. |
| **Two repos** (Android + shared \| iOS) | Android and iOS can be versioned separately. Android keeps using shared code as a local module. | iOS needs a way to get the shared code across the repository boundary, usually a published framework. Shared code feels "owned" by the Android repo. | **Moves in lockstep with the Android app** (same repo). iOS consumes a published framework version. |
| **Three repos** (Android \| iOS \| shared) | Full independence; both platforms consume the **same** versioned artifact (version parity). Shared code is no longer "Android's". | Most overhead: shared modules must be **published for Android/JVM too**, not just iOS, and Android developers lose the "just a local module" simplicity. | **Independently versioned and published** (e.g. 1.2.0). Both apps pin and upgrade a version. |

### Task 23: Publishing the shared code for Android/JVM (three repositories)

The two options from the lecture:

1. **Publish each feature module separately** (`auth:1.4.0`, `profile:2.1.0`, …).
   *Pro:* the Android app depends on only the features it needs, and each feature can be released on its own.
   *Con:* more artifacts and versions to publish and keep compatible; more dependency management.
2. **Publish only the umbrella module** (one artifact, e.g. `shared:1.2.0`).
   *Pro:* simple, with one version to manage, and Android and iOS consume exactly the same bundle.
   *Con:* all or nothing. Android pulls in every feature, even unused ones, and any change means a release of the whole thing.

**Trade-off:** granular control and smaller apps vs. simpler publishing and versioning.

### Task 24: `maven-publish` to `mavenLocal()`

[`shared/build.gradle.kts`](shared/build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "uz.ttpu.shared"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")   // publish the Android AAR too
    }
    // jvm(), iosArm64(), iosSimulatorArm64() ...
}

publishing {
    repositories {
        mavenLocal()
    }
}
```

`./gradlew :shared:publishToMavenLocal` publishes one root artifact plus one per target:

```
~/.m2/repository/uz/ttpu/shared/shared/1.0.0/                       <- root (Gradle module metadata)
~/.m2/repository/uz/ttpu/shared/shared-android/1.0.0/
~/.m2/repository/uz/ttpu/shared/shared-jvm/1.0.0/
~/.m2/repository/uz/ttpu/shared/shared-iosarm64/1.0.0/
~/.m2/repository/uz/ttpu/shared/shared-iossimulatorarm64/1.0.0/
```

**In the Android repository:**

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

// app/build.gradle.kts
dependencies {
    implementation("uz.ttpu.shared:shared:1.0.0")
}
```

Gradle reads the root module's metadata and picks `shared-android` automatically.

### Task 25: XCFramework + Swift package

**Gradle** ([`shared/build.gradle.kts`](shared/build.gradle.kts)):

```kotlin
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

kotlin {
    val xcFramework = XCFramework("Shared")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcFramework.add(this)
        }
    }
}
```

`./gradlew :shared:assembleSharedXCFramework` → `shared/build/XCFrameworks/release/Shared.xcframework`. CI zips it and prints its checksum with `swift package compute-checksum`.

**Swift package** ([`swift-package/Package.swift`](swift-package/Package.swift)):

```swift
.binaryTarget(
    name: "Shared",
    url: "https://github.com/AsadDeving/MobileAPPsPR4/releases/download/1.0.0/Shared.xcframework.zip",
    checksum: "<output of: swift package compute-checksum Shared.xcframework.zip>"
)
```

**Why a checksum is required:** SwiftPM downloads a **pre-built binary** from a URL, so it cannot rebuild or inspect it from source. The SHA-256 checksum pins the exact zip you released. SwiftPM refuses the package if the downloaded file differs, whether it was tampered with, corrupted, or replaced on the server under the same URL. Builds stay reproducible and safe. Every new release needs a new zip **and** a new checksum.

### Task 26: Local vs. remote distribution

| Axis | Local (source) | Remote (artifact) |
|---|---|---|
| **iOS developer onboarding** | Heavy: every iOS developer needs a JDK, Gradle and Android Studio/IntelliJ, plus basic Kotlin. | Light: add a Swift package / CocoaPod as usual, with no Kotlin tooling. |
| **Iteration speed** | Fast: a Kotlin change appears on the next iOS build, with no publishing. | Slow: change → publish → bump version → update app → rebuild. Debugging Kotlin from Xcode is hard. |
| **Versioning overhead** | None: no version number; the framework is rebuilt from source every time. | High: every change needs a versioned release (often automated, e.g. KMMBridge). |
| **Who can edit shared code** | Both Android and iOS developers; maintenance is a shared responsibility. | In practice the Kotlin/Android side. iOS contributions taper off over time. |

### Task 27: The hybrid workflow, step by step (iOS developer)

1. **Default setup (remote):** the iOS app depends on the **published** framework, e.g. `Shared 1.2.0` via Swift Package Manager. No Kotlin tooling is needed day to day.
2. **Switch to a local dependency:** to change shared code, check out the shared Kotlin repository and point the iOS build at a **locally built framework**. With SPM: replace the remote package with a local one, or link the output of `./gradlew :shared:assembleSharedXCFramework` / direct framework integration. With CocoaPods: flip the environment variable that switches the pod between local and remote.
3. **Verify the change:** edit the Kotlin code, rebuild, and run the iOS app. The change shows up on the next iOS build, so you can debug it immediately. Run the shared tests (`./gradlew :shared:allTests`) and get the change reviewed.
4. **Publish:** merge the Kotlin change and **publish a new version** of the shared module/framework (e.g. `1.3.0`, new XCFramework zip + checksum).
5. **Switch back:** point the iOS app back to the **remote** dependency and **update it to the new version** (`1.3.0`). Remove the local override, then build and commit.

**Correct order:** **publish first, then update the app** to depend on the new remote version. Never the reverse: an app that references a version that doesn't exist yet, or still points at a local build, is in an inconsistent state that nobody else can build.

### Task 28: Two apps, three libraries: repository graph

```
 logging-lib repo        networking-lib repo        analytics-lib repo
 (own version)           (own version)              (own version)
      │                     │        │                     │
      │   published for Android/JVM + iOS (Maven / klib)   │
      ▼                     ▼        ▼                     ▼
 ┌──────────────────────────────┐  ┌──────────────────────────────┐
 │ app-a-umbrella repo          │  │ app-b-umbrella repo          │
 │ api(logging), api(networking)│  │ api(networking), api(analytics)
 │ → Shared.xcframework (App A) │  │ → Shared.xcframework (App B) │
 └──────────────┬───────────────┘  └──────────────┬───────────────┘
                ▼                                 ▼
        App A repo (Android + iOS)        App B repo (Android + iOS)
        Android: logging + networking     Android: networking + analytics
        iOS: App A umbrella framework     iOS: App B umbrella framework
```

**Why each app gets its own umbrella repository:** an iOS app must link **exactly one** Kotlin framework (Tasks 18 and 21), and that framework must contain **exactly the set of libraries the app uses**. App A and App B need different subsets, so one shared umbrella would force App A to ship analytics (and App B logging) that it doesn't use. Frameworks cannot be combined after they are built separately. So each app gets a small umbrella repository whose only job is to depend on its libraries and produce its own single framework. The Android apps don't need it; they depend on the libraries directly.

### Task 29: Pick a configuration

| Case | Modules | Repositories | Distribution | Reason |
|---|---|---|---|---|
| **Solo developer, small app** | **Single** shared module | **Monorepo** | **Local** (source) | Simplest possible setup, exactly what the wizard generates. One person knows both stacks, so there's no onboarding cost and no reason to publish. |
| **Four engineers releasing Android and iOS together** | Single module, splitting into **features + umbrella** as it grows | **Monorepo** | **Local** | They ship together, so apps and shared code should always be at the same commit, with no versioning overhead. A small team can all install the Kotlin tooling. |
| **Separate Android and iOS teams, independent release cycles** | **Several modules + umbrella** | **Two or three repositories** (three if neither team should "own" the shared code) | **Remote**, plus the **hybrid** workflow for developers changing shared code | Independent releases need a versioned artifact that each team upgrades on its own schedule. Remote keeps iOS onboarding painless, and hybrid keeps a fast loop when someone works on the Kotlin code. |

### Task 30: Splitting a 40k-line `:shared` without changing the iOS API

Goal: Swift code keeps compiling **unchanged**: same framework name, same class names, same `import Shared`.

1. **Lock the current API.** Save the current framework header (`Shared.framework/Headers/Shared.h`) as a baseline, and make sure `commonTest` coverage is good. After the split, the new header must match it.
2. **Find the feature boundaries.** Group the code by feature (auth, profile, networking, …) and find a small **core** that everything uses (models, utilities). Break dependency cycles first.
3. **Create the new modules:** `:core`, `:feature:auth`, `:feature:profile`, … (KMP library modules with the same targets).
4. **Move code feature by feature, keeping package and class names.** Kotlin/Native names Swift types from the class name plus the framework prefix, not from the Gradle module, so unchanged class names mean unchanged Swift names. Move one feature at a time, building and running tests after each move.
5. **Turn `:shared` into the umbrella module** (or add `:umbrella` and point Xcode at it). It depends on every feature with `api(project(...))`, keeps `baseName = "Shared"` (same `isStatic` setting), and `export(project(...))`s every feature whose types Swift uses. The framework name, and therefore `import Shared` and the class prefixes, stay the same.
6. **Keep Android working:** the Android app can keep depending on `:shared` (the umbrella re-exports everything via `api`) or switch to depending on just the features it needs.
7. **Verify:** rebuild the framework, diff the new header against the baseline from step 1, build the iOS app without touching any Swift code, and run all tests (`allTests`, `iosSimulatorArm64Test`, Android unit tests).
8. **Benefit:** the build is faster, because Gradle compiles modules in parallel and only rebuilds the feature that changed, while the iOS app sees no API change.

This repository's [`feature/auth`](feature/auth), [`feature/profile`](feature/profile) and [`umbrella`](umbrella) modules show the end state of steps 3–5.
