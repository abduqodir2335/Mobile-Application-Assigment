package com.asaddeving.advancedlab

// Q17: with manual dependsOn there is no shared iosMain, so each iOS leaf needs its own actual.
actual fun platformName(): String = "iOS (iosSimulatorArm64)"
