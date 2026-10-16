package com.asaddeving.dependencieslab

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking

// Q19: NOT compiled by default. Enabled with -Pq19.showError=true to reproduce the lecture screenshot.
fun q19Screenshot() {
    val dispatcher = Dispatchers.Main // OK: coroutines commonMain
    runBlocking { }                   // OK: coroutines concurrentMain
    dispatcher.asExecutor()           // ERROR: Unresolved reference 'asExecutor' (JVM-only source set)
}
