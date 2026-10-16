package uz.ttpu.android

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.ttpu.shared.LoginValidator

/** What the login screen draws. Errors are shown only after the user presses "Log in". */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoggedIn: Boolean = false,
)

// Task 14 (Android): the ViewModel holds UI state; the rules come from shared LoginValidator.
class LoginViewModel(
    private val validator: LoginValidator = LoginValidator(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, emailError = null) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, passwordError = null) }

    fun onLoginClick() = _uiState.update { state ->
        val result = validator.validate(state.email, state.password)
        state.copy(
            emailError = result.emailError,
            passwordError = result.passwordError,
            isLoggedIn = result.isValid,
        )
    }
}
