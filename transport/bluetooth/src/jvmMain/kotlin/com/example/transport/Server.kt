package com.example.transport


import com.example.transport.impl.gattManager
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.ProfileChangeListener
import com.github.hypfvieh.bluetooth.wrapper.ProfileHandler
import com.github.hypfvieh.bluetooth.wrapper.ProfileManager
import jnr.posix.POSIXFactory
import jnr.unixsocket.UnixSocketChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import org.bluez.GattCharacteristic1
import org.bluez.GattService1
import org.bluez.datatypes.TwoTuple
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.FileDescriptor
import org.freedesktop.dbus.errors.NotSupported
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant
import java.nio.channels.Channels
import java.util.*

fun DeviceManager.startServer(name: String, uuid: UUID): Flow<BluetoothSocket> {
    val profManager = ProfileManager(dbusConnection)
    val profilePath = "/net/bluetooth/Profile"
    var closedForConnections = false
    //var profileListener: ProfileChangeListener

    return channelFlow<BluetoothSocket> {
        val profileListener = object : ProfileChangeListener {
            val POSIX = POSIXFactory.getPOSIX()
            internal val sockets = hashMapOf<String, UnixSocketChannel>()

            override fun onProfileConnection(
                _dbusPath: String,
                _fd: FileDescriptor,
                _fdProperties: MutableMap<String, Variant<*>>
            ) {
                println("try to establish connection")
                if (closedForConnections) {
                    throw org.bluez.exceptions.BluezCanceledException("")
                }

                println("New connection: FD= \"$_fd\", Path=\"$_dbusPath\", Props=\"$_fdProperties\"")
                val channel = UnixSocketChannel.fromFD(_fd.intFileDescriptor)
                sockets[_dbusPath] = channel

                val int = dbusConnection.getBluetoothDeviceInt(_dbusPath)
                val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)
                println("device ${device.name}:${device.address}")
                trySend(
                    BluetoothSocket(
                        device,
                        channel.toInputStream(),
                        //FileOutputStream(_fd.toJavaFileDescriptor())
                        Channels.newOutputStream(channel)
                    ) {
                        val socket = sockets.remove(device.dbusPath) ?: run {
                            println("service \"$uuid\" is not connected")
                            return@BluetoothSocket
                        }
                        //println("res: ${POSIXFactory.getPOSIX().close(socket.fd)}")
                        if (socket.isOpen) POSIX.close(_fd.intFileDescriptor)
                        onSocketClose()
                    }
                )
            }

            fun onSocketClose() {
                if (sockets.isEmpty()) {
                    profManager.unregisterProfile(uuid, profilePath)
                    dbusConnection.unExportObject(profilePath)
                    println("here closing")
                }
            }

            override fun onProfileDisconnectRequest(_path: String) {
                println("Disconnected requested: Path=$_path")

                if (sockets.isEmpty()) {
                    val result = profManager.unregisterProfile(uuid, profilePath)
                    dbusConnection.unExportObject(profilePath)
                }
            }

            override fun onProfileRelease() {
                sockets.forEach { (_, socket) -> POSIX.close(socket.fd) }
            }
        }

        val profile = ProfileHandler(profilePath, profileListener)
        dbusConnection.exportObject(profilePath, profile)


        /*val discoveryHandler = object : AbstractPropertiesChangedHandler() {
            override fun handle(properties: Properties.PropertiesChanged) {
                if (properties.interfaceName == Adapter1::class.java.name) {
                    properties.propertiesChanged["Discoverable"]?.let { discoverable ->
                        if (!(discoverable as Variant<Boolean>).value) {
                            unRegisterPropertyHandler(this)
                            close(TimeoutException("Timeout occurred"))
                        }
                    }
                    return
                }
            }
        }
        registerPropertyHandler(discoveryHandler)*/
        println("before getting")
        val registered = profManager.registerProfile(
            profilePath, uuid.toString(), //rfcommUUID.toString(),
            mapOf(
                "Name" to name,
                "Role" to "server",
                "PSM" to UInt16(3),
                "Channel" to UInt16(0),
                "RequireAuthentication" to true,
                "RequireAuthorization" to true,
            )
        )
        println("registered: $registered")
        awaitClose {
            if (profileListener.sockets.isEmpty()) {
                profManager.unregisterProfile(uuid, profilePath)
                dbusConnection.unExportObject(profilePath)
            }
            println("closed")
            closedForConnections = true
        }
    }
}

fun DeviceManager.startServerLE(name: String, uuid: UUID) {
    val manager = getAdapter().gattManager()

    val root = "/game"
    val servicePath = "$root/service0"
    val charPath = "$servicePath/char0"
    val charUUID = UUID.fromString("8681b031-fb44-441b-bcc7-cb111b242dc7")

    val service = object :
        Properties, GattService1 {
        val primary = true

        override fun isRemote(): Boolean {
            return false
        }

        override fun getObjectPath(): String {
            return servicePath
        }

        override fun <A : Any?> Get(interface_name: String, property_name: String): A {
            println("service:  trying to get")
            throw NotImplementedError()
        }

        override fun <A : Any?> Set(interface_name: String, property_name: String, value: A) {
            println("service:  trying to set")
        }

        override fun GetAll(interface_name: String): MutableMap<String, Variant<*>> {
            require(interface_name == GattService1::class.java.name)

            val res = mutableMapOf<String, Variant<*>>(
                "Primary" to Variant(primary),
                "UUID" to Variant(uuid.toString()),
                //"Includes" to Variant(arrayOf(DBusPath(charPath))),
                "Characteristics" to Variant(arrayOf(DBusPath(charPath))),
                //"Includes" to Variant(arrayOf<String>()),
            )

            println("service")
            println(res)

            return res
        }
    }

    val char = object :
        Properties, GattCharacteristic1 {

        val cUUID = charUUID
        val service = service
        val flags = arrayOf("read")

        override fun isRemote(): Boolean = false

        override fun getObjectPath(): String {
            return charPath
        }

        override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
            println("ReadValue called")
            throw NotSupported("")
        }

        override fun WriteValue(_value: ByteArray?, _options: MutableMap<String, Variant<*>>?) {
            println("WriteValue called")
            throw NotSupported("")
        }

        override fun AcquireWrite(_options: MutableMap<String, Variant<*>>?): TwoTuple<FileDescriptor, UInt16> {
            println("AcquireWrite called")
            throw NotSupported("")
        }

        override fun AcquireNotify(_options: MutableMap<String, Variant<*>>?): TwoTuple<FileDescriptor, UInt16> {
            println("AcquiredNotify called")
            throw NotSupported("")
        }

        override fun StartNotify() {
            println("StartNotify called")
            throw NotSupported("")
        }

        override fun StopNotify() {
            println("StopNotify called")
            throw NotSupported("")
        }

        override fun Confirm() {
            println("Confirm called")
        }

        override fun <A : Any?> Get(interface_name: String, property_name: String): A {
            println("char: trying to get")
            throw NotImplementedError()
        }

        override fun <A : Any?> Set(interface_name: String, property_name: String, value: A) {
            println("char: trying to set")
        }

        override fun GetAll(interface_name: String): MutableMap<String, Variant<*>> {
            require(interface_name == GattCharacteristic1::class.java.name)

            val res = mutableMapOf(
                "Service" to Variant(DBusPath(service.objectPath)),
                "UUID" to Variant(cUUID.toString()),
                "Flags" to Variant(flags),
            )
            println("char")
            println(res)
            return res
        }
    }


    val properties = object : ObjectManager {
        override fun isRemote(): Boolean {
            return false
        }

        override fun getObjectPath(): String {
            return root
        }

        override fun GetManagedObjects(): MutableMap<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>> {
            val serviceData = mutableMapOf<String, Variant<*>>(
                charUUID.toString() to Variant(DBusPath(charPath))
            )

            val servName = GattService1::class.java.name
            val servRes = service.GetAll(servName)
            val charName = GattCharacteristic1::class.java.name
            val charRes = char.GetAll(charName)

            val result = mutableMapOf<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>>(
                DBusPath(servicePath) to mutableMapOf(servName to servRes),
                DBusPath(charPath) to mutableMapOf(charName to charRes),
            )

            return result
        }
    }

    dbusConnection.exportObject(service)
    dbusConnection.exportObject(char)
    dbusConnection.exportObject(properties)

    println("after")
    manager.registerApplication(
        DBusPath(properties.objectPath), mapOf("Address_Type" to Variant("LE Public"))
        /*mapOf<String, Variant<*>>(
            "UUID" to Variant(uuid.toString()),
            "Primary" to Variant(true),
            "Includes" to Variant(arrayOf<DBusPath>())
        )*/
        //"Includes" to Variant(arrayOf<DBusPath>(DBusPath(char.dbusPath)))
    )
    println("here")
}