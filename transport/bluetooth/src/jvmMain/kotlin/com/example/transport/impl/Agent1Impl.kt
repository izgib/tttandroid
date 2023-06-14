package com.example.transport.impl

import com.example.transport.BluetoothInteractor
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import org.bluez.Agent1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.UInt32
import org.bluez.Device1
import org.bluez.exceptions.BluezRejectedException


class Agent1Impl(private val adapter: BluetoothAdapter): Agent1 {
    override fun Release() {
        println("release called")
    }

    override fun RequestPinCode(_device: DBusPath): String {
        val device = getDevice(_device)
        println("RequestPinCode called on device ${device.name}:${device.address}$")
        return "0000"
    }

    override fun DisplayPinCode(_device: DBusPath, _pincode: String) {
        val device = getDevice(_device)
        println("DisplayPinCode called on device ${device.name}:${device.address} with code $_pincode")
    }

    override fun RequestPasskey(_device: DBusPath): UInt32 {
        val device = getDevice(_device)
        println("RequestPasskey called on device ${device.name}:${device.address}")
        return UInt32(111111)
    }

    override fun DisplayPasskey(_device: DBusPath, _passkey: UInt32, _entered: UInt16) {
        val device = getDevice(_device)
        println("DisplayPasskey called on device ${device.name}:${device.address} with code $_passkey, entered=$_entered")
    }

    override fun RequestConfirmation(_device: DBusPath, _passkey: UInt32) {
        val device = getDevice(_device)
        println("RequestConfirmation called on device ${device.name}:${device.address} with code $_passkey")
        device.uuids?.forEachIndexed { i, uuid ->
            println("${i+1}: $uuid")
        }
    }

    override fun RequestAuthorization(_device: DBusPath) {
        val device = getDevice(_device)
        println("RequestAuthorization called on device: ${device.name}:${device.address}")
    }

    override fun AuthorizeService(_device: DBusPath, _uuid: String) {
        val device = getDevice(_device)
        println("AuthorizeService called on device ${device.name}:${device.address} with service $_uuid")
        if (_uuid != BluetoothInteractor.MY_UUID.toString()) throw BluezRejectedException("")
    }

    override fun Cancel() {
        println("cancel called")
    }

    override fun isRemote(): Boolean {
        return false
    }

    override fun getObjectPath(): String {
        return "/net/TTTAgent"
        //return "/"
    }

    private fun getDevice(_device: DBusPath): BluetoothDevice {
        val devInt = adapter.dbusConnection.getRemoteObject("org.bluez", _device.path, Device1::class.java)
        return BluetoothDevice(devInt, adapter, _device.path, adapter.dbusConnection)
    }
}