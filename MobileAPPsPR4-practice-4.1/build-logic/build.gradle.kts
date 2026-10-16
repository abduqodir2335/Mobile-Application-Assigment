plugins {
    `kotlin-dsl`
}

dependencies {
    // Compile-only: the plugin reads the KMP model but the real KGP comes from the consuming build.
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}
