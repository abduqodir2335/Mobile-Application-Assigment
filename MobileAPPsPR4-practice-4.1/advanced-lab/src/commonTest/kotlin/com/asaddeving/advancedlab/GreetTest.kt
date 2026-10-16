package com.asaddeving.advancedlab

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetTest {
    @Test
    fun greetingNamesThePlatform() {
        assertTrue(greet().startsWith("Hello from "))
        assertTrue(greet().endsWith(platformName()))
    }
}
