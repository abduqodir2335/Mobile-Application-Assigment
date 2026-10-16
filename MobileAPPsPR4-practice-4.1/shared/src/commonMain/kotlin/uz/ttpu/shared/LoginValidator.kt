package uz.ttpu.shared

// Task 14: the login rules are written once and used by Android (ViewModel) and iOS (SwiftUI).

/** Result of checking a login form. A `null` error means that field is valid. */
data class LoginValidationResult(
    val emailError: String?,
    val passwordError: String?,
) {
    val isValid: Boolean get() = emailError == null && passwordError == null
}

class LoginValidator {

    fun validate(email: String, password: String): LoginValidationResult =
        LoginValidationResult(
            emailError = validateEmail(email),
            passwordError = validatePassword(password),
        )

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !isValidEmail(email) -> "Enter a valid email address"
        else -> null
    }

    private fun validatePassword(password: String): String? = when {
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        password.none { it.isDigit() } -> "Password must contain a digit"
        password.none { it.isLetter() } -> "Password must contain a letter"
        else -> null
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
