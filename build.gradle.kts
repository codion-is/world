plugins {
    java
}

version = libs.versions.codion.get().replace("-SNAPSHOT", "")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

configure(subprojects) {
    apply(plugin = "java")

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