/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.extension.type.processing"))
    api(project(":jul.communication.mqtt"))
    api("java3d:vecmath:_")
    api("java3d:j3d-core:_")
    api(Testing.junit.jupiter)
    api(Testing.junit.jupiter.api)
}

description = "JUL Transformation"

java {
    withJavadocJar()
}
