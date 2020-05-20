import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project

plugins {
    application
    kotlin("jvm")
}

group = "pro.mezentsev.risovaka.client"
version = "0.1"

application {
   mainClassName = "pro.mezentsev.risovaka.WsClientAppKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://kotlin.bintray.com/kotlin-js-wrappers") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(ktor("client-core"))
    implementation(ktor("client-core-jvm"))
    implementation(ktor("client-json-jvm"))
    implementation(ktor("client-gson"))
    implementation(ktor("client-logging-jvm"))
    implementation(ktor("client-websockets"))
    implementation(ktor("client-cio"))

    implementation("ch.qos.logback:logback-classic:$logback_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

fun ktor(artifact: String) = "io.ktor:ktor-$artifact:$ktor_version"
