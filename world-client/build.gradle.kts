plugins {
    `java-library`
    id("world.jasperreports.modules")
    id("world.spotless.plugin")
    id("io.github.f-cramer.jasperreports") version "0.0.4"
}

dependencies {
    implementation(project(":world-domain-api"))

    implementation(libs.codion.framework.json.domain)
    implementation(libs.codion.swing.framework.ui)
    implementation(libs.codion.plugin.flatlaf.lookandfeels)
    implementation(libs.codion.plugin.flatlaf.intellij.themes)
    implementation(libs.codion.plugin.jasperreports) {
        exclude(group = "org.apache.xmlgraphics")
    }

    implementation(libs.json)
    implementation(libs.jfreechart)

    implementation(libs.jxmapviewer2) {
        isTransitive = false
    }

    jasperreportsClasspath(libs.jasperreports.jdt) {
        exclude(group = "net.sf.jasperreports")
    }
    implementation(libs.jasperreports.pdf) {
        exclude(group = "net.sf.jasperreports")
    }
    implementation(libs.jasperreports.fonts)

    runtimeOnly(libs.codion.plugin.logback.proxy)

    testImplementation(project(":world-domain"))
    testImplementation(libs.codion.framework.db.local)
    testRuntimeOnly(libs.codion.dbms.h2)
    testRuntimeOnly(libs.h2)
}

jasperreports {
    classpath.from(
        project.sourceSets.main.get().java.classesDirectory,
        configurations.named(project.sourceSets.main.get().compileClasspathConfigurationName)
    )
}

sourceSets.main {
    resources.srcDir(tasks.compileAllReports)
}

tasks.register<WriteProperties>("writeVersion") {
    destinationFile = file("${temporaryDir.absolutePath}/version.properties")
    property("version", libs.versions.codion.get().replace("-SNAPSHOT", ""))
}

tasks.processResources {
    from(tasks.named("writeVersion"))
}