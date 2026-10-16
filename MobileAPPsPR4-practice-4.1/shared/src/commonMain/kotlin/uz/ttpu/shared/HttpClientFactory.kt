package uz.ttpu.shared

import io.ktor.client.HttpClient

// Task 9: common code only sees ktor-client-core. HttpClient() picks the engine that is on the
// platform's classpath: OkHttp on Android and desktop, Darwin (NSURLSession) on iOS.
fun createHttpClient(): HttpClient = HttpClient()
