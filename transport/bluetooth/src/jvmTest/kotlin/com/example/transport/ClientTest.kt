package com.example.transport

import com.example.controllers.models.NetworkGameModel
import com.example.controllers.models.PlayerType
import com.example.controllers.winX
import com.example.game.Mark
import com.example.game.not
import com.example.transport.impl.Agent1Impl
import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.DiscoveryFilter
import com.github.hypfvieh.bluetooth.DiscoveryTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bluez.Agent1
import org.bluez.AgentManager1
import org.bluez.Device1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import java.io.*
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess


fun main() {
    val deviceManager = DeviceManager.createInstance(false)


    val agentManager: AgentManager1 //= DbusHelper.getRemoteObject(deviceManager.dbusConnection, "/org/bluez", AgentManager1::class.java)
    val btAgent: Agent1 //= Agent1Impl(deviceManager.adapter)

    deviceManager.apply {
        agentManager =
            DbusHelper.getRemoteObject(dbusConnection, "/org/bluez", AgentManager1::class.java)
        btAgent = Agent1Impl(adapter)
        agentManager.RegisterAgent(DBusPath(btAgent.objectPath), "DisplayYesNo")
        agentManager.RequestDefaultAgent(DBusPath(btAgent.objectPath))
        dbusConnection.exportObject(btAgent.objectPath, btAgent)
    }

    val closeThread = thread(start = false, isDaemon = true) {
        deviceManager.closeConnection()
    }
    Runtime.getRuntime().addShutdownHook(closeThread)
    deviceManager.apply {
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
                                    substring(indexOfFirst { it == '_' } + 1, count()).replace(
                                        "_",
                                        ":"
                                    )
                                }
                                println("address: $address")
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
    }


    val example = winX.toNetworkTest(Mark.Cross)
    val isCreator = true
    val scope = CoroutineScope(Dispatchers.Default)

    val device = deviceManager.getDevice()

    val join = channelFlow<GameJoinStatus> {
        send(Loading)
        val socket: BluetoothSocket
        try {
            socket = device.startClient(BluetoothInteractor.NAME, BluetoothInteractor.MY_UUID)
            //socket = device.startClientTest(BluetoothInteractor.MY_UUID).first()
        } catch (e: IOException) {
            println(e.stackTraceToString())
            send(JoinFailure)
            return@channelFlow
        }

        val connectionScope = CoroutineScope(Dispatchers.Default)
        val client = BluetoothClientWrapper(connectionScope, socket.toConnectionWrapper())
        val settings = client.getParams()
        send(Joined(settings, client))
        invokeOnClose {
            println("closed here")
        }
        close()
    }

    val job = scope.launch {
        join.collect { status ->
            when (status) {
                is Loading -> {
                    println("loading")
                }
                is JoinFailure -> {
                    println("failure")
                }
                is Joined -> {
                    println("here")
                    val btClient = status.client
                    val params = status.params

                    assert(params.rows == example.rows) {
                        "rows: expected ${example.rows} but got ${params.rows}"
                    }
                    assert(params.cols == example.cols) {
                        "cols: expected ${example.cols} but got ${params.cols}"
                    }
                    assert(params.win == example.win) {
                        "win: expected ${example.win} but got ${params.win}"
                    }
                    assert(params.creatorMark == example.playerMark) {
                        "mark: expected ${example.playerMark} but got ${params.creatorMark}"
                    }

                    val playerMark = if (isCreator) params.creatorMark else !params.creatorMark
                    val (player1, player2) = if (playerMark == Mark.Cross) {
                        Pair(PlayerType.Bluetooth, PlayerType.Human)
                    } else {
                        Pair(PlayerType.Human, PlayerType.Bluetooth)
                    }

                    val modelScope = CoroutineScope(Dispatchers.Default)
                    val model = NetworkGameModel(
                        params.rows, params.cols, params.win, modelScope, btClient
                    )

                    /*connectClickListener(scope, model, example.moves, player1, player2)
                    consumeGameFlow(this, model, example.moves, example.endSignal)*/
                }
                NeedsPairing -> TODO()
            }
        }
    }

    runBlocking {
        job.join()
    }

    exitProcess(0)
}

fun DeviceManager.getDevice(): com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice {
    setScanFilter(
        mapOf(
            DiscoveryFilter.Transport to DiscoveryTransport.BREDR,
            DiscoveryFilter.DuplicateData to false,
            //DiscoveryFilter.RSSI to (-100).toShort(),
        )
    )
    getDevices(true).firstOrNull { device ->
        device.uuids?.any { UUID.fromString(it) == BluetoothInteractor.MY_UUID } ?: false
    }?.let { return it }

    val deviceList = runBlocking {
        deviceSearchFlow(5000).onEach { dev ->
            dev.uuids.forEachIndexed { i, service ->
                println("${i + 1}: $service")
            }
        }.toList()
    }

    deviceList.forEach { device ->
        val fakeServiceUUID = "00001101-0000-1000-8000-00805F9B34FB"
        // getting full list of device services
        try {
            device.connectProfile(fakeServiceUUID)
        } catch (e: DBusExecutionException) {
            println(e)
            // pass
        } finally {
            device.uuids.forEachIndexed { index, uuid ->
                println("${index + 1}:  $uuid")
            }
        }
        if (device.uuids.any { UUID.fromString(it) == BluetoothInteractor.MY_UUID }) {
            return device
        }
    }
    throw IllegalStateException("searching for bluetooth profile with UUID:${BluetoothInteractor.MY_UUID}, but could not do it")
}