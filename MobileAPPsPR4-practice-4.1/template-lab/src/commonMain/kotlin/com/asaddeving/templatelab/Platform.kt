package com.asaddeving.templatelab

expect fun platformName(): String

fun greet(): String = "Hello from ${platformName()}"
