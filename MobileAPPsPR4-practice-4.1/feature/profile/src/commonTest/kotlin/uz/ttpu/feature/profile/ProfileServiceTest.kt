package uz.ttpu.feature.profile

import uz.ttpu.feature.auth.AuthService
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileServiceTest {
    @AfterTest
    fun logOut() = AuthService().logOut()

    @Test
    fun profileSeesTheSessionWrittenByAuth() {
        AuthService().logIn("student@ttpu.uz")
        assertEquals("Profile of student@ttpu.uz", ProfileService().title())
    }

    @Test
    fun loggedOutByDefault() {
        assertEquals("Not logged in", ProfileService().title())
    }
}
