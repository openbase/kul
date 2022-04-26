/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.communication"))
    api(project(":jul.schedule"))
    api(project(":jul.extension.type.processing"))
    api("com.hivemq:hivemq-mqtt-client:_")
    testImplementation(Testing.mockK)
    testImplementation("org.testcontainers:junit-jupiter:_")  {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("io.quarkus:quarkus-junit4-mock:_") // required as long as testcontainers depends on junit4
    testImplementation("io.kotest:kotest-assertions-core-jvm:_")
}

description = "JUL Extension MQTT"

java {
    withJavadocJar()
}
