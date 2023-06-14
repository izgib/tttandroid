package com.example.transport.impl

import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.wrapper.AbstractBluetoothObject
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDeviceType
import org.bluez.LEAdvertisingManager1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant

class LEAdvertisingManager(
    val rawLEAdvertisingManager: LEAdvertisingManager1,
    dBusConnection: DBusConnection,
    dbusPath: String
) : AbstractBluetoothObject(BluetoothDeviceType.NONE, dBusConnection, dbusPath), Properties {

    // Number of active advertising instances.
    var activeInstances: Byte? = null
        //get() = getTyped("ActiveInstances", Byte::class.java)
        private set

    // Number of available advertising instances.
    var supportedInstances: Byte? = null
        //get() = getTyped("SupportedInstances", Byte::class.java)
        private set

    // List of supported system includes.
    var supportedIncludes: Array<*>? = null
/*        get() {
            return getTyped("SupportedIncludes", ArrayList::class.java)?.let { arr ->
                return@let arr.toArray() as Array<*>
            }
        }*/
        private set

    /*
    * List of supported Secondary channels. Secondary<br>
    * channels can be used to advertise with the<br>
    * corresponding PHY
    * <b>EXPERIMENTAL</b>
    */
    var supportedSecondaryChannels: String? = null
        //get() = getTyped("SupportedSecondaryChannels", String::class.java)
        private set


    override fun getInterfaceClass(): Class<out DBusInterface> = LEAdvertisingManager1::class.java


    fun registerAdvertisement(advertisement: DBusPath, options: Map<String, Variant<*>>) {
        rawLEAdvertisingManager.RegisterAdvertisement(advertisement, options)
    }

    fun unregisterAdvertisement(advertisement: DBusPath) {
        rawLEAdvertisingManager.UnregisterAdvertisement(advertisement)
    }

    override fun getObjectPath(): String {
        return dbusPath
    }

    override fun <A : Any> Get(interface_name: String?, property_name: String?): A {
        throw IllegalStateException("Advertising Get is not implemented")
    }

    override fun <A : Any> Set(interface_name: String?, property_name: String?, value: A) {
        if (interface_name == LEAdvertisingManager1::class.java.name) {
            when (property_name) {
                "ActiveInstances" -> activeInstances = value as Byte
                "SupportedInstances" -> supportedInstances = value as Byte
                "SupportedIncludes" -> supportedIncludes = value as Array<*>
                "SupportedSecondaryChannels" -> supportedIncludes = value as Array<*>
            }
        }
        println("$interface_name: $property_name $value")
    }

    override fun GetAll(interface_name: String?): MutableMap<String, Variant<*>> {
        throw IllegalStateException("Advertising GetAll is not implemented")
    }
}

val BluetoothAdapter.LEAdvertisingManager: LEAdvertisingManager
    get() {
        val manager =
            DbusHelper.getRemoteObject(dbusConnection, dbusPath, LEAdvertisingManager1::class.java)

        return LEAdvertisingManager(manager, dbusConnection, dbusPath)
    }
