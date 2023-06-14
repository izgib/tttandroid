package com.example.transport

import java.io.InputStream
import java.io.OutputStream
import java.util.*

actual typealias BluetoothDevice = com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice


/*actual class BluetoothDevice(
    rawDevice: Device1,
    adapter: BluetoothAdapter,
    dbusPath: String,
    dBusConnection: DBusConnection
) : BluetoothDevice(rawDevice, adapter, dbusPath, dBusConnection) {
    private val btAgent = Agent1Impl(adapter)

    actual fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothSocket {
        val btConnection = SynchronousQueue<BluetoothSocket>()
        val profManager = ProfileManager(dbusConnection)
        val profilePath = "/net/bluetooth/Profile"

        val profile = ProfileHandler(profilePath, object : ProfileChangeListener {
            override fun onProfileConnection(
                _dbusPath: String,
                _fd: FileDescriptor,
                _fdProperties: MutableMap<String, Variant<*>>
            ) {
                println("New connection: FD= \"$_fd\", Path=\"$_dbusPath\", Props=\"$_fdProperties\"")
                btConnection.put(
                    BluetoothSocket(
                        FileInputStream(_fd.toJavaFileDescriptor()),
                        FileOutputStream(_fd.toJavaFileDescriptor())
                    )
                )

                btConnection.put(
                    BluetoothConnection(
                        FileInputStream(_fd.toJavaFileDescriptor()), FileOutputStream(_fd.toJavaFileDescriptor()),
                    )
                )
            }

            override fun onProfileDisconnectRequest(_path: String) {
                println("Disconnected requested: Path=$_path")
                profManager.unregisterProfile(uuid, profilePath)
            }

            override fun onProfileRelease() {
                println("released")
            }
        })
        dbusConnection.exportObject(profilePath, profile)

        val registered = profManager.registerProfile(
            profilePath, uuid.toString(), mapOf(
                "Name" to name,
                "Role" to "server",
                "PSM" to UInt16(3), //"Channel" to UInt16(5),
                "Service" to uuid.toString(),
                "RequireAuthentication" to true,
                "RequireAuthorization" to true,
                //"ServiceRecord" to record,
            )
        )
        connectProfile(uuid.toString())

        return btConnection.take()
        val protocol = "btspp"
        val intAddr = StringBuilder(8).run {
            append(address.split(':'))
        }
        val con = Connector.open("$protocol://$intAddr;authenticate=true;authorize=true;encrypt=true") as StreamConnection
        return BluetoothSocket(con)
    }
}*/


/*class BluetoothAdapter {
    fun listenUsingRfcommWithServiceRecord(name: String, uuid: UUID) {
        LocalDevice.getLocalDevice().apply {
            discoverable = DiscoveryAgent.LIAC
        }

        val protocol = "btspp"
        val connection = ((Connector.open("$protocol://localhost:$uuid;name=$name;authenticate=true;authorize=true;encrypt=true")) as StreamConnectionNotifier).run {
            acceptAndOpen()
        }
    }
}*/

actual class BluetoothSocket(
    private val remoteDevice: BluetoothDevice,
    private val iS: InputStream,
    private val oS: OutputStream,
    private val onClose: () -> Unit
) {

    //actual fun getInputStream(): InputStream = con.openInputStream()
    //actual fun getOutputStream(): OutputStream = con.openOutputStream()

    //actual fun close() {
    //    descriptor.intFileDescriptor
    //    con.close()
    //}
    actual fun getRemoteDevice(): BluetoothDevice = remoteDevice
    actual fun getInputStream(): InputStream = iS
    actual fun getOutputStream(): OutputStream = oS
    actual fun close() = onClose()
}

actual fun BluetoothSocket.toConnectionWrapper() = object : ConnectionWrapper {
    override val inputStream = this@toConnectionWrapper.getInputStream()
    override val outputStream = this@toConnectionWrapper.getOutputStream()
    override fun close() = this@toConnectionWrapper.close()
}

