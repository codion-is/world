plugins {
    `java-library`
    id("world.jasperreports.modules")
    id("world.spotless.plugin")
    id("io.github.f-cramer.jasperreports") version "0.0.3"
}

dependencies {
    implementation(project(":world-domain-api"))

    implementation(libs.codion.framework.json.domain)
    implementation(libs.codion.swing.framework.ui)
    implementation(libs.codion.plugin.jasperreports) {
        exclude(group = "org.apache.xmlgraphics")
    }

    implementation(libs.json)
    implementation(libs.jfreechart)
    implementation(libs.flatlaf)
    implementation(libs.flatlaf.intellij.themes)
    implementation(libs.ikonli.foundation)

    implementation(libs.jxmapviewer2) {
        isTransitive = false
    }

    compileOnly(libs.jasperreports.jdt) {
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
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

extraJavaModuleInfo {
    automaticModule("org.eclipse.jdt:ecj", "org.eclipse.jdt.ecj")
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

tasks.test {
    useJUnitPlatform()
    systemProperty("codion.test.user", "scott:tiger")
    systemProperty("codion.db.url", "jdbc:h2:mem:h2db")
    systemProperty("codion.db.initScripts", "classpath:create_schema.sql")
}

tasks.register<WriteProperties>("writeVersion") {
    destinationFile = file("${temporaryDir.absolutePath}/version.properties")
    property("version", libs.versions.codion.get().replace("-SNAPSHOT", ""))
}

tasks.processResources {
    from(tasks.named("writeVersion"))
}