package com.example.bluetooth_gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

class BluetoothPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.log(LogLevel.DEBUG, "bluetooth plugin Registered")
    }
}