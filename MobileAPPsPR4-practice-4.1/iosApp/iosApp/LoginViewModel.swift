import Foundation
import Shared // the static framework built from :shared (./gradlew :shared:assembleSharedXCFramework)

// Task 14 (iOS): a SwiftUI view model. The rules come from the same Kotlin LoginValidator
// that the Android ViewModel uses; Swift only keeps the UI state.
@MainActor
final class LoginViewModel: ObservableObject {
    @Published var email = "" { didSet { emailError = nil } }
    @Published var password = "" { didSet { passwordError = nil } }
    @Published private(set) var emailError: String?
    @Published private(set) var passwordError: String?
    @Published private(set) var isLoggedIn = false

    private let validator = LoginValidator()

    func logIn() {
        // Kotlin: fun validate(email: String, password: String): LoginValidationResult
        let result = validator.validate(email: email, password: password)
        emailError = result.emailError     // Kotlin String? -> Swift String?
        passwordError = result.passwordError
        isLoggedIn = result.isValid
    }
}
