# iosApp (Swift side of Practice 4.1)

SwiftUI files that consume the `Shared` framework built from [`:shared`](../shared):

- [`LoginViewModel.swift`](iosApp/LoginViewModel.swift): Task 14, a SwiftUI view model that calls the Kotlin `LoginValidator`.
- [`LoginView.swift`](iosApp/LoginView.swift): the screen that uses it.

To run them, create an iOS App project in Xcode, add these two files, and link the framework:

```bash
./gradlew :shared:assembleSharedXCFramework   # -> shared/build/XCFrameworks/release/Shared.xcframework
```

Drag `Shared.xcframework` into the target's *Frameworks, Libraries, and Embedded Content* (local/source
distribution), or consume it remotely with the Swift package in [`../swift-package`](../swift-package) (Task 25).
