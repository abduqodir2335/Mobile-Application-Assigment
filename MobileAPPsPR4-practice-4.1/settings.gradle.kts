rootProject.name = "KmpAdvancedProjectStructure"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

// Practice 4: Questions 14-25
include(":advanced-lab")      // Q16, Q17, Q21: custom jvmAndJsMain source set + compilations
include(":template-lab")      // Q17: same targets, no manual dependsOn (default hierarchy template)
include(":dependencies-lab")  // Q18, Q19, Q20: propagation, compatibility, version alignment

// Practice 4.1: Tasks 1-30 (see PRACTICE_4.1.md)
include(":shared")            // Task 17: one KMP module shared by both apps
include(":androidApp")
include(":desktopApp")
include(":feature:auth")      // Tasks 18-20, 30: feature modules ...
include(":feature:profile")
include(":umbrella")          // ... and the umbrella module that builds the single iOS framework
