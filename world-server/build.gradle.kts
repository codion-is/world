plugins {
    id("org.beryx.jlink")
}

dependencies {
    runtimeOnly(libs.codion.framework.server)
    runtimeOnly(libs.codion.framework.servlet)
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
        "-Dcodion.server.objectInputFilterFactoryClassName=is.codion.common.rmi.server.WhitelistInputFilterFactory",
        "-Dcodion.server.serializationFilterWhitelist=classpath:serialization-whitelist.txt",
        //SSL configuration
        "-Dcodion.server.classpathKeyStore=keystore.jks",
        "-Djavax.net.ssl.keyStorePassword=crappypass",
        //The port used by clients
        "-Dcodion.server.port=${serverPort}",
        //The servlet server
        "-Dcodion.server.auxiliaryServerFactoryClassNames=is.codion.framework.servlet.EntityServiceFactory",
        "-Dcodion.server.http.secure=false",
        "-Dcodion.server.http.port=${serverHttpPort}",
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
    imageName = project.name
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
        imageName = "World-Server"
        if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
            installerType = "deb"
            icon = "../world.png"
            installerOptions = listOf(
                "--resource-dir",
                "build/jpackage/World-Server/lib",
                "--linux-shortcut"
            )
        }
        if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
            installerType = "msi"
            icon = "../world.ico"
            imageOptions = imageOptions + listOf("--win-console")
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
    }
}