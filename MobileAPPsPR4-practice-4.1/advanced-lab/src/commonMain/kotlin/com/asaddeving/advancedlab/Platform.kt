package com.asaddeving.advancedlab

/** Name of the platform the code is running on. Every target provides its own `actual`. */
expect fun platformName(): String

/** Common code: compiled for every target (JVM, JS, Linux, iOS). */
fun greet(): String = "Hello from ${platformName()}"
