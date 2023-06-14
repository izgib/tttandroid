package com.example.transport

import java.io.InputStream
import java.io.OutputStream

data class BluetoothConnection(val inputStream: InputStream, val outputStream: OutputStream)


/*
fun DeviceManager.startServerTest(uuid: UUID, name: String): BluetoothConnection {
    val btConnection = SynchronousQueue<BluetoothConnection>()

    val profManager = ProfileManager(dbusConnection)
    val profilePath = "/net/bluetooth/ProfileLol"
    val profile = ProfileHandler(profilePath, object : ProfileChangeListener {
        override fun onProfileConnection(
            _dbusPath: String,
            _fd: FileDescriptor,
            _fdProperties: MutableMap<String, Variant<*>>
        ) {
            println("New connection: FD= \"$_fd\", Path=\"$_dbusPath\", Props=\"$_fdProperties\"")
            btConnection.put(
                BluetoothConnection(
                    FileInputStream(_fd.toJavaFileDescriptor()), FileOutputStream(_fd.toJavaFileDescriptor()),
                )
            )
        }

        override fun onProfileDisconnectRequest(_path: String) {
            println("Disconnected requested: Path=$_path")
        }

        override fun onProfileRelease() {
            println("released")
        }
    })
    dbusConnection.exportObject(profilePath, profile)

    //val record = serviceRecord(ServiceInfo(uuid, name, null), 3, 5)

    //val rfcommUUID = "00000003-0000-1000-8000-00805F9B34FB"
    val rfcommUUID = uuid.toString()
    val registered = profManager.registerProfile(
        profilePath, rfcommUUID, mapOf(
            "Name" to name, "Role" to "server", "PSM" to UInt16(3), //"Channel" to UInt16(5),
            "Service" to uuid.toString(), "RequireAuthentication" to true, "RequireAuthorization" to true,
            //"ServiceRecord" to record,
        )
    )
    println("registered: $registered")

    getDevices(true).forEachIndexed { index, bluetoothDevice ->
        println("${index + 1}: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
    }

    registerPropertyHandler(object : AbstractPropertiesChangedHandler() {
        override fun handle(properties: Properties.PropertiesChanged) {
            if (properties.interfaceName != Device1::class.java.name) {
                return
            }

            val deviceInt =
                DbusHelper.getRemoteObject(dbusConnection, properties.path, Device1::class.java)
            val device =
                BluetoothDevice(deviceInt, adapter, properties.path, dbusConnection)

            properties.propertiesChanged.forEach { (key, variant) ->
                when (key) {
                    "Connected" -> {
                        val isConnected = (variant as Variant<Boolean>).value
                        if (isConnected) {
                            println("trusted: ${device.isTrusted}")
                        } else {
                            val address = properties.path.run {
                                substring(indexOfFirst { it == '_' } + 1, count()).replace("_", ":")
                            }
                            println("address: $address")
                        }
                        device.advertisingFlags?.forEach { flag ->
                            println("flag: $flag")
                        }
                        println("Connected: $isConnected")
                    }
                    "UUIDs" -> {
                        println(variant.value)
                    }
                    else -> {
                        println("key: $key")
                    }
                }

            }
        }
    })

    return btConnection.take()
}*/
