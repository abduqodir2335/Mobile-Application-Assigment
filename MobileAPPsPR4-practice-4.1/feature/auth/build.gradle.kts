plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Feature module (Task 18-20, 30). jvm() stands in for the Android/desktop side; iOS gets it
// ONLY through the umbrella framework, never as its own framework.
kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
}
