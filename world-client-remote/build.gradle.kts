plugins {
    id("org.beryx.jlink")
    id("world.jasperreports.modules")
    id("world.spotless.plugin")
}

dependencies {
    implementation(project(":world-client"))

    runtimeOnly(libs.codion.framework.db.rmi)
}

val serverHost: String by project
val serverRegistryPort: String by project

application {
    mainModule = "is.codion.demos.world.client"
    mainClass = "is.codion.demos.world.ui.WorldAppPanel"
    applicationDefaultJvmArgs = listOf(
        "-Xmx128m",
        "-Dcodion.client.connectionType=remote",
        "-Dcodion.server.hostname=${serverHost}",
        "-Dcodion.server.registryPort=${serverRegistryPort}",
        "-Dcodion.client.trustStore=truststore.jks",
        "-Dcodion.client.trustStorePassword=crappypass",
        "-Dsun.awt.disablegrab=true"
    )
}

jlink {
    imageName = project.name
    moduleName = application.mainModule
    options = listOf(
        "--strip-debug",
        "--no-header-files",
        "--no-man-pages",
        "--add-modules",
        "jdk.crypto.ec,is.codion.framework.db.rmi,is.codion.plugin.logback.proxy"
    )

    mergedModule {
        excludeRequires("com.fasterxml.jackson.annotation")
    }

    jpackage {
        if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            icon = "../world.png"
            installerOptions = listOf(
                "--linux-shortcut"
            )
        }
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            icon = "../world.ico"
            installerOptions = listOf(
                "--win-menu",
                "--win-shortcut"
            )
        }
    }
}

tasks.prepareMergedJarsDir {
    doLast {
        copy {
            from("src/main/resources")
            into("build/jlinkbase/mergedjars")
        }
        //https://github.com/TIBCOSoftware/jasperreports/issues/463
        project.delete(files("build/jlinkbase/mergedjars/net/sf/jasperreports/fonts"))
    }
}