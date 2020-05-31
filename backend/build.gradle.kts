val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

group = "pro.mezentsev.risovaka.backend"
version = "0.1"

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://kotlin.bintray.com/kotlin-js-wrappers") }
}

dependencies {
    implementation(project(":session"))
    implementation(project(":chat:server"))

    implementation(ktor("server-netty"))
    implementation(ktor("server-core"))
    implementation(ktor("server-sessions"))
    implementation(ktor("server-host-common"))

    implementation(ktor("client-websockets"))
    implementation(ktor("client-logging-jvm"))

    implementation(ktor("html-builder"))
    implementation(ktor("websockets"))
    implementation(ktor("freemarker"))
    implementation(ktor("gson"))
    implementation(ktor("metrics"))

    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.94-kotlin-1.3.70")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation(ktor("server-tests"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

fun ktor(artifact: String) = "io.ktor:ktor-$artifact:$ktor_version"