import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    id("kmp-structure-report") // Task 7: ./gradlew :shared:printSourceSets
    `maven-publish`            // Task 24
}

// Task 24: Maven coordinates -> uz.ttpu.shared:shared:1.0.0
group = "uz.ttpu.shared"
version = "1.0.0"

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
        publishLibraryVariants("release") // Task 24: publish the Android AAR too
    }

    jvm() // Task 4: desktop (JVM)

    // Task 25: one XCFramework named "Shared" containing both iOS architectures.
    val xcFramework = XCFramework("Shared")
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            xcFramework.add(this)
        }
    }

    // Task 9: one HTTP API (Ktor) in common code, one engine per platform.
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp) // desktop needs an engine too
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "uz.ttpu.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Local JVM unit tests run against a stub android.jar; let Build.* return defaults there.
    testOptions.unitTests.isReturnDefaultValues = true
}

// Task 24: the Kotlin plugin already creates one publication per target; this adds the repository.
publishing {
    repositories {
        mavenLocal()
    }
}
