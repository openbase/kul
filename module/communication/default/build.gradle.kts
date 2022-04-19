/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.jul")
}

dependencies {
    api("org.openbase:type:[1.1,1.2-alpha)")
    api(project(":jul.extension.type.processing"))
    api(project(":jul.interface"))
    api(project(":jul.exception"))
    api("org.jetbrains.kotlin:kotlin-reflect:_")
}

description = "JUL Communication Default"

java {
    withJavadocJar()
}
