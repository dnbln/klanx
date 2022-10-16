import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "dev.dnbln"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation(project(":klanx-syn"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("dev.dnbln.klanx.MainKt")
}