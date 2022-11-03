import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("kapt") version "1.7.10"
    id("java")
    id("idea")
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

val versionCode = "1.0.0"
group = "de.mobanisto"
version = versionCode

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.2")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    implementation("de.topobyte:system-utils:0.0.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
