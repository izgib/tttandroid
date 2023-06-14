package com.example.transport

import com.example.transport.bluez.interfaceAddListener
import com.example.transport.bluez.property
import com.example.transport.bluez.propertyListener
import com.github.hypfvieh.bluetooth.DeviceManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.channelFlow
import org.bluez.Adapter1
import org.bluez.Device1

fun DeviceManager.deviceSearchFlow(ms: Long) = channelFlow<BluetoothDevice> {
    val newDevices = HashMap<String, BluetoothDevice>()

    val added = interfaceAddListener {
        if (interfaces.containsKey(Device1::class.java.name)) {
            val int = dbusConnection.getBluetoothDeviceInt(signalSource.path)
            val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)
            newDevices[device.dbusPath]
            trySendBlocking(device)
        }
    }

    val changed = propertyListener {
/*        propertiesChanged.forEach { prop, value ->
            println("$prop: $value")
        }*/
        when (interfaceName) {
            Adapter1::class.java.name -> if (!property<Boolean>("Discovering")!!) close()
            Device1::class.java.name -> {
                propertiesChanged.forEach { (key, _) ->
                    println("property: $key")
                }
                val int = dbusConnection.getBluetoothDeviceInt(path)
                val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)
                newDevices[device.dbusPath] = device
                trySendBlocking(device)
            }
        }
    }

    registerPropertyHandler(changed)
    registerSignalHandler(added)

    if (!adapter.startDiscovery()) {
        throw IllegalStateException("can not start service discovery")
    }
    println("discovery started")
    awaitClose {
        adapter.stopDiscovery()
        unRegisterPropertyHandler(changed)
        unRegisterSignalHandler(added)
    }
}