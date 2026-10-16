package com.asaddeving.dependencieslab

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

// Q19: the lecture's IntelliJ screenshot, as code that compiles.
// commonMain here compiles to jvm (stand-in for android), iosArm64 and iosSimulatorArm64.

/** Resolves from coroutines' own commonMain, which compiles to every target. */
fun mainDispatcher(): CoroutineDispatcher = Dispatchers.Main

/** Resolves from coroutines' concurrentMain (every target except JS), which still covers jvm + iOS. */
fun <T> blockingCall(block: suspend () -> T): T = runBlocking { block() }

// dispatcher.asExecutor() would NOT resolve here: it lives in coroutines' JVM-only source set,
// which does not cover iOS. See src/q19Error and jvmMain/ExecutorBridge.kt.
