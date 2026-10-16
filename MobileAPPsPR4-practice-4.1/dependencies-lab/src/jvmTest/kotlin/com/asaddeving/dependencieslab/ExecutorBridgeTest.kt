package com.asaddeving.dependencieslab

import kotlinx.coroutines.Dispatchers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertTrue

class ExecutorBridgeTest {
    @Test
    fun dispatcherRunsWorkAsJavaExecutor() {
        val done = CountDownLatch(1)
        Dispatchers.Default.toJavaExecutor().execute { done.countDown() }
        assertTrue(done.await(5, TimeUnit.SECONDS))
    }
}
