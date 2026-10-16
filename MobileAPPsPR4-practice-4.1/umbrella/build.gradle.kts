plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

// Task 20: `-Ptask20.broken=true` rebuilds the framework the way the task's broken snippet does
// (implementation(), no export()), so the generated header can be compared with the fixed one.
val reproduceTask20Bug = providers.gradleProperty("task20.broken").orNull == "true"

// Task 19: the umbrella module. Its only job is to depend on every feature module and build
// the ONE static iOS framework, "Shared", that the iOS app links.
kotlin {
    jvm()

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            if (!reproduceTask20Bug) {
                // export() puts the features' public API into the framework's Swift/Obj-C header.
                export(project(":feature:auth"))
                export(project(":feature:profile"))
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            if (reproduceTask20Bug) {
                implementation(project(":feature:auth"))
                implementation(project(":feature:profile"))
            } else {
                // export() needs api(): an implementation() dependency is compiled into the
                // framework but stays invisible to Swift (Task 20).
                api(project(":feature:auth"))
                api(project(":feature:profile"))
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
