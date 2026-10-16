package com.asaddeving.advancedlab

actual fun platformName(): String = "JS"

/** jsMain depends on jvmAndJsMain, so it can call [formatPrice] directly. */
fun jsReceipt(cents: Long): String = "JS receipt: ${formatPrice(cents)}"
