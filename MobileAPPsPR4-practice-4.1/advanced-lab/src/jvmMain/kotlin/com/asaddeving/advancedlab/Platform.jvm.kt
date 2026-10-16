package com.asaddeving.advancedlab

actual fun platformName(): String = "JVM ${System.getProperty("java.version")}"

/** jvmMain depends on jvmAndJsMain, so it can call [formatPrice] directly. */
fun jvmReceipt(cents: Long): String = "JVM receipt: ${formatPrice(cents)}"
