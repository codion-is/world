import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask

plugins {
    java
    id("com.github.breadmoirai.github-release") version "2.5.2" apply false
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configure(subprojects) {
    apply(plugin = "java")
    
    dependencies {
        implementation(platform(rootProject.libs.codion.framework.bom))
    }

    version = rootProject.libs.versions.codion.get().replace("-SNAPSHOT", "")

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.isDeprecation = true
    }

    testing {
        suites {
            val test by getting(JvmTestSuite::class) {
                useJUnitJupiter()
                targets {
                    all {
                        testTask.configure {
                            systemProperty("codion.db.url", "jdbc:h2:mem:h2db")
                            systemProperty("codion.db.initScripts", "classpath:create_schema.sql")
                            systemProperty("codion.test.user", "scott:tiger")
                        }
                    }
                }
            }
        }
    }

    tasks.withType<GithubReleaseTask>().configureEach {
        dependsOn(tasks.named("jlinkZip"))
        dependsOn(tasks.named("jpackage"))
    }
}