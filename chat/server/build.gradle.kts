val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
    kotlin("jvm")
}

group = "pro.mezentsev.risovaka.chat.server"
version = "0.1"

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation(project(":session"))

    implementation(ktor("server-core"))
    implementation(ktor("server-sessions"))
    implementation(ktor("server-host-common"))

    implementation(ktor("client-websockets"))
    implementation(ktor("client-logging-jvm"))

    implementation(ktor("websockets"))
    implementation(ktor("metrics"))

    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation(ktor("server-tests"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

fun ktor(artifact: String) = "io.ktor:ktor-$artifact:$ktor_version"