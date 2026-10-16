package uz.ttpu.shared

// Task 12: pure Kotlin, so it lives in commonMain and is tested once in commonTest.

private val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

fun isValidEmail(text: String): Boolean = EMAIL_PATTERN.matches(text.trim())
