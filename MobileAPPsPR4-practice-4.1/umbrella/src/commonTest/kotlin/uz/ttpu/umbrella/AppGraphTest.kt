package uz.ttpu.umbrella

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppGraphTest {
    private val graph = AppGraph()

    @AfterTest
    fun logOut() = graph.auth.logOut()

    @Test
    fun oneCopyOfSessionStoreIsSharedByBothFeatures() {
        graph.auth.logIn("student@ttpu.uz")
        assertEquals("Profile of student@ttpu.uz", graph.profile.title())
    }
}
