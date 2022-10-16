import java.net.URI

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.bytedeco.gradle-javacpp-platform") version "1.5.8-SNAPSHOT"
}

ext {
    set("javacppPlatform", "linux-x86_64,windows-x86_64")
}

group = "dev.dnbln"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation("org.bytedeco:llvm-platform:15.0.2-1.5.8-SNAPSHOT")
    implementation("org.bytedeco:javacpp:1.5.8-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}