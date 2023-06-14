package com.example.transport

import com.example.controllers.GameSettings
import com.example.controllers.models.BluetoothServerGameModel
import com.example.controllers.models.PlayerType
import com.example.controllers.winX
import com.example.game.Mark
import com.example.game.not
import com.example.transport.impl.Agent1Impl
import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import org.bluez.AgentManager1
import org.bluez.Device1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.Variant
import kotlin.system.exitProcess

fun main() {
    val deviceManager = DeviceManager.createInstance(false)
    val agentManager =
        DbusHelper.getRemoteObject(
            deviceManager.dbusConnection,
            "/org/bluez",
            AgentManager1::class.java
        )
    val btAgent = Agent1Impl(deviceManager.adapter)

    agentManager.RegisterAgent(DBusPath(btAgent.objectPath), "")
    agentManager.RequestDefaultAgent(DBusPath(btAgent.objectPath))
    deviceManager.dbusConnection.exportObject(btAgent.objectPath, btAgent)

    Runtime.getRuntime().addShutdownHook(Thread() {
        println("exit")
        agentManager.UnregisterAgent(DBusPath(btAgent.objectPath))
        deviceManager.closeConnection()
    })

    deviceManager.apply {
        registerPropertyHandler(
            object : AbstractPropertiesChangedHandler() {
                override fun handle(properties: Properties.PropertiesChanged) {
                    if (properties.interfaceName != Device1::class.java.name) {
                        return
                    }

                    val deviceInt =
                        DbusHelper.getRemoteObject(
                            dbusConnection,
                            properties.path,
                            Device1::class.java
                        )
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
                                        substring(indexOfFirst { it == '_' } + 1,
                                            count()).replace(
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

    //val socket = deviceManager.startServer(BluetoothInteractor.MY_UUID, BluetoothInteractor.NAME)
    val example = winX.toNetworkTest(Mark.Cross)
    val settings =
        GameSettings(example.rows, example.cols, example.win, example.playerMark)
    val isCreator = true
    val scope = CoroutineScope(Dispatchers.Default)

    val creator = channelFlow<GameCreateStatus> {
        send(Awaiting)

        val socket: BluetoothSocket
        try {
            //con = deviceManager.startServer(BluetoothInteractor.NAME, BluetoothInteractor.MY_UUID).first()
            socket = deviceManager.startServer(BluetoothInteractor.NAME, BluetoothInteractor.MY_UUID).first()
            println("here")
            val connectionScope = CoroutineScope(Dispatchers.Default)
            val wrapper = BluetoothServerWrapper(connectionScope, socket.toConnectionWrapper())
            wrapper.sendParams(settings)
            send(Connected(wrapper))
        } catch (e: Throwable) {
            println(e)
            send(CreatingFailure)
        } finally {
            close()
        }
    }


    val job = scope.launch {
        BluetoothInteractorImpl.createGame(settings).collect { status ->
            when (status) {
                is Awaiting -> {}
                is CreatingFailure -> {
                }
                is Connected -> {
                    val btServer = status.server
                    val playerMark =
                        if (isCreator) example.playerMark else !example.playerMark
                    val (player1, player2) = if (playerMark == Mark.Cross) {
                        Pair(PlayerType.Human, PlayerType.Network)
                    } else {
                        Pair(PlayerType.Network, PlayerType.Human)
                    }

                    val modelScope = CoroutineScope(Dispatchers.Default)
                    val model = BluetoothServerGameModel(
                        example.rows, example.cols,
                        example.win, modelScope, btServer
                    )
                    /*connectClickListener(scope, model, example.moves, player1, player2)
                    consumeGameFlow(this, model, example.moves, example.endSignal)*/
                }
            }
        }
    }

    runBlocking {
        job.join()
        delay(1000)
        //agentManager.UnregisterAgent(DBusPath(btAgent.objectPath))
        //println("agent unregistered")
    }

    exitProcess(0)
}