import SwiftUI
import Shared

struct LoginView: View {
    @StateObject private var viewModel = LoginViewModel()

    var body: some View {
        Form {
            Text(PlatformKt.greeting()) // top-level Kotlin functions live in <FileName>Kt classes
            TextField("Email", text: $viewModel.email)
                .textInputAutocapitalization(.never)
                .keyboardType(.emailAddress)
            if let error = viewModel.emailError {
                Text(error).foregroundColor(.red)
            }
            SecureField("Password", text: $viewModel.password)
            if let error = viewModel.passwordError {
                Text(error).foregroundColor(.red)
            }
            Button("Log in", action: viewModel.logIn)
            if viewModel.isLoggedIn {
                Text("Welcome!").foregroundColor(.green)
            }
        }
    }
}
