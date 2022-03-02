/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.java-conventions")
}

dependencies {
    api(project(":jul.exception"))
    api("com.io7m.xom:xom:1.2.10")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-databind:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-annotations:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-core:[2.11,2.12-alpha)")
    api("org.codehaus.woodstox:woodstox-core-asl:[4.1,4.2-alpha)")
}

description = "JUL Processing XML"

java {
    withJavadocJar()
}
