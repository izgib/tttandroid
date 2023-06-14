package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.BluetoothServerGameModel
import com.example.transport.impl.*
import com.example.transport.service.*
import com.example.transport.service.Application
import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.bluez.AgentManager1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.freedesktop.dbus.interfaces.Properties
import sun.misc.Signal

fun main() {
    val deviceManager = DeviceManager.createInstance(false)

    val adapter = deviceManager.adapter.apply {
        isPowered = true
    }
    println("name: ${deviceManager.dbusConnection.uniqueName}")

    val agentManager =
        DbusHelper.getRemoteObject(
            deviceManager.dbusConnection,
            "/org/bluez",
            AgentManager1::class.java
        )
    val btAgent = Agent1Impl(adapter)

    agentManager.RegisterAgent(DBusPath(btAgent.objectPath), "")
    agentManager.RequestDefaultAgent(DBusPath(btAgent.objectPath))
    adapter.dbusConnection.exportObject(btAgent.objectPath, btAgent)

    val propertyHandler = object : AbstractPropertiesChangedHandler() {
        override fun handle(properties: Properties.PropertiesChanged) {
            if (!properties.interfaceName.startsWith("org.bluez")) return
            properties.propertiesChanged.forEach { (key, variant) ->
                println("${properties.interfaceName}    key: $key - $variant")
            }
        }
    }
    deviceManager.registerPropertyHandler(propertyHandler)

    val app = Application(deviceManager)
    app.exportDbusObjects(deviceManager.dbusConnection)
    deviceManager.adapter.isDiscoverable
    app.registerApplication()

    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        val joined: ClientJoined = app.announceGame().onEach { state ->
            when (state) {
                is ClientJoined -> {
                    println("client joined")
                }
                is Started -> println("advertising started")
                is Failed -> println("advertising failed")
            }
        }.last() as ClientJoined
        val model = with(app.settings) {
            BluetoothServerGameModel(rows, cols, win, this@launch, joined.server)
        }
        val moveRegister = MoveRegister(model)
        moveRegister.consumeMoves(scope, winX.moves)
        val player = DummyBot(winX.moves.filterIndexed { index, _ -> (index and 1) == 0 })
        model.setupPlayerX(player)
        model.consumeFlow(scope, winX.moves, winX.endSignal)
    }

    /*val advPath = "/game/advertisement"
    val adv = Advertisement(app, advPath).apply {
        addServiceUUID(gattUUID.toString())
        addServiceData(charUUID.toString(), byteArrayOf(0x01))
    }
    val advProps = adv.GetAll(Properties::class.java.name)
    println(advProps)
    println()
    deviceManager.dbusConnection.exportObject(adv)

    val advManager = deviceManager.adapter.LEAdvertisingManager
    advManager.registerAdvertisement(DBusPath(adv.objectPath), mapOf())*/


    //advManager.registerAdvertisement(DBusPath(adv.objectPath), mapOf("privacy" to Variant("off")))

/*    deviceManager.adapter.isDiscoverable = true
    deviceManager.setScanFilter(
        mapOf(
            DiscoveryFilter.Transport to DiscoveryTransport.LE,
            DiscoveryFilter.DuplicateData to true,
        )
    )
    val deviceList = runBlocking {
        deviceManager.deviceSearchFlow(60000).onEach { dev ->
            dev.refreshGattServices()
            println("data: ${dev.serviceData}")
            dev.manufacturerData?.forEach { t, u ->
                println("manData $t - [$u]")
            }

            println("flags: ${dev.advertisingFlags?.toList()?.joinToString()}")
            println("${dev.name}: ${dev.address}")
            dev.uuids.forEachIndexed { i, service ->
                println("${i + 1}: $service")
            }
        }.toList()
    }
    println(deviceList.count())
    deviceManager.adapter.isDiscoverable = false*/

    /*println("instances: ${advManager.supportedInstances}")
    println("act instances: ${advManager.activeInstances}")
    println("includes:")
    advManager.supportedIncludes?.forEach {
        println(it)
    }*/

    fun closeConnection() {
        /*advManager.unregisterAdvertisement(DBusPath(adv.objectPath))
        deviceManager.dbusConnection.unExportObject(adv.objectPath)*/
        deviceManager.unRegisterPropertyHandler(propertyHandler)
        app.unregisterApplication()
        app.unExportDbusObjects(deviceManager.dbusConnection)
        deviceManager.closeConnection()
    }

    Signal.handle(Signal("INT")) { signal ->
        println("Interrupted by Ctrl+C")
        closeConnection()
        //exitProcess(0)
    }
    println("here in end")
}

