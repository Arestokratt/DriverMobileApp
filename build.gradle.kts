plugins {
    kotlin("jvm") version "2.3.0"
    id("application")              // Этот плагин добавляет task 'run'
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-gson:2.3.7")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.mindrot:jbcrypt:0.4")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}