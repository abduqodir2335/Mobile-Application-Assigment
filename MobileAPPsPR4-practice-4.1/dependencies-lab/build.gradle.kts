plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("kmp-structure-report")
}

kotlin {
    // Lecture 10 uses android + iosArm64 + iosSimulatorArm64. This lab uses jvm() in place of the
    // Android target so it builds without the Android SDK; for dependency resolution the two behave
    // the same way (both are JVM-family targets fed by commonMain).
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // Q18: declared ONCE on commonMain, propagated to jvmMain, iosMain, iosArm64Main, iosSimulatorArm64Main.
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core) // 1.7.3, the version written in the lecture
        }

        // Q20 (*Main alignment): a JVM-only library that requires coroutines 1.8.0, playing the role of
        // androidx.navigation-compose:2.7.7 in androidMain. Gradle raises commonMain to 1.8.0 everywhere.
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.reactive)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // Q20 (*Test alignment): try a newer coroutines in iosTest only. It aligns the *Test tree,
        // but no *Main source set (commonMain, jvmMain, iosMain) is affected.
        iosTest.dependencies {
            implementation(libs.kotlinx.coroutines.core.experiment)
        }

        // Q19: `./gradlew :dependencies-lab:compileCommonMainKotlinMetadata -Pq19.showError=true`
        // adds src/q19Error to commonMain and reproduces the lecture's red asExecutor() call.
        if (providers.gradleProperty("q19.showError").orNull == "true") {
            commonMain { kotlin.srcDir("src/q19Error/kotlin") }
        }
    }
}

// Q20: prints the coroutines version Gradle actually resolved (not the one typed in the build script).
//  1. per source set: the version the IDE and the shared-code compiler see. KGP aligns these
//     through one *Main graph (allSourceSetsCompileDependenciesMetadata) and one *Test graph.
//  2. per platform compilation: the classpath handed to the compiler for that target.
tasks.register("printCoroutinesVersions") {
    group = "kmp practice"
    description = "Prints the resolved kotlinx-coroutines-core version per source set and per compilation."
    fun coroutinesIn(configurationName: String): String =
        configurations.getByName(configurationName).incoming.resolutionResult.allComponents
            .mapNotNull { it.moduleVersion }
            .filter { it.group == "org.jetbrains.kotlinx" && it.name == "kotlinx-coroutines-core" }
            .map { it.version }
            .distinct()
            .ifEmpty { listOf("-") }
            .joinToString()

    val report = provider {
        val bySourceSet = kotlin.sourceSets
            .sortedWith(compareBy({ it.name.endsWith("Test") }, { it.name }))
            .joinToString("\n") { "  %-24s %s".format(it.name, coroutinesIn("${it.name}ResolvableDependenciesMetadata")) }
        val byCompilation = kotlin.targets
            .filter { it.name != "metadata" }
            .flatMap { target -> target.compilations.map { "${target.name}/${it.name}" to it.compileDependencyConfigurationName } }
            .sortedBy { it.first }
            .joinToString("\n") { (label, configuration) -> "  %-24s %s".format(label, coroutinesIn(configuration)) }
        "kotlinx-coroutines-core per SOURCE SET (*Main and *Test aligned separately):\n$bySourceSet\n\n" +
            "kotlinx-coroutines-core per platform COMPILATION classpath:\n$byCompilation"
    }
    doLast { println(report.get()) }
}
