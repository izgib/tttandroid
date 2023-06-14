package com.example.transport.service

import com.example.controllers.GameSettings
import com.example.game.not
import com.example.transport.BluetoothGameItem
import com.example.transport.BluetoothLEInteractor
import com.example.transport.GameParams
import com.example.transport.bluez.interfaceAddListener
import com.example.transport.bluez.property
import com.example.transport.bluez.propertyListener
import com.example.transport.extensions.toGameSettings
import com.example.transport.getBluetoothDeviceInt
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.DiscoveryFilter
import com.github.hypfvieh.bluetooth.DiscoveryTransport
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.bluez.Adapter1
import org.bluez.Device1
import org.freedesktop.dbus.handlers.AbstractInterfacesAddedHandler
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.types.Variant
import java.util.*

data class AdvertisingSearch(val device: BluetoothDevice, val serviceData: Map<String, ByteArray>?)

fun DeviceManager.searchLEDevices(serviceUuid: UUID) = callbackFlow<AdvertisingSearch> {
    val devices = HashMap<String, Map<String, ByteArray>>()

    setScanFilter(
        mapOf(
            DiscoveryFilter.Transport to DiscoveryTransport.LE,
            //DiscoveryFilter.DuplicateData to true,
            DiscoveryFilter.UUIDs to arrayOf(serviceUuid.toString()),
        )
    )

    val added = interfaceAddListener lst@{
/*        interfaces.forEach { int, props ->
            println("$int: $props")
        }*/
        if (interfaces.containsKey(Device1::class.java.name)) {
            val devInt = dbusConnection.getBluetoothDeviceInt(signalSource.path)
            val device = BluetoothDevice(devInt, adapter, devInt.objectPath, dbusConnection)
            if (device.uuids.none { uuid -> uuid == BluetoothLEInteractor.service.toString() })
                return@lst
            val serviceData = device.serviceData
            println("service Data: $serviceData")
            devices[device.dbusPath] = serviceData
            trySend(AdvertisingSearch(device, serviceData))
        }
    }

    val changed = propertyListener lst@{
        when (interfaceName) {
            Adapter1::class.java.name -> if (!(property<Boolean>("Discovering")
                    ?: return@lst)
            ) close()
            Device1::class.java.name -> {
                val int = dbusConnection.getBluetoothDeviceInt(path)
                val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)
                if (device.uuids.none { uuid -> uuid == BluetoothLEInteractor.service.toString() }) {
                    return@lst
                }
                val serviceData =
                    property<Map<String, ByteArray>>("ServiceData") ?: return@lst
                trySend(AdvertisingSearch(device, serviceData))
            }
        }
    }

    registerSignalHandler(added)
    registerPropertyHandler(changed)

    if (!adapter.startDiscovery()) {
        throw IllegalStateException("can not start service discovery")
    }

    awaitClose {
        unRegisterSignalHandler(added)
        unRegisterPropertyHandler(changed)
        adapter.stopDiscovery()
    }
}