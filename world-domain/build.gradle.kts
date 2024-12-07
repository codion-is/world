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
}