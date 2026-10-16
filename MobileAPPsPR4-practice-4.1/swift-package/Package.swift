// swift-tools-version:5.9
// Task 25: remote (artifact) distribution of the Kotlin XCFramework through Swift Package Manager.
// The zip is produced by `./gradlew :shared:assembleSharedXCFramework` and uploaded to a release;
// the checksum comes from `swift package compute-checksum Shared.xcframework.zip`.
import PackageDescription

let package = Package(
    name: "Shared",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "Shared", targets: ["Shared"]),
    ],
    targets: [
        .binaryTarget(
            name: "Shared",
            url: "https://github.com/AsadDeving/MobileAPPsPR4/releases/download/1.0.0/Shared.xcframework.zip",
            checksum: "<output of: swift package compute-checksum Shared.xcframework.zip>"
        ),
    ]
)
