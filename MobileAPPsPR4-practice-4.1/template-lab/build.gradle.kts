import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("kmp-structure-report")
}

kotlin {
    // Q17 control group: the SAME five targets as advanced-lab, but no manual dependsOn calls.
    jvm()
    js(IR) { browser() }
    linuxX64()
    iosArm64()
    iosSimulatorArm64()

    // Q17 fix: instead of manual dependsOn, EXTEND the default hierarchy template with a custom group.
    // Result: jvmAndJsMain exists AND the template still creates nativeMain, appleMain, iosMain, linuxMain...
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("jvmAndJs") {
                withJvm()
                withJs()
            }
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
