rootProject.name = "jul"

pluginManagement {
    plugins {
        id("de.fayard.refreshVersions") version "0.51.0"
    }
}

plugins {
    id("de.fayard.refreshVersions")
}

include(":jul.pattern.launch")
include(":jul.schedule")
include(":jul.audio")
include(":jul.storage")
include(":jul.visual.swing")
include(":jul.extension.type.util")
include(":jul.interface")
include(":jul.processing.module")
include(":jul.extension.type.storage")
include(":jul.pattern.module")
include(":jul.extension.type")
include(":jul.communication.mqtt")
include(":jul.communication.mqtt.test")
include(":jul.processing.xml")
include(":jul.extension.protobuf")
include(":jul.processing.json")
include(":jul.communication.tcp")
include(":jul.annotation")
include(":jul.exception")
include(":jul.visual")
include(":jul.communication.controller")
include(":jul.extension.type.transform")
include(":jul.visual.javafx")
include(":jul.communication")
include(":jul.processing")
include(":jul.extension.type.processing")
include(":jul.pattern.controller")
include(":jul.extension.type.interface")
include(":jul.pattern")
include(":jul.communication.module")
include(":jul.pattern.trigger")
include(":jul.extension")
include(":jul.transformation")
include(":jul.test")
project(":jul.pattern.launch").projectDir = file("module/pattern/launch")
project(":jul.schedule").projectDir = file("module/schedule")
project(":jul.audio").projectDir = file("module/audio")
project(":jul.storage").projectDir = file("module/storage")
project(":jul.visual.swing").projectDir = file("module/visual/swing")
project(":jul.extension.type.util").projectDir = file("module/extension/type/util")
project(":jul.interface").projectDir = file("module/interface")
project(":jul.processing.module").projectDir = file("module/processing")
project(":jul.extension.type.storage").projectDir = file("module/extension/type/storage")
project(":jul.pattern.module").projectDir = file("module/pattern")
project(":jul.extension.type").projectDir = file("module/extension/type")
project(":jul.communication.mqtt").projectDir = file("module/communication/mqtt")
project(":jul.communication.mqtt.test").projectDir = file("module/communication/mqtttest")
project(":jul.processing.xml").projectDir = file("module/processing/xml")
project(":jul.extension.protobuf").projectDir = file("module/extension/protobuf")
project(":jul.processing.json").projectDir = file("module/processing/json")
project(":jul.communication.tcp").projectDir = file("module/communication/tcp")
project(":jul.annotation").projectDir = file("module/annotation")
project(":jul.exception").projectDir = file("module/exception")
project(":jul.visual").projectDir = file("module/visual")
project(":jul.communication.controller").projectDir = file("module/communication/controller")
project(":jul.extension.type.transform").projectDir = file("module/extension/type/transform")
project(":jul.visual.javafx").projectDir = file("module/visual/javafx")
project(":jul.communication").projectDir = file("module/communication/default")
project(":jul.processing").projectDir = file("module/processing/default")
project(":jul.extension.type.processing").projectDir = file("module/extension/type/processing")
project(":jul.pattern.controller").projectDir = file("module/pattern/controller")
project(":jul.extension.type.interface").projectDir = file("module/extension/type/interface")
project(":jul.pattern").projectDir = file("module/pattern/default")
project(":jul.communication.module").projectDir = file("module/communication")
project(":jul.pattern.trigger").projectDir = file("module/pattern/trigger")
project(":jul.extension").projectDir = file("module/extension")
project(":jul.transformation").projectDir = file("module/transformation")
project(":jul.test").projectDir = file("module/test")
