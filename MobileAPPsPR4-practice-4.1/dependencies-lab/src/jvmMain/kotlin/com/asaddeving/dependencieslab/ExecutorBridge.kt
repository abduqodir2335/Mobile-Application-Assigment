package com.asaddeving.dependencieslab

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executor

// Q19: asExecutor() is JVM-only API. In jvmMain (JVM target only) coroutines' JVM source set IS
// compatible, so the same call that fails in commonMain compiles fine here.
fun CoroutineDispatcher.toJavaExecutor(): Executor = asExecutor()
