package uz.ttpu.desktop

import kotlin.test.Test
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun validLogin() {
        assertEquals("Login OK for student@ttpu.uz", describeLogin("student@ttpu.uz", "secret123"))
    }

    @Test
    fun invalidLoginListsEveryError() {
        assertEquals(
            "Login rejected: Email is required, Password must contain a digit",
            describeLogin("", "abcdefgh"),
        )
    }
}
