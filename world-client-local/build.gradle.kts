import org.gradle.internal.os.OperatingSystem

plugins {
    id("org.beryx.jlink")
    id("world.jasperreports.modules")
    id("world.spotless.plugin")
    id("com.github.breadmoirai.github-release")
}

dependencies {
    implementation(project(":world-client"))

    runtimeOnly(project(":world-domain"))

    runtimeOnly(libs.codion.framework.db.local)
    runtimeOnly(libs.codion.dbms.h2)
    runtimeOnly(libs.h2)
}

application {
    mainModule = "is.codion.demos.world.client"
    mainClass = "is.codion.demos.world.ui.WorldAppPanel"
    applicationDefaultJvmArgs = listOf(
        "-Xmx128m",
        "-Dcodion.client.connectionType=local",
        "-Dcodion.db.url=jdbc:h2:mem:h2db",
        "-Dcodion.db.initScripts=classpath:create_schema.sql",
        "-Dsun.awt.disablegrab=true"
    )
}

jlink {
    imageName = project.name + "-" + project.version + "-" +
            OperatingSystem.current().familyName.replace(" ", "").lowercase()
    moduleName = application.mainModule
    options = listOf(
        "--strip-debug",
        "--no-header-files",
        "--no-man-pages",
        "--add-modules",
        "jdk.crypto.ec,is.codion.framework.db.local,is.codion.dbms.h2," +
                "is.codion.plugin.logback.proxy,is.codion.demos.world.domain"
    )

    addExtraDependencies("slf4j-api")

    jpackage {
        if (OperatingSystem.current().isLinux) {
            icon = "../world.png"
            installerType = "deb"
            installerOptions = listOf(
                "--linux-shortcut"
            )
        }
        if (OperatingSystem.current().isWindows) {
            icon = "../world.ico"
            installerType = "msi"
            installerOptions = listOf(
                "--win-menu",
                "--win-shortcut"
            )
        }
        if (OperatingSystem.current().isMacOsX) {
            icon = "../world.icns"
            installerType = "dmg"
        }
    }
}