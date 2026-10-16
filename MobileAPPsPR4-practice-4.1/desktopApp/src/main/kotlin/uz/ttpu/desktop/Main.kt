package uz.ttpu.desktop

import uz.ttpu.shared.LoginValidator
import uz.ttpu.shared.greeting

/** Desktop client: `./gradlew :desktopApp:run --args="student@ttpu.uz secret123"`. */
fun main(args: Array<String>) {
    println(greeting())
    val email = args.getOrElse(0) { "" }
    val password = args.getOrElse(1) { "" }
    println(describeLogin(email, password))
}

fun describeLogin(email: String, password: String): String {
    val result = LoginValidator().validate(email, password)
    return if (result.isValid) {
        "Login OK for $email"
    } else {
        listOfNotNull(result.emailError, result.passwordError).joinToString(prefix = "Login rejected: ")
    }
}
