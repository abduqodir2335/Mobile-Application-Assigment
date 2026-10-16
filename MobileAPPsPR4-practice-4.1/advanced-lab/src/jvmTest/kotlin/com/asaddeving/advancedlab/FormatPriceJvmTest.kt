package com.asaddeving.advancedlab

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FormatPriceJvmTest {
    @Test
    fun formatsCentsWithTwoDigits() {
        assertTrue(formatPrice(1999).startsWith("$19.99"))
        assertTrue(formatPrice(5).startsWith("$0.05"))
    }

    @Test
    fun jvmCodeCanCallTheJvmAndJsSourceSet() {
        assertEquals("JVM receipt: ${formatPrice(100)}", jvmReceipt(100))
    }

    @Test
    fun rejectsNegativeAmounts() {
        assertFailsWith<IllegalArgumentException> { formatPrice(-1) }
    }
}
