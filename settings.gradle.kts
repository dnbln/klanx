pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
            url = java.net.URI("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

rootProject.name = "klanx"

include("klanx-syn")
include("kllvm")
