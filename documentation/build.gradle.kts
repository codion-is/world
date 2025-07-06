plugins {
    id("org.asciidoctor.jvm.convert") version "4.0.4"
}

version = libs.versions.codion.get().replace("-SNAPSHOT", "")

tasks.asciidoctor {
    inputs.dir("../world-domain-api/src/main/java")
    inputs.dir("../world-domain/src/main/java")
    inputs.dir("../world-client/src/main/java")
    inputs.dir("../world-client/src/main/reports")
    inputs.dir("../world-domain/src/main/resources")
    inputs.dir("../world-domain/src/test/java")
    inputs.dir("../world-client/src/test/java")

    baseDirFollowsSourceFile()

    attributes(
        mapOf(
            "codion-version" to project.version,
            "source-highlighter" to "prettify",
            "tabsize" to 2
        )
    )
    asciidoctorj {
        setVersion("2.5.13")
    }
}