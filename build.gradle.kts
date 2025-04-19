plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

configure(subprojects) {
    apply(plugin = "java")

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
}