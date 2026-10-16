package uz.ttpu.android

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginViewModelTest {
    private val viewModel = LoginViewModel()

    @Test
    fun invalidInputShowsSharedErrorMessages() {
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("short")
        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertEquals("Enter a valid email address", state.emailError)
        assertEquals("Password must be at least 8 characters", state.passwordError)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun validInputLogsIn() {
        viewModel.onEmailChange("student@ttpu.uz")
        viewModel.onPasswordChange("secret123")
        viewModel.onLoginClick()

        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertTrue(state.isLoggedIn)
    }

    @Test
    fun typingClearsTheError() {
        viewModel.onLoginClick()
        viewModel.onEmailChange("s")
        assertNull(viewModel.uiState.value.emailError)
    }
}
