plugins {
    id("org.beryx.jlink")
    id("world.jasperreports.modules")
    id("world.spotless.plugin")
}

dependencies {
    implementation(project(":world-client"))

    runtimeOnly(libs.codion.framework.db.http)
}

val serverHost: String by project
val serverHttpPort: String by project

application {
    mainModule.set("is.codion.framework.demos.world.client")
    mainClass.set("is.codion.framework.demos.world.ui.WorldAppPanel")

    applicationDefaultJvmArgs = listOf(
        "-Xmx128m",
        "-Dcodion.client.connectionType=http",
        "-Dcodion.client.http.secure=false",
        "-Dcodion.client.http.hostname=${serverHost}",
        "-Dcodion.client.http.port=${serverHttpPort}",
        "-Dsun.awt.disablegrab=true"
    )
}

jlink {
    imageName.set(project.name)
    moduleName.set(application.mainModule)
    options.set(
        listOf(
            "--strip-debug",
            "--no-header-files",
            "--no-man-pages",
            "--add-modules",
            "jdk.crypto.ec,is.codion.framework.db.http,is.codion.plugin.logback.proxy"
        )
    )

    jpackage {
        imageName = "World-Http"
        if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            installerType = "deb"
            icon = "../world.png"
            installerOptions = listOf(
                "--resource-dir",
                "build/jpackage/World-Http/lib",
                "--linux-shortcut"
            )
        }
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerType = "msi"
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