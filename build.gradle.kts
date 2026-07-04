plugins {
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:3.21.6")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
