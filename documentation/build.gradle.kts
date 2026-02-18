plugins {
    id("org.asciidoctor.jvm.convert") version "4.0.4"
}

version = libs.versions.codion.get().replace("-SNAPSHOT", "")

tasks.asciidoctor {
    dependsOn(rootProject.subprojects.map { it.tasks.build })
    rootProject.subprojects.forEach { subproject ->
        inputs.file(subproject.buildFile)
        inputs.files(subproject.sourceSets.main.get().allSource)
        inputs.files(subproject.sourceSets.test.get().allSource)
    }

    baseDirFollowsSourceFile()

    attributes(
        mapOf(
            "codion-version" to project.version,
            "source-highlighter" to "rouge",
            "tabsize" to 2
        )
    )
    asciidoctorj {
        setVersion("2.5.13")
    }
}