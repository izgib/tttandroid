package com.example.transport.bluez

import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.handlers.AbstractInterfacesAddedHandler
import org.freedesktop.dbus.handlers.AbstractInterfacesRemovedHandler
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.interfaces.Properties.PropertiesChanged
import org.freedesktop.dbus.types.Variant


class Listener : AbstractPropertiesChangedHandler() {
    override fun handle(properties: Properties.PropertiesChanged) {

    }

    fun DBusInterface.onListening(properties: PropertiesChanged) {
        this::class.java.name

    }
}

inline fun propertyListener(crossinline handler: PropertiesChanged.() -> Unit) =
    object : AbstractPropertiesChangedHandler() {
        override fun handle(properties: PropertiesChanged) {
            properties.handler()
        }

        fun unregister(connection: DBusConnection) {
            connection.removeSigHandler(implementationClass, this)
        }
    }

inline fun interfaceAddListener(crossinline handler: ObjectManager.InterfacesAdded.() -> Unit) =
    object : AbstractInterfacesAddedHandler() {
        override fun handle(int: ObjectManager.InterfacesAdded) {
            int.handler()
        }
    }

inline fun interfaceRemovedListener(crossinline handler: ObjectManager.InterfacesRemoved.() -> Unit) =
    object : AbstractInterfacesRemovedHandler() {
        override fun handle(int: ObjectManager.InterfacesRemoved) {
            int.handler()
        }
    }

/*fun Properties.onProperties(int: DBusInterface) {
    int::class.java.name
}*/


inline fun DBusInterface.onProperties(
    properties: PropertiesChanged,
    init: PropertiesChanged.() -> Unit
) {
    if (properties.interfaceName == this::class.java.name) properties.init()
}

inline fun <T> PropertiesChanged.property(property: String): T? {
    return (propertiesChanged[property] as Variant<T>?)?.value
}


/*class Lol : AbstractPropertiesChangedHandler() {
    override fun handle(properties: Properties.PropertiesChanged) {
        properties.interfaceName ==
    }

    fun fromInterface(inter: DBusInterface) {
        inter::class.java.name
    }

    private class Check(inter: DBusInterface)
}*/


fun listener(init: AbstractPropertiesChangedHandler.() -> Unit): Listener {
    val lol = Listener()
    lol.init()
    return lol
}