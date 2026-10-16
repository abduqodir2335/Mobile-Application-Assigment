package uz.ttpu.umbrella

import uz.ttpu.feature.auth.AuthService
import uz.ttpu.feature.profile.ProfileService

// Umbrella modules are a natural home for app-wide wiring (manual DI) that belongs to no single feature.
class AppGraph {
    val auth: AuthService by lazy { AuthService() }
    val profile: ProfileService by lazy { ProfileService() }
}
