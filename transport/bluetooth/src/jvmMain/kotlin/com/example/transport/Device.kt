package com.example.transport

import com.github.hypfvieh.DbusHelper
import org.bluez.Device1
import org.freedesktop.dbus.connections.impl.DBusConnection

fun DBusConnection.getBluetoothDeviceInt(devicePath: String): Device1 =
    DbusHelper.getRemoteObject(this, devicePath, Device1::class.java)