import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Convention plugin (Lecture 7: build logic) shared by every lab module.
// Adds two read-only report tasks that print what the Kotlin Gradle plugin built from build.gradle.kts:
//   printSourceSets  -> each source set, its dependsOn edges and the targets it compiles to (Q16, Q17)
//   printCompilations -> each target and its compilations (Q21)

val kotlinExtension = extensions.getByType<KotlinMultiplatformExtension>()

tasks.register("printSourceSets") {
    group = "kmp practice"
    description = "Prints every source set, its dependsOn edges and the targets it compiles to."
    // Build the report lazily but in the configuration phase, so the task action only prints strings.
    val report = provider {
        val targetsBySourceSet = mutableMapOf<String, MutableSet<String>>()
        kotlinExtension.targets
            .filter { it.name != "metadata" }
            .forEach { target ->
                target.compilations.forEach { compilation ->
                    compilation.allKotlinSourceSets.forEach { sourceSet ->
                        targetsBySourceSet.getOrPut(sourceSet.name) { sortedSetOf() } += target.name
                    }
                }
            }
        kotlinExtension.sourceSets.sortedBy { it.name }.joinToString("\n") { sourceSet ->
            val parents = sourceSet.dependsOn.map { it.name }.sorted().ifEmpty { listOf("-") }
            val targets = targetsBySourceSet[sourceSet.name]?.joinToString().orEmpty().ifEmpty { "NONE (not compiled)" }
            "%-24s dependsOn: %-28s targets: %s".format(sourceSet.name, parents.joinToString(), targets)
        }
    }
    doLast { println(report.get()) }
}

tasks.register("printCompilations") {
    group = "kmp practice"
    description = "Prints every target and the compilations Kotlin registered for it."
    val report = provider {
        kotlinExtension.targets.sortedBy { it.name }.joinToString("\n") { target ->
            val compilations = target.compilations.joinToString { compilation ->
                "${compilation.name} (default source set: ${compilation.defaultSourceSet.name})"
            }
            "%-20s -> %s".format(target.name, compilations)
        }
    }
    doLast { println(report.get()) }
}
