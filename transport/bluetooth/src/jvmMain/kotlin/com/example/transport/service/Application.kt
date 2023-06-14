package com.example.transport.service

import com.example.controllers.GameSettings
import com.example.transport.*
import com.example.transport.impl.LEAdvertisingManager
import com.example.transport.impl.Service
import com.example.transport.impl.gattManager
import com.github.hypfvieh.bluetooth.DeviceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.bluez.GattCharacteristic1
import org.bluez.GattDescriptor1
import org.bluez.GattService1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class Application(private val deviceManager: DeviceManager) : ObjectManager,
    com.example.transport.Application {
    val services = mutableListOf<Service>()
    val service = GameService(deviceManager.dbusConnection)
    override var settings: GameSettings
        get() = service.settings.settings
        set(value) {
            service.settings.settings = value
        }


    private var advertisementID: Byte? = null

    var onStartListening: ((listening: Boolean) -> Unit)? = null

    init {
        addService(service)
        service.serverMessages.onNotify = onStartListening
    }

    override fun getObjectPath(): String {
        //return "game"
        return "/org/bluez/game"
    }

    fun addService(service: Service) {
        services.add(service)
    }


    @Suppress("NewApi")
    private fun startAdvertising() {
        deviceManager.adapter.run {
            LEAdvertisingManager.apply {
                advertisementID = activeInstances?.inc() ?: 1
                println("advertising id: $advertisementID")
                ProcessBuilder().command(
                    "sudo",
                    "btmgmt",
                    "add-adv",
                    "-g",
                    "-c",
                    "-u", service.uuid, //"-D", "60",
                    "-s", toScanResponseData(
                        BluetoothLEInteractor.settings,
                        service.settings.settingsData
                    ),
                    advertisementID.toString()
                ).inheritIO().start().waitFor()
            }
        }
    }

    @Suppress("NewApi")
    private fun stopAdvertising() {
        requireNotNull(advertisementID)
        ProcessBuilder().command(
            "sudo", "btmgmt", "rm-adv", advertisementID.toString()
        ).inheritIO().start().waitFor()
    }

    override fun GetManagedObjects(): MutableMap<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>> {
        println("here objects")

        println(objectPath)
        val res =
            mutableMapOf<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>>().apply {
                services.forEach { serv ->
                    val serviceMap = mutableMapOf<String, MutableMap<String, Variant<*>>>(
                        GattService1::class.java.name to serv.GetAll(Properties::class.java.name)
                    )
                    put(DBusPath(serv.objectPath), serviceMap)
                    serv.characteristics.forEach { char ->
                        val charMap = mutableMapOf<String, MutableMap<String, Variant<*>>>(
                            GattCharacteristic1::class.java.name to char.GetAll(Properties::class.java.name)
                        )
                        //put(char.objectPath, char.getVariantProperties())
                        put(DBusPath(char.objectPath), charMap)
                        char.descriptors.forEach { desc ->
                            val descMap = mutableMapOf<String, MutableMap<String, Variant<*>>>(
                                GattDescriptor1::class.java.name to desc.GetAll(Properties::class.java.name)
                            )
                            put(DBusPath(desc.objectPath), descMap)
                        }
                    }
                }
            }
        println(res)
        return res

        val objects = mutableMapOf<String, MutableMap<String, Variant<*>>>().apply {
            services.forEach { serv ->
                //put(serv.objectPath, serv.getVariantProperties())
                put(serv.objectPath, serv.GetAll(Properties::class.java.name))
                serv.characteristics.forEach { char ->
                    //put(char.objectPath, char.getVariantProperties())
                    put(char.objectPath, char.GetAll(Properties::class.java.name))
                }
            }
        }
        println(objectPath)
        println(objects)
        return mutableMapOf<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>>(
            DBusPath(objectPath) to objects
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun announceGame(): Flow<Announcement> = callbackFlow {
        send(Started)
        startAdvertising()
        service.serverMessages.onNotify = { notify: Boolean ->
            //stopAdvertising()
            println("notify: $notify")
            if (notify) {
                trySendBlocking(
                    ClientJoined(
                        BluetoothLEServerWrapper(
                            service.clientMessages.response,
                            service.serverMessages
                        )
                    )
                )
            }
            close()
        }
        awaitClose {
            startAdvertising()
            println("close announcing")
        }
    }

    override fun registerApplication() {
        deviceManager.adapter.gattManager().registerApplication(DBusPath(objectPath), mapOf())
    }

    override fun unregisterApplication() {
        deviceManager.adapter.gattManager().unregisterApplication(DBusPath(objectPath))
    }
}

fun Application.exportDbusObjects(conn: DBusConnection): Unit = conn.run {
    exportObject(objectPath, this@exportDbusObjects as ObjectManager)
    services.forEach { service ->
        exportObject(service.objectPath, service)
        service.characteristics.forEach { characteristic ->
            //exportObject(characteristic as GattCharacteristic1)
            exportObject(characteristic as Properties)
            println(characteristic.objectPath)
            characteristic.descriptors.forEach { descriptor ->
                //exportObject(descriptor as GattDescriptor1)
                exportObject(descriptor as Properties)
            }
        }
    }
    println("objects exported")
}

fun Application.unExportDbusObjects(conn: DBusConnection): Unit = conn.run {
    unExportObject(this@unExportDbusObjects.objectPath)
    services.forEach { service ->
        unExportObject(service.objectPath)
        service.characteristics.forEach { characteristic ->
            unExportObject(characteristic.objectPath)
            characteristic.descriptors.forEach { descriptor ->
                unExportObject(descriptor.objectPath)
            }
        }
    }
}

fun toScanResponseData(uuid: UUID, data: ByteArray): String {
    val byteData = ByteBuffer.allocate(16 + data.count()).apply {
        order(ByteOrder.LITTLE_ENDIAN)
        putLong(uuid.leastSignificantBits)
        putLong(uuid.mostSignificantBits)
        put(data)
    }.array()
    val dataCount = byteData.count() + 1
    return buildString((dataCount + 1) * 2) {
        append(dataCount.toString(16))  // package size
        append("21") // 21    Service Data - 128-bit UUID
        byteData.forEach { append(String.format("%02x", it)) }
    }
}