import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("jvm") version "1.3.72" apply false
}

allprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

subprojects {
    repositories {
        mavenCentral()
    }
}

fun DependencyHandler.ktor(artifact: String) = "io.ktor:ktor-$artifact:$ktor_version"