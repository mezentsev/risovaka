val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("jvm")
}


group = "pro.mezentsev.risovaka.common"
version = "0.1"

repositories {
    mavenLocal()
    jcenter()
}

dependencies {
    implementation(ktor("gson"))
    implementation("io.github.microutils:kotlin-logging:1.7.9")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

fun ktor(artifact: String) = "io.ktor:ktor-$artifact:$ktor_version"