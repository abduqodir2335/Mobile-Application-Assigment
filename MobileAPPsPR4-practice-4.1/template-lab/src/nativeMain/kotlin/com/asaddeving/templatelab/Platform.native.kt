package com.asaddeving.templatelab

// One actual for linuxX64 + both iOS targets: nativeMain exists because the template is applied.
actual fun platformName(): String = "Native (${nativeFamily()})"

internal expect fun nativeFamily(): String
