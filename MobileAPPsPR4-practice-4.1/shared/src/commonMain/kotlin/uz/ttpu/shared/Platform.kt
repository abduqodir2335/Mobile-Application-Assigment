package uz.ttpu.shared

// Task 4: one common declaration, one actual per platform (androidMain, iosMain, jvmMain).

/** Describes the platform the shared code is running on. */
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

fun greeting(): String = "Hello from ${getPlatform().name}"
