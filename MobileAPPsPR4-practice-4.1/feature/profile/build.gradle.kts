plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // profile reads the session that auth writes: both must see the same SessionStore.
            implementation(project(":feature:auth"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
