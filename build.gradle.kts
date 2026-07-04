plugins {
    kotlin("jvm") version "2.3.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jooq:jooq:3.21.6")
    testImplementation(kotlin("test"))
    testImplementation("org.postgresql:postgresql:42.7.3")
}

tasks.test {
    useJUnitPlatform()
}
