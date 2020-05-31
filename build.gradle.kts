val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("jvm") version "1.3.72" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}

fun DependencyHandler.ktor(artifact: String) = "io.ktor:ktor-$artifact:$ktor_version"