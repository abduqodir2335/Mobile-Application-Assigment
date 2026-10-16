package uz.ttpu.feature.profile

import uz.ttpu.feature.auth.SessionStore

class ProfileService {
    fun title(): String = SessionStore.currentUser?.let { "Profile of $it" } ?: "Not logged in"
}
