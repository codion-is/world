plugins {
    id("org.beryx.jlink")
    id("world.jasperreports.modules")
    id("world.spotless.plugin")
}

dependencies {
    implementation(project(":world-client"))

    runtimeOnly(project(":world-domain"))

    runtimeOnly(libs.codion.framework.db.local)
    runtimeOnly(libs.codion.dbms.h2)
    runtimeOnly(libs.h2)
}

application {
    mainModule.set("is.codion.framework.demos.world.client")
    mainClass.set("is.codion.framework.demos.world.ui.WorldAppPanel")

    applicationDefaultJvmArgs = listOf(
        "-Xmx128m",
        "-Dcodion.client.connectionType=local",
        "-Dcodion.db.url=jdbc:h2:mem:h2db",
        "-Dcodion.db.initScripts=classpath:create_schema.sql",
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
            "jdk.crypto.ec,is.codion.framework.db.local,is.codion.dbms.h2," +
                    "is.codion.plugin.logback.proxy,is.codion.framework.demos.world.domain"
        )
    )

    addExtraDependencies("slf4j-api")

    launcher {
        jvmArgs.addAll(application.applicationDefaultJvmArgs)
    }

    jpackage {
        imageName = "World-Local"
        if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            installerType = "deb"
            icon = "../world.png"
            installerOptions = listOf(
                "--resource-dir",
                "build/jpackage/World-Local/lib",
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