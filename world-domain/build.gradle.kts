plugins {
    `java-library`
    id("world.spotless.plugin")
}

dependencies {
    api(project(":world-domain-api"))

    testImplementation(libs.codion.framework.domain.test)
    testRuntimeOnly(libs.codion.framework.db.local)
    testRuntimeOnly(libs.codion.dbms.h2)
    testRuntimeOnly(libs.h2)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("codion.test.user", "scott:tiger")
    systemProperty("codion.db.url", "jdbc:h2:mem:h2db")
    systemProperty("codion.db.initScripts", "classpath:create_schema.sql")
}