plugins {
    id("java-library")
    id("world.spotless.plugin")
}

dependencies {
    api(libs.codion.framework.domain)
    api(libs.codion.framework.db.core)
}