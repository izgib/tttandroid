package com.example.transport.impl

import org.bluez.GattDescriptor1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant

open class Descriptor(
    private val index: Int,
    val uuid: String,
    val flags: List<String>,
    val characteristic: Characteristic,
    val value: ByteArray? = null,
    private val handle: UInt16? = null
) : GattDescriptor1, Properties {
    override fun isRemote(): Boolean {
        return false
    }

    override fun getObjectPath(): String = "${characteristic.objectPath}/desc$index"

    override fun <A : Any?> Get(interface_name: String?, property_name: String?): A {
        TODO("Not yet implemented")
    }

    override fun <A : Any?> Set(interface_name: String?, property_name: String?, value: A) {
        println("$interface_name: $property_name $value")
    }

    override fun GetAll(interface_name: String): MutableMap<String, Variant<*>> {
        println("descriptor")
        val map = mutableMapOf(
            "Characteristic" to Variant(DBusPath(characteristic.objectPath)),
            "UUID" to Variant(uuid),
            "Flags" to Variant(flags.toTypedArray())
        )

        value?.let { map["Value"] = Variant(it) }
        handle?.let { map["Handle"] = Variant(it) }
        return map
    }

    override fun ReadValue(_flags: MutableMap<String, Variant<*>>?): ByteArray {
        println("read desc")

        TODO("Not yet implemented")
    }

    override fun WriteValue(_value: ByteArray?, _flags: MutableMap<String, Variant<*>>?) {
        println("write desc")

        TODO("Not yet implemented")
    }
}