import org.gradle.internal.os.OperatingSystem

plugins {
    id("org.beryx.jlink")
    id("com.github.breadmoirai.github-release")
}

dependencies {
    runtimeOnly(libs.codion.framework.server)
    runtimeOnly(libs.codion.plugin.hikari.pool)
    runtimeOnly(libs.codion.dbms.h2)
    runtimeOnly(libs.h2)

    runtimeOnly(project(":world-domain"))

    //logging library, skipping the email stuff
    runtimeOnly(libs.codion.plugin.logback.proxy) {
        exclude(group = "com.sun.mail", module = "javax.mail")
    }
}

val serverHost: String by project
val serverRegistryPort: String by project
val serverPort: String by project
val serverHttpPort: String by project
val serverAdminPort: String by project

application {
    mainModule = "is.codion.framework.server"
    mainClass = "is.codion.framework.server.EntityServer"
    applicationDefaultJvmArgs = listOf(
        "-Xmx256m",
        "-Dlogback.configurationFile=logback.xml",
        //RMI configuration
        "-Djava.rmi.server.hostname=${serverHost}",
        "-Dcodion.server.registryPort=${serverRegistryPort}",
        "-Djava.rmi.server.randomIDs=true",
        "-Djava.rmi.server.useCodebaseOnly=true",
        //The serialization whitelist
        "-Dcodion.server.objectInputFilterFactory=is.codion.common.rmi.server.SerializationFilterFactory",
        "-Dcodion.server.serialization.filter.patternFile=classpath:serialization-whitelist.txt",
        //SSL configuration
        "-Dcodion.server.classpathKeyStore=keystore.jks",
        "-Djavax.net.ssl.keyStorePassword=crappypass",
        //The port used by clients
        "-Dcodion.server.port=${serverPort}",
        //The port for the admin interface, used by the server monitor
        "-Dcodion.server.admin.port=${serverAdminPort}",
        //The admin user credentials, used by the server monitor application
        "-Dcodion.server.admin.user=scott:tiger",
        //Database configuration
        "-Dcodion.db.url=jdbc:h2:mem:h2db",
        "-Dcodion.db.initScripts=classpath:create_schema.sql",
        //A connection pool based on this user is created on startup
        "-Dcodion.server.connectionPoolUsers=scott:tiger",
        //Client logging disabled by default
        "-Dcodion.server.clientLogging=false"
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
        "--ignore-signing-information",
        "--add-modules",
        "is.codion.framework.db.local,is.codion.dbms.h2,is.codion.plugin.hikari.pool," +
                "is.codion.plugin.logback.proxy,is.codion.demos.world.domain,is.codion.framework.servlet"
    )

    addExtraDependencies("slf4j-api")

    mergedModule {
        excludeRequires("jetty.servlet.api")
    }

    forceMerge("kotlin")

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
            imageOptions = imageOptions + listOf("--win-console")
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

if (properties.containsKey("githubAccessToken")) {
    githubRelease {
        token(properties["githubAccessToken"] as String)
        owner = "codion-is"
        repo = "world"
        allowUploadToExisting = true
        releaseAssets.from(tasks.named("jlinkZip").get().outputs.files)
        releaseAssets.from(fileTree(tasks.named("jpackage").get().outputs.files.singleFile) {
            exclude(project.name + "/**", project.name + ".app/**")
        })
    }
}