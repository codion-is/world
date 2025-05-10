import org.gradle.internal.os.OperatingSystem

plugins {
    id("org.beryx.jlink")
    id("com.github.breadmoirai.github-release")
}

dependencies {
    runtimeOnly(libs.codion.tools.monitor.ui)
    runtimeOnly(libs.codion.plugin.logback.proxy)
}

val serverHost: String by project
val serverRegistryPort: String by project

application {
    mainModule = "is.codion.tools.monitor.ui"
    mainClass = "is.codion.tools.monitor.ui.EntityServerMonitorPanel"
    applicationDefaultJvmArgs = listOf(
        "-Xmx512m",
        "-Dcodion.server.hostname=${serverHost}",
        "-Dcodion.server.registryPort=${serverRegistryPort}",
        "-Dcodion.client.trustStore=truststore.jks",
        "-Dcodion.client.trustStorePassword=crappypass",
        "-Dlogback.configurationFile=logback.xml",
        "-Dcodion.server.admin.user=scott:tiger"
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
        "java.naming,is.codion.plugin.logback.proxy"
    )

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

tasks.prepareMergedJarsDir {
    doLast {
        copy {
            from("src/main/resources")
            into("build/jlinkbase/mergedjars")
        }
    }
}