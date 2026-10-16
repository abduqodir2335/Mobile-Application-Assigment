plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("kmp-structure-report")
}

kotlin {
    // Q16: the lecture's flagship example: JVM + JS + Linux.
    jvm {
        // Q21: reach a specific compilation through its target: target -> compilations -> name.
        val mainCompilation = compilations.getByName("main")
        val testCompilation = compilations.getByName("test")
        println("[Q21] jvm compilations: main = '${mainCompilation.name}', test = '${testCompilation.name}'")
    }
    js(IR) { browser() }
    linuxX64()

    // Q17: two Apple targets added to the Q16 project.
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // Q16: a source set Kotlin has no default for: code shared by JVM and JS only (not Linux, not iOS).
        val jvmAndJsMain by creating {
            // "by creating" registers a NEW source set. Until the dependsOn calls below
            // it has no targets and would never be compiled.
        }
        jvmAndJsMain.dependsOn(commonMain.get())
        jvmMain.get().dependsOn(jvmAndJsMain)
        jsMain.get().dependsOn(jvmAndJsMain)

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

// Q21: one small task that prints the main compilation of the jvm target (lecture access pattern).
tasks.register("printJvmMainCompilation") {
    group = "kmp practice"
    description = "Prints kotlin.jvm().compilations.getByName(\"main\") and its source sets."
    val report = provider {
        val main = kotlin.jvm().compilations.getByName("main")
        "jvm main compilation: name='${main.name}', " +
            "source sets=${main.allKotlinSourceSets.map { it.name }.sorted()}"
    }
    doLast { println(report.get()) }
}
