package com.asaddeving.advancedlab

// Q16: lives in the custom jvmAndJsMain source set.
// It is compiled for JVM and JS only, so linuxX64Main and the iOS source sets cannot see it.

/** Formats a price the same way on the JVM backend and the JS web client. */
fun formatPrice(cents: Long): String {
    require(cents >= 0) { "cents must not be negative, was $cents" }
    val whole = cents / CENTS_PER_UNIT
    val fraction = (cents % CENTS_PER_UNIT).toString().padStart(2, '0')
    return "$$whole.$fraction (${platformName()})"
}

private const val CENTS_PER_UNIT = 100
