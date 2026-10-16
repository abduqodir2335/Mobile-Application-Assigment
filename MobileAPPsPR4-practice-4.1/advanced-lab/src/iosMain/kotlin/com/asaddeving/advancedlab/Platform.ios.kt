package com.asaddeving.advancedlab

// Q17: this directory is the conventional iosMain source set. Because the build script uses manual
// dependsOn, the default hierarchy template is NOT applied, so Kotlin does not create iosMain and
// does not compile this file (see `./gradlew :advanced-lab:printSourceSets` and ANSWERS.md, Q17).
// The iOS actuals that ARE compiled live in iosArm64Main and iosSimulatorArm64Main.
actual fun platformName(): String = "iOS"
