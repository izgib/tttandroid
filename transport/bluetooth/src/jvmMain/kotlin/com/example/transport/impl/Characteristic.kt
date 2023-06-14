package com.example.transport.impl

import org.bluez.GattCharacteristic1
import org.bluez.datatypes.TwoTuple
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.FileDescriptor
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant

open class Characteristic(
    index: Int,
    private val uuid: String,
    private val flags: List<String>,
    private val service: Service,
    handle: UInt16? = null,
    private val notifying: Boolean = false
) : Properties, GattCharacteristic1 {
    var handle = handle
        private set

    val descriptors = mutableListOf<Descriptor>()


    private val path: String

    init {
        path = "${Service.path}/char$index"
    }

    fun addDescriptor(descriptor: Descriptor) {
        descriptors.add(descriptor)
    }

    override fun isRemote(): Boolean {
        return false
    }

    override fun getObjectPath(): String {
        return path
    }

    override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
        throw NotImplementedError()
    }

    override fun WriteValue(_value: ByteArray, _options: MutableMap<String, Variant<*>>) {
        throw NotImplementedError()
    }

    override fun AcquireWrite(_options: MutableMap<String, Variant<*>>): TwoTuple<FileDescriptor, UInt16> {
        throw NotImplementedError()
    }

    override fun AcquireNotify(_options: MutableMap<String, Variant<*>>): TwoTuple<FileDescriptor, UInt16> {
        throw NotImplementedError()
    }

    override fun StartNotify() {
        throw NotImplementedError()
    }

    override fun StopNotify() {
        throw NotImplementedError()
    }

    override fun Confirm() {
        throw NotImplementedError()
    }

    override fun <A : Any?> Get(interface_name: String, property_name: String): A {
        throw IllegalStateException("Characteristic Get is not implemented: $path - $property_name")
    }

    override fun <A : Any?> Set(interface_name: String, property_name: String, value: A) {
        require(interface_name == GattCharacteristic1::class.java.name) { "invalid interface: $interface_name" }
        when (property_name) {
            "Handle" -> handle = value as UInt16
            else -> throw IllegalArgumentException()
        }
    }

    // Service, UUID and Flags Are configured
    fun getDefaultProperties(): MutableMap<String, Variant<*>> {
        val res = mutableMapOf<String, Variant<*>>(
            "Service" to Variant(DBusPath(service.objectPath)),
            "UUID" to Variant(uuid),
            "Flags" to Variant(flags.toTypedArray()),
        )
        if (handle != null) res["Handle"] = Variant(handle)
        return res
    }

    override fun GetAll(interface_name: String?): MutableMap<String, Variant<*>> {
        val res = mutableMapOf<String, Variant<*>>(
            "Service" to Variant(DBusPath(service.objectPath)),
            "UUID" to Variant(uuid),
            "Flags" to Variant(flags.toTypedArray()),
            //"Value" to Variant(byteArrayOf(127))
            //"Descriptors" to Variant(arrayOf<String>())
            //"Includes" to Variant(arrayOf(DBusPath(charPath))),
            //"Characteristics" to Variant(arrayOf(DBusPath(charPath))),
            //"Includes" to Variant(arrayOf<String>()),
        )
        if (handle != null) res["Handle"] = Variant(handle)
        if (notifying) res["Notifying"] = Variant(notifying)

        println("characteristic: $path")

        return res
    }
}