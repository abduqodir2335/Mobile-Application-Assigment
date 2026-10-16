plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    // Task 17: same line as the Android app. Gradle picks the shared module's JVM (desktop) variant.
    implementation(project(":shared"))

    testImplementation(libs.kotlin.test)
}

application {
    mainClass.set("uz.ttpu.desktop.MainKt")
}
