package com.asaddeving.dependencieslab

import kotlinx.coroutines.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockingCallTest {
    @Test
    fun runBlockingIsUsableFromCommonCode() {
        val result = blockingCall {
            delay(1)
            42
        }
        assertEquals(42, result)
    }
}
