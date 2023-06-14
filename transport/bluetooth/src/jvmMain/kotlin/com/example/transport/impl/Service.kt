package com.example.transport.impl

import org.bluez.GattCharacteristic1
import org.bluez.GattService1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant

open class Service(val uuid: String, private val primary: Boolean, private var handle: UInt16? = null): GattService1, Properties {
    val characteristics = mutableListOf<Characteristic>()


    override fun isRemote(): Boolean {
        return false
    }

    override fun getObjectPath() = path

    override fun <A : Any?> Get(interface_name: String, property_name: String): A {
        println("service: trying to get")
        throw NotImplementedError()
    }

    override fun <A : Any> Set(interface_name: String, property_name: String, value: A) {
        if (interface_name == GattService1::class.java.name) {
            if (property_name == "Handle") {
                handle = value as UInt16
            }
        }
        println("$interface_name: $property_name $value")
    }

    private fun characteristicPaths(): Array<DBusPath> {
        return characteristics.map { DBusPath(it.objectPath) }.toTypedArray()
    }

    fun getVariantProperties(): MutableMap<String, Variant<*>> {
        return mutableMapOf<String, Variant<*>>(
            "Primary" to Variant(primary),
            "UUID" to Variant(uuid),
            "Characteristics" to Variant(characteristicPaths()),
        )
    }

    override fun GetAll(interface_name: String): MutableMap<String, Variant<*>> {
        //require(interface_name == GattService1::class.java.name)

        val res = mutableMapOf<String, Variant<*>>(
            "Primary" to Variant(primary),
            "UUID" to Variant(uuid),
            //"Includes" to Variant(arrayOf(DBusPath(charPath))),
            //"Characteristics" to Variant(characteristicPaths()),
            //"Includes" to Variant(arrayOf<String>()),
        )
        if (handle != null) res["Handle"] = Variant(handle)

        return res
    }

    fun addCharacteristic(char: Characteristic) {
        characteristics.add(char)
    }

    companion object {
        const val path = "/org/bluez/game/service0"
        //const val path = "game/service"
    }
}