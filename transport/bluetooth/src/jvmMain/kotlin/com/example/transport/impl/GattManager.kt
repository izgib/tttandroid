package com.example.transport.impl

import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.wrapper.AbstractBluetoothObject
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDeviceType
import org.bluez.GattManager1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.types.Variant

class GattManager(
    val rawGattManager: GattManager1,
    dBusConnection: DBusConnection,
    dbusPath: String
) :
    AbstractBluetoothObject(BluetoothDeviceType.NONE, dBusConnection, dbusPath) {

    override fun getInterfaceClass(): Class<out DBusInterface> = GattManager1::class.java

    /**
    Registers a local GATT services hierarchy (GATT Server)
    and/or GATT profiles (GATT Client).
     */
    fun registerApplication(application: DBusPath, options: Map<String, Variant<*>>) {
        rawGattManager.RegisterApplication(application, options)
    }

    /**
    This unregisters the services that has been
    previously registered. The object path parameter
    must match the same value that has been used
    on registration.
     */
    fun unregisterApplication(application: DBusPath) {
        rawGattManager.UnregisterApplication(application)
    }
}

fun BluetoothAdapter.gattManager(): GattManager {
    val manager = DbusHelper.getRemoteObject(dbusConnection, dbusPath, GattManager1::class.java)
    return GattManager(manager, dbusConnection, dbusPath)
}