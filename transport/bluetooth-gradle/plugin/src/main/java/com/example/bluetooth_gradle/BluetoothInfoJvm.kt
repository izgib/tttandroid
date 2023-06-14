package com.example.bluetooth_gradle

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream

abstract class BluetoothInfoJvm : BluetoothInfo, Exec() {

    /*@get:OutputFile
    protected abstract val bluetoothInfo: RegularFileProperty*/

    //private val btInfo: BluetoothInfo by lazy { getData() }

    /*override val deviceName: String
        get() = btInfo.deviceName
    override val macAddress: String
        get() = btInfo.macAddress*/

    //abstract val deviceNameT: Property<String>
    @Internal
    final override lateinit var deviceName: String
        private set
    @Internal
    final override lateinit var macAddress: String
        private set

    private val buffer = ByteArrayOutputStream()

    init {
        commandLine = listOf("hciconfig", "hci0", "name")
        standardOutput = buffer

        //bluetoothInfo.convention(project.layout.buildDirectory.file("bluetooth-info.txt"))
    }

    /*private fun getData(): BluetoothInfo {
        bluetoothInfo.get().asFile.readText().split("\n", limit = 2).run {
            return BluetoothInfo(get(0), get(1))
        }
    }*/

    @TaskAction
    fun getInfo() {
        val address: String
        val name: String
        buffer.toString().run {
            val addressStr = "BD Address: "
            val addressInd = indexOf(addressStr).run {
                if (this == -1) throw IllegalStateException("can not find BD Address")
                this + addressStr.length
            }
            address = substring(addressInd, addressInd + 17)

            val nameStr = "Name: '"
            val nameInd = indexOf(nameStr).run {
                if (this == -1) throw IllegalStateException("can not find BD Name")
                this + nameStr.length
            }
            name = substring(nameInd, length - 2)
            //if (getData().run { macAddress == address && deviceName == name }) return
            macAddress = address
            deviceName = name

            //bluetoothInfo.get().asFile.writeText("$address\n$name")
        }
    }
}