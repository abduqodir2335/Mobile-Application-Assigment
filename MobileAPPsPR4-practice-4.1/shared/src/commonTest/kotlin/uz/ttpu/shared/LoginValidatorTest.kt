package uz.ttpu.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginValidatorTest {
    private val validator = LoginValidator()

    @Test
    fun validCredentialsPass() {
        val result = validator.validate("student@ttpu.uz", "secret123")
        assertTrue(result.isValid)
        assertNull(result.emailError)
        assertNull(result.passwordError)
    }

    @Test
    fun blankEmailIsRequired() {
        assertEquals("Email is required", validator.validate("  ", "secret123").emailError)
    }

    @Test
    fun shortPasswordIsRejected() {
        assertEquals("Password must be at least 8 characters", validator.validate("a@b.uz", "abc1").passwordError)
    }

    @Test
    fun passwordNeedsDigitAndLetter() {
        assertEquals("Password must contain a digit", validator.validate("a@b.uz", "abcdefgh").passwordError)
        assertEquals("Password must contain a letter", validator.validate("a@b.uz", "12345678").passwordError)
    }
}
