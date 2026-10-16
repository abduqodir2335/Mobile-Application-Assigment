# Practice 4: Kotlin Multiplatform Project Structure, continued (Answers)

Answers to Sections 3–4 (Questions 14–25) of *Practice 4: KMP Project Structure (continuation)*.

- **Theoretical** questions are answered here.
- **Hands-on** questions are answered here **and** built in this repository. Each one links to the real files, gives the command that checks it, and shows the output it printed.

| Module | Used for |
|---|---|
| [`advanced-lab/`](advanced-lab) | Q16, Q17, Q21: JVM + JS + Linux + iOS, custom `jvmAndJsMain` wired with manual `dependsOn` |
| [`template-lab/`](template-lab) | Q17: the same targets **without** manual `dependsOn` (default hierarchy template) |
| [`dependencies-lab/`](dependencies-lab) | Q18, Q19, Q20: dependency propagation, compatibility and version alignment |
| [`build-logic/`](build-logic) | Convention plugin (Lecture 7) that adds the `printSourceSets` / `printCompilations` report tasks |

Versions: Kotlin 2.2.20, Gradle 8.14.3.

---

## Contents

- [Section 3: KMP Advanced Project Structure (Lecture 10)](#section-3--kmp-advanced-project-structure-lecture-10): Q14–Q21
- [Section 4: Choosing a KMP Project Configuration (Lecture 11)](#section-4--choosing-a-kmp-project-configuration-lecture-11): Q22–Q25

---

## Section 3 – KMP Advanced Project Structure (Lecture 10)

### Question 14 (Theoretical): `dependsOn` is not a `dependencies {}` entry

The claim is wrong because the two do different jobs:

| | `A.dependsOn(B)` | `dependencies { implementation(...) }` |
|---|---|---|
| Connects | two **source sets of the same project** in one target hierarchy | a source set to code **outside** it: a published library or another Gradle project |
| Kotlin-specific? | Yes, it is a Kotlin relation. Gradle itself does not know about it | No, it is a normal Gradle dependency |
| Where it is written | its own statement, **outside** `dependencies {}` (the DSL keeps them apart on purpose) | inside a source set's `dependencies {}` block |

When `A.dependsOn(B)` is declared, **all four** of these hold at the same time:

1. **A sees B's API, including `internal` declarations**, not just the public API.
2. **A can provide `actual` implementations for B's `expect` declarations.** This is an *if and only if*: an actual can only be written in a source set that depends on the expect's source set, directly or through a chain of `dependsOn`.
3. **B compiles to every target A compiles to**, plus its own. The target list flows *up* the tree. This is how `jvmAndJsMain` gets its JVM and JS labels in Q16.
4. **A inherits all of B's regular dependencies.**

So `dependsOn` is a **visibility and actualization relation** ("who can see this code, and who may implement its `expect`s?"). It is **not** a packaging or build dependency. The compiler checks these edges in every compilation (Lecture 10, slide 4), and they form a tree with no cycles.

### Question 15 (Theoretical): Two misuses of `dependsOn`

**`commonTest.dependsOn(commonMain)` is a mistake.** `dependsOn` is reserved for building the hierarchy of source sets Kotlin already understands as one target tree (`commonMain → iosMain → iosArm64Main`, and the parallel `*Test` tree). There is also nothing to gain: test code already sees production code, because each target's `test` compilation is linked to its `main` compilation (`commonTest` sees `commonMain`, `jvmTest` sees `jvmMain`). Adding a `dependsOn` edge across the Main/Test trees just mixes the two hierarchies, and Kotlin 2.2.20 rejects it at configuration time (I tried it in `dependencies-lab`):

```
e: ❌ Invalid `dependsOn` Configuration in Common Source Set
commonTest can't declare dependsOn on other source sets
Solution: Please remove the `dependsOn` configuration from the commonTest source set
```

**Calling `dependsOn` on a library's `commonMain` is a mistake.** `dependsOn` only works between source sets **of your own project**. It cannot point at a published library or at another Gradle project's source set. External code always goes through the `dependencies {}` block:

```kotlin
commonMain.dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // a library
    implementation(project(":utils:concurrency"))                          // another Gradle project
}
```

Kotlin then resolves that library dependency down to the library's own source sets by itself (see Q19). Rule of thumb: **`dependsOn` shapes your own tree, and `dependencies {}` brings code in from outside it.**

### Question 16 (Hands-on): Custom `jvmAndJsMain` source set

**Build script:** [`advanced-lab/build.gradle.kts`](advanced-lab/build.gradle.kts)

```kotlin
kotlin {
    jvm()
    js(IR) { browser() }
    linuxX64()

    sourceSets {
        val jvmAndJsMain by creating {
            // "by creating" registers a NEW source set; until the dependsOn calls below
            // it has no targets and would never be compiled.
        }
        jvmAndJsMain.dependsOn(commonMain.get())
        jvmMain.get().dependsOn(jvmAndJsMain)
        jsMain.get().dependsOn(jvmAndJsMain)
    }
}
```

> Kotlin 2.x exposes the default source sets as `NamedDomainObjectProvider`s, so `jvmMain` and `jsMain` need `.get()` too. That is the same reason the lecture writes `commonMain.get()`.

**Shared function moved into `jvmAndJsMain`:** [`advanced-lab/src/jvmAndJsMain/.../JvmAndJsShared.kt`](advanced-lab/src/jvmAndJsMain/kotlin/com/asaddeving/advancedlab/JvmAndJsShared.kt) defines `formatPrice(cents)`. It is called from [`jvmMain`](advanced-lab/src/jvmMain/kotlin/com/asaddeving/advancedlab/Platform.jvm.kt) (`jvmReceipt`) and [`jsMain`](advanced-lab/src/jsMain/kotlin/com/asaddeving/advancedlab/Platform.js.kt) (`jsReceipt`). `linuxX64Main` **cannot** see it.

**Check it:**

```bash
./gradlew :advanced-lab:printSourceSets
./gradlew :advanced-lab:jvmTest :advanced-lab:compileKotlinJs :advanced-lab:compileJvmAndJsMainKotlinMetadata
```

Output of `printSourceSets` (from [`build-logic`](build-logic/src/main/kotlin/kmp-structure-report.gradle.kts)):

```
commonMain               dependsOn: -                            targets: iosArm64, iosSimulatorArm64, js, jvm, linuxX64
jvmAndJsMain             dependsOn: commonMain                   targets: js, jvm
jvmMain                  dependsOn: commonMain, jvmAndJsMain     targets: jvm
jsMain                   dependsOn: commonMain, jvmAndJsMain     targets: js
linuxX64Main             dependsOn: commonMain                   targets: linuxX64
...
```

`jvmAndJsMain` now carries the **js, jvm** labels. It got them from the source sets that depend on it (rule 3 from Q14). Linux sits on its own branch. The project syncs, `jvmTest` passes ([`FormatPriceJvmTest`](advanced-lab/src/jvmTest/kotlin/com/asaddeving/advancedlab/FormatPriceJvmTest.kt)), and both the JS and the `jvmAndJsMain` metadata compilations succeed.

### Question 17 (Hands-on): Adding iOS targets: where did `appleMain` / `iosMain` go?

`iosArm64()` and `iosSimulatorArm64()` were added to the Q16 project ([`advanced-lab/build.gradle.kts`](advanced-lab/build.gradle.kts)).

**What I observed:** **no** `appleMain`, `iosMain` or `nativeMain` appeared. Both iOS leaves hang directly off `commonMain`:

```
$ ./gradlew :advanced-lab:printSourceSets
iosArm64Main             dependsOn: commonMain                   targets: iosArm64
iosSimulatorArm64Main    dependsOn: commonMain                   targets: iosSimulatorArm64
```

In Android Studio's Project view, `advanced-lab/src/iosMain` is a plain folder, not a source root (no blue/teal icon). The file in it ([`Platform.ios.kt`](advanced-lab/src/iosMain/kotlin/com/asaddeving/advancedlab/Platform.ios.kt)) is **not compiled**. Gradle prints a warning during sync:

```
w: ⚠️ Default Kotlin Hierarchy Template Not Applied Correctly
The Default Kotlin Hierarchy Template was not applied to 'project ':advanced-lab'':
Explicit .dependsOn() edges were configured for the following source sets:
[jsMain, jvmAndJsMain, jvmMain]
```

Because nothing is shared between the iOS targets, each iOS leaf needs its own `actual` ([`iosArm64Main`](advanced-lab/src/iosArm64Main/kotlin/com/asaddeving/advancedlab/Platform.iosArm64.kt), [`iosSimulatorArm64Main`](advanced-lab/src/iosSimulatorArm64Main/kotlin/com/asaddeving/advancedlab/Platform.iosSimulatorArm64.kt)).

**Control group:** [`template-lab`](template-lab/build.gradle.kts) has the same five targets but **no manual `dependsOn`**, so the template is applied:

```
$ ./gradlew :template-lab:printSourceSets
nativeMain               dependsOn: commonMain                   targets: iosArm64, iosSimulatorArm64, linuxX64
appleMain                dependsOn: nativeMain                   targets: iosArm64, iosSimulatorArm64
iosMain                  dependsOn: appleMain                    targets: iosArm64, iosSimulatorArm64
iosArm64Main             dependsOn: iosMain                      targets: iosArm64
linuxMain                dependsOn: nativeMain                   targets: linuxX64
webMain                  dependsOn: commonMain                   targets: js
jvmAndJsMain             dependsOn: commonMain                   targets: js, jvm
...
```

**Explanation (the lecture's caveat):** the **default hierarchy template** is what silently creates intermediate source sets such as `nativeMain`, `appleMain`, `iosMain` and `linuxMain`. **As soon as a project writes any manual `dependsOn` call, Kotlin stops applying that template for the whole project.** It is not limited to the source sets you touched. Manual wiring is a trade: you get exactly the tree you asked for, but you lose the intermediate source sets you used to get for free and have to declare them yourself.

Two ways to get both:

1. Re-create the missing level by hand: `val iosMain by creating { dependsOn(commonMain.get()) }`, then `iosArm64Main.dependsOn(iosMain)` and `iosSimulatorArm64Main.dependsOn(iosMain)`.
2. **Extend** the template instead of replacing it. `template-lab` does this:

   ```kotlin
   @OptIn(ExperimentalKotlinGradlePluginApi::class)
   applyDefaultHierarchyTemplate {
       common {
           group("jvmAndJs") { withJvm(); withJs() }
       }
   }
   ```

   The output above shows the result: `jvmAndJsMain` **and** `nativeMain`/`appleMain`/`iosMain` all exist.

### Question 18 (Theoretical): Dependency propagation

**Propagation** means a dependency declared on one source set is automatically available to **every source set that depends on it, directly or transitively**. When `kotlinx-coroutines-core:1.7.3` is declared once in `commonMain.dependencies {}`, Kotlin passes it down every `dependsOn` edge to `androidMain`, `iosMain`, `iosArm64Main` and `iosSimulatorArm64Main`. This follows rule 4 from Q14 (A inherits B's dependencies).

**No, you don't add it again** to `androidMain` or `iosMain`. Declare a dependency once, at the highest source set that needs it.

**Scoping it to iOS only:** don't declare it on `commonMain`. Declare it on `iosMain`:

```kotlin
iosMain.dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
```

It then propagates only to `iosArm64Main` and `iosSimulatorArm64Main` and never reaches `androidMain`. **Which source set you attach a dependency to is itself a scoping decision.**

In this repo: [`dependencies-lab/build.gradle.kts`](dependencies-lab/build.gradle.kts) declares coroutines once on `commonMain`. `./gradlew :dependencies-lab:printCoroutinesVersions` shows it on every `*Main` source set (`jvmMain`, `nativeMain`, `appleMain`, `iosMain`, `iosArm64Main`, `iosSimulatorArm64Main`).

### Question 19 (Theoretical): Why exactly one of the three calls is red

**The compatibility rule:** a published KMP library ships its **own** source-set structure. `kotlinx-coroutines-core` has its own `commonMain` (all targets), `concurrentMain` (every target **except JS**), `jsMain`, and a JVM-only source set. A dependency on "coroutines" is really resolved into **source set → source set** edges. A library source set is visible from your source set **only if it compiles to at least all of your source set's targets.**

Your `commonMain` compiles to **android, iosArm64, iosSimulatorArm64**:

| Call | Library source set it comes from | Targets of that source set | Covers android + iosArm64 + iosSimulatorArm64? | Result |
|---|---|---|---|---|
| `Dispatchers.Main` | coroutines **`commonMain`** | all targets | ✅ yes | resolves |
| `runBlocking { }` | coroutines **`concurrentMain`** | all except JS | ✅ yes (none of yours is JS) | resolves |
| `dispatcher.asExecutor()` | coroutines **JVM-only** source set | JVM only | ❌ no, iOS is missing | **Unresolved reference** |

`asExecutor()` returns a `java.util.concurrent.Executor`, which does not exist on iOS. Its source set does not cover your iOS targets, so it is incompatible and invisible from `commonMain`. The same call compiles fine in `androidMain`/`jvmMain`, where JVM is the only target.

**Reproduced in this repo** (with `jvm()` in place of Android):

- [`commonMain/CoroutinesInCommon.kt`](dependencies-lab/src/commonMain/kotlin/com/asaddeving/dependencieslab/CoroutinesInCommon.kt) uses `Dispatchers.Main` and `runBlocking`. It compiles.
- [`jvmMain/ExecutorBridge.kt`](dependencies-lab/src/jvmMain/kotlin/com/asaddeving/dependencieslab/ExecutorBridge.kt) uses `asExecutor()`. It compiles, and [`ExecutorBridgeTest`](dependencies-lab/src/jvmTest/kotlin/com/asaddeving/dependencieslab/ExecutorBridgeTest.kt) passes.
- [`q19Error/Q19Error.kt`](dependencies-lab/src/q19Error/kotlin/com/asaddeving/dependencieslab/Q19Error.kt) is the lecture screenshot. With the opt-in flag it is added to `commonMain`, and only `asExecutor` fails:

```
$ ./gradlew :dependencies-lab:compileCommonMainKotlinMetadata -Pq19.showError=true
e: .../Q19Error.kt:4:27 Unresolved reference 'asExecutor'.
e: .../Q19Error.kt:11:16 Unresolved reference 'asExecutor'.
BUILD FAILED
```

### Question 20 (Theoretical): Version alignment, and why `iosTest` doesn't leak

**`*Main` alignment:** `commonMain` is compiled several times: once on its own, and again inside every platform compilation it feeds. All of these must agree on **one** version of each library, or the binaries would be inconsistent. When the Android target is built, `commonMain` (coroutines **1.7.3**) and `androidMain` (`navigation-compose:2.7.7`, which needs coroutines **1.8.0+**) are compiled together. Gradle's conflict resolution picks the **newest** requested version, **1.8.0**, and applies it to `commonMain` itself, even though 1.7.3 is what the build script says. Since `commonMain` must be consistent everywhere, the same 1.8.0 then **propagates to `iosMain`** (and the iOS leaves), even though nothing on iOS asked for it.

> A version written in the build script is a **minimum, not a guarantee**. Check the real version with `./gradlew :module:dependencies` / `dependencyInsight`, or with this repo's report task.

**Why `iosTest` doesn't affect `*Main`:** the `*Test` source sets are aligned in a **separate** tree. The Gradle configuration behind each `*Test` source set includes everything from its matching `*Main`, **but not the other way round**. Upgrading coroutines in `iosTest` aligns `commonTest`, `iosTest` and the other `*Test` source sets (including `androidTest`). Every `*Main` source set keeps the version it already aligned to. That is what lets a team try a new library version in tests without shipping it.

**Reproduced in this repo** ([`dependencies-lab/build.gradle.kts`](dependencies-lab/build.gradle.kts)). The build declares `commonMain → coroutines 1.7.3` and `jvmMain → kotlinx-coroutines-reactive:1.8.0`, a JVM-only library that needs coroutines 1.8.0 and stands in for `navigation-compose` in `androidMain`. It also declares `iosTest → coroutines 1.8.1`.

```
$ ./gradlew :dependencies-lab:printCoroutinesVersions
kotlinx-coroutines-core per SOURCE SET (*Main and *Test aligned separately):
  appleMain                1.8.0
  commonMain               1.8.0     <- typed 1.7.3, raised to 1.8.0 by jvmMain
  iosArm64Main             1.8.0
  iosMain                  1.8.0     <- nothing on iOS asked for 1.8.0
  iosSimulatorArm64Main    1.8.0
  jvmMain                  1.8.0
  nativeMain               1.8.0
  appleTest                1.8.1
  commonTest               1.8.1     <- iosTest's 1.8.1 aligns the *Test tree
  iosTest                  1.8.1
  jvmTest                  1.8.1
  ...                                <- and no *Main source set moved to 1.8.1
```

The task also prints each platform compilation's own classpath. With Kotlin 2.2.20, the iOS **native** `main` classpath still resolves 1.7.3, because that graph contains no 1.8.0 requirement. The source-set level shown above, which is what the IDE and the shared-code compilations use, is the alignment the lecture describes.

### Question 21 (Hands-on): Reaching a compilation

**Build script:** [`advanced-lab/build.gradle.kts`](advanced-lab/build.gradle.kts)

```kotlin
kotlin {
    jvm {
        val mainCompilation = compilations.getByName("main")
        val testCompilation = compilations.getByName("test")
        println("[Q21] jvm compilations: main = '${mainCompilation.name}', test = '${testCompilation.name}'")
    }
}

tasks.register("printJvmMainCompilation") {
    val report = provider {
        val main = kotlin.jvm().compilations.getByName("main")
        "jvm main compilation: name='${main.name}', source sets=${main.allKotlinSourceSets.map { it.name }.sorted()}"
    }
    doLast { println(report.get()) }
}
```

**Check it:** `./gradlew :advanced-lab:printJvmMainCompilation :advanced-lab:printCompilations`

```
[Q21] jvm compilations: main = 'main', test = 'test'
jvm main compilation: name='main', source sets=[commonMain, jvmAndJsMain, jvmMain]

iosArm64             -> main (default source set: iosArm64Main), test (default source set: iosArm64Test)
iosSimulatorArm64    -> main (default source set: iosSimulatorArm64Main), test (default source set: iosSimulatorArm64Test)
js                   -> main (default source set: jsMain), test (default source set: jsTest)
jvm                  -> main (default source set: jvmMain), test (default source set: jvmTest)
linuxX64             -> main (default source set: linuxX64Main), test (default source set: linuxX64Test)
```

Every target has a **`main`** and a **`test`** compilation registered by default, including the `jvm` target.

**Compilation vs. source set, in one sentence:** a *source set* is a group of source files with its own dependencies (**how code is organized and shared**), while a *compilation* is **one compiler run for one target** (e.g. `jvm/main`) that bundles every source set relevant to that target, here `commonMain + jvmAndJsMain + jvmMain`, into one binary.

---

## Section 4 – Choosing a KMP Project Configuration (Lecture 11)

> Lecture 11's slides weren't among the uploaded files, so these answers follow the source the course decks are built on: kotlinlang.org's *Choosing a configuration for your Kotlin Multiplatform project* page (`multiplatform-project-configuration.html`).

### Question 22 (Theoretical): One shared module or several?

| | **Single shared module** | **Several shared (feature) modules** |
|---|---|---|
| **Pros** | Simplest setup: one module, one framework for iOS, little Gradle configuration. Ideal for a first project or a few features. | Separation of concerns: each feature is isolated, can be owned by a team, and only depends on what it needs. Scales with the codebase. Gradle can build modules in parallel and cache them. |
| **Cons** | Doesn't scale. Every feature lands in one module, so it becomes a monolith: features can't be separated, the module gets harder to maintain, and every change rebuilds all of it. | More configuration and more Gradle modules. On iOS the features must be combined into **one umbrella framework** (see Q23). |

**The growing-app trade-off:** start with **one shared module**. It's enough while the app is small. When the app grows (more features, more people working in the shared code, longer builds, features that should not see each other), the single module becomes hard to maintain. That is the point to **split it into several feature modules** and add an umbrella module for iOS. For this team: one module now, split later.

### Question 23 (Theoretical): One iOS framework per feature module breaks down

**Why it breaks:** every Kotlin/Native framework is a self-contained binary that includes **its own copy** of everything it depends on: the Kotlin runtime, kotlinx libraries and shared modules. Suppose `featureA` and `featureB` both depend on a common `data` module and each is exported as its own framework. The iOS app then contains:

- **duplicated code**: two copies of `data`, coroutines and the Kotlin runtime, so the app gets bigger;
- **two different, incompatible copies of the same types**: `FeatureA.DataRepository` and `FeatureB.DataRepository` are unrelated classes to Swift. An object created through framework A cannot be passed to framework B.

**The concrete failure mode:** **state is not shared through "the same" dependency.** If `data` holds a singleton (a cache, a logged-in user, a `StateFlow` of settings), each framework has its **own** instance. Feature A updates its copy, and feature B never sees the change, even though in Kotlin both "depend on the same module".

**The umbrella module fixes it.** It is one extra shared module that **depends on all feature modules and is the only module that produces the iOS framework**. It re-exports the features' API:

```kotlin
// umbrella/build.gradle.kts
kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "Shared"
            export(project(":featureA"))
            export(project(":featureB"))
        }
    }
    sourceSets.commonMain.dependencies {
        api(project(":featureA"))
        api(project(":featureB"))
    }
}
```

The iOS app imports a **single** framework, so each dependency (and each singleton) exists **once**, and types can move freely between features. Android doesn't need an umbrella; it uses the feature modules directly.

### Question 24 (Theoretical): Monorepo vs. 2 repos vs. 3 repos

| Layout | Advantage | Disadvantage |
|---|---|---|
| **Monorepo** (Android + iOS + shared) | Simplest: one change can update shared code and both apps in the same commit, and there is no versioning or publishing of the shared code. | Everyone works in one repository and needs the whole toolchain. It's hard to adopt if the Android and iOS apps already live in separate repos with separate teams. |
| **Two repos** (Android + shared \| iOS) | Android keeps using shared code from source, with no publishing for Android. iOS developers don't need the Kotlin/Gradle toolchain; they consume a ready-made framework. | The iOS app only gets shared changes after a **framework is published** (e.g. as an XCFramework through Swift Package Manager or CocoaPods). Shared code is effectively owned by the Android side, and iOS iterates more slowly. |
| **Three repos** (Android \| iOS \| shared) | Shared code is independent and owned on its own, with clean boundaries and its own release cycle. Neither app repo contains the other platform's code. | The most complex setup: both apps depend on **published versions**, every shared change needs a release, and a feature that touches shared and app code spans several repos and PRs. |

**The versioning consequence:**

- **Two repositories:** shared modules live next to the Android app and are consumed **from source**. Only the **iOS framework** has to be built, versioned and published.
- **Three repositories:** neither app has the shared source, so the **shared modules themselves must be versioned and published for both platforms**: as **Maven artifacts (`.klib`/`.aar`) for Android** and as a **framework/XCFramework (SPM/CocoaPods) for iOS**. Both apps then pin and upgrade a shared version explicitly.

### Question 25 (Theoretical): Design for 200 engineers, 5 apps, shared logging/networking/analytics

**1. Repositories: a "many repositories" setup.**
- **One repository per shared library**: `logging`, `networking`, `analytics`. That is three library repos, each a KMP library with its own owners, CI and release version. (If one platform team owns all three, they can live in one *libraries* repo with three independent modules and versions. The point is that each library is **separately versioned and published**.)
- **One repository per app**, five in total (each with its Android + iOS code, or split per platform as the app team prefers).

*Why:* with five consumers and 200 engineers, a monorepo would force every team onto one build and one release train. Separate libraries give clear ownership and let each library evolve at its own pace.

**2. Each app consumes only what it needs.**
- **Android:** each app adds only the Maven coordinates it uses, e.g. `implementation("com.company:logging:2.3.0")` and `implementation("com.company:analytics:1.8.0")`, but not `networking` if it doesn't need it.
- **iOS:** each app gets **its own umbrella module** (inside the app repo) that depends on, and `export`s, only that app's libraries and builds **one framework** for it. This avoids the duplicate-dependency and split-state problem from Q23, and no app is forced to ship libraries it doesn't use.

**3. Distribution: the hybrid workflow.**
- **Remote** as the default: libraries are published to an internal Maven repository (Android) and as XCFrameworks/SPM packages (iOS). App teams pin stable versions and don't need to build library code. Most of the 200 engineers never need the Kotlin/Native toolchain for the libraries. Pure **local** (source) distribution doesn't scale to five apps: every app build would compile every library, and every library change would immediately affect every app.
- **Local** while developing a library: an engineer changing `networking` swaps the remote artifact for the local source (e.g. Gradle `includeBuild("../networking")` / a local SPM path). They can test the change inside a real app before publishing.
- That combination is the **hybrid workflow**: remote for consumers, local for whoever is changing the shared code. It gives fast iteration for library authors and stable, versioned dependencies for everyone else, which fits a large organization.
