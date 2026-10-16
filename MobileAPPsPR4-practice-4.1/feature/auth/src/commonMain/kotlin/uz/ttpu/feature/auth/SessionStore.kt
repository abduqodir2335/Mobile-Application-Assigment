package uz.ttpu.feature.auth

/** Who is logged in. App-wide state: there must be exactly ONE of these in the app (Task 18). */
object SessionStore {
    var currentUser: String? = null
        private set

    fun logIn(email: String) {
        currentUser = email
    }

    fun logOut() {
        currentUser = null
    }
}

class AuthService {
    fun logIn(email: String) = SessionStore.logIn(email)

    fun logOut() = SessionStore.logOut()
}
