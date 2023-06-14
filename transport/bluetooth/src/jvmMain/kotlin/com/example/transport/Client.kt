package com.example.transport

import com.example.transport.bluez.property
import com.github.hypfvieh.bluetooth.wrapper.ProfileChangeListener
import com.github.hypfvieh.bluetooth.wrapper.ProfileHandler
import com.github.hypfvieh.bluetooth.wrapper.ProfileManager
import jnr.posix.POSIXFactory
import jnr.unixsocket.UnixSocketChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.bluez.Device1
import org.bluez.exceptions.BluezCanceledException
import org.freedesktop.dbus.FileDescriptor
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant
import java.nio.channels.Channels
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resumeWithException

@OptIn(InternalCoroutinesApi::class)
suspend fun BluetoothDevice.startClient(name: String, uuid: UUID) =
    suspendCancellableCoroutine<BluetoothSocket> { cont ->

        val profManager = ProfileManager(dbusConnection)
        val profilePath = "/net/bluetooth/Profile"
        val profileListener = object : ProfileChangeListener {
            val POSIX = POSIXFactory.getPOSIX()
            private var closed = false
            private var socket: UnixSocketChannel? = null

            override fun onProfileConnection(
                _dbusPath: String,
                _fd: FileDescriptor,
                _fdProperties: MutableMap<String, Variant<*>>
            ) {
                if (closed) {
                    throw BluezCanceledException("")
                }
                println("New connection: FD= \"$_fd\", Path=\"$_dbusPath\", Props=\"$_fdProperties\"")


                val channel = UnixSocketChannel.fromFD(_fd.intFileDescriptor)
                socket = channel

                closed = true
                cont.resume(
                    BluetoothSocket(
                        this@startClient,
                        channel.toInputStream(),
                        Channels.newOutputStream(channel),
                    ) {
                        val device = this@startClient
                        println("${device.name}:$address closing $uuid profile")
                        try {
                            disconnectProfile(uuid.toString())
                        } catch (e: Throwable) {
                            println("disconnection error: $e")
                            if (e is DBusExecutionException && e.message == "Operation already in progress") {
                                val latch = CountDownLatch(1)
                                val disconnectListener =
                                    object : AbstractPropertiesChangedHandler() {
                                        override fun handle(properties: Properties.PropertiesChanged) {
                                            with(properties) {
                                                if (interfaceName == Device1::class.java.name) {
                                                    if (!(property<Boolean>("Connected")
                                                            ?: return)
                                                    ) unregister(dbusConnection)
                                                }
                                            }
                                        }

                                        fun unregister(connection: DBusConnection) {
                                            connection.removeSigHandler(implementationClass, this)
                                            channel.close()
                                            profManager.unregisterProfile(uuid, profilePath)
                                            dbusConnection.unExportObject(profilePath)
                                            println("unexported")
                                            latch.countDown()
                                        }
                                    }
                                dbusConnection.addSigHandler(
                                    disconnectListener.implementationClass,
                                    disconnectListener
                                )
                                if (!this@startClient.isConnected) {
                                    disconnectListener.unregister(dbusConnection)
                                }
                                latch.await()
                            }
                        }

                    }
                )
                {}
            }

            override fun onProfileDisconnectRequest(_path: String) {
                println("Disconnected requested: Path=$_path")
                check(_path == this@startClient.dbusPath) { "device ${this@startClient} is not connected to service $uuid" }
                socket?.let { socket ->
                    socket.close()
                }
                profManager.unregisterProfile(uuid, profilePath)
                dbusConnection.unExportObject(profilePath)
            }

            override fun onProfileRelease() {
                println("released")
                socket?.let { socket ->
                    socket.close()
                }
                cont.cancel()
            }

        }

        val profile = ProfileHandler(profilePath, profileListener)
        dbusConnection.exportObject(profilePath, profile)

        val registered = profManager.registerProfile(
            profilePath, uuid.toString(), //rfcommUUID.toString(),
            mapOf(
                "Name" to name,
                "Role" to "client",
                "PSM" to UInt16(3),
                "Channel" to UInt16(0),
                "RequireAuthentication" to true,
                "RequireAuthorization" to true,
            )
        )

        cont.invokeOnCancellation { cause: Throwable? ->
            if (cause == null) return@invokeOnCancellation
            println("cause: $cause")
            if (profManager.unregisterProfile(uuid, profilePath)) {
                println("profile: $uuid unregistered")
                dbusConnection.unExportObject(
                    profilePath
                )
            }
            if (cause !is CancellationException) cont.tryResumeWithException(cause)
        }
        println("registered: $registered")
        try {
            rawDevice.ConnectProfile(uuid.toString())
        } catch (e: Throwable) {
            println(e)
            e.printStackTrace()
            if (profManager.unregisterProfile(uuid, profilePath)) {
                println("profile: $uuid unregistered")
                dbusConnection.unExportObject(profilePath)
            }
            cont.resumeWithException(e)
        }
    }

