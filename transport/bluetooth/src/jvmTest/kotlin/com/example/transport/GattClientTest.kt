package com.example.transport

import com.example.controllers.DummyBot
import com.example.controllers.GameSettings
import com.example.controllers.consumeFlow
import com.example.controllers.models.NetworkGameModel
import com.example.controllers.winX
import com.example.transport.extensions.toGameSettings
import com.example.transport.impl.Agent1Impl
import com.example.transport.service.searchLEDevices
import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.bluez.*
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

    val agentManager = DbusHelper.getRemoteObject(
        deviceManager.dbusConnection, "/org/bluez", AgentManager1::class.java
    )
    val btAgent = Agent1Impl(adapter)
    deviceManager.devices

    agentManager.RegisterAgent(DBusPath(btAgent.objectPath), "")
    agentManager.RequestDefaultAgent(DBusPath(btAgent.objectPath))
    adapter.dbusConnection.exportObject(btAgent.objectPath, btAgent)


    val gameItem = runBlocking {
        withTimeout(10000) {
            deviceManager.searchAdvertisingData()
        }.first { it.settings != null }
    }

    println(gameItem)
    deviceManager.adapter.isDiscoverable = false

    val gameDevice = gameItem.device as com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice
    println(gameDevice)

    if (!gameDevice.isConnected) {
        println("not connected")
        gameDevice.connect()
    }
    println("resolved: ${gameDevice.isServicesResolved}")

    if (!gameDevice.isServicesResolved) {
        println("not resolved")
        runBlocking {
            deviceManager.discoverServices(gameDevice)
        }
    } else {
        println("resolved")
    }

    var clientMessages: BluetoothGattCharacteristic? = null
    var serverMessages: BluetoothGattCharacteristic? = null
    val service: BluetoothGattService? =
        DbusHelper.findNodes(deviceManager.dbusConnection, gameDevice.dbusPath).run {
            forEach { node ->
                val prefix = "service"
                if (!node.startsWith(prefix)) return
                node.substring(prefix.length).toIntOrNull() ?: return
                val nodePath = "${gameDevice.dbusPath}/$node"
                val serviceInt = DbusHelper.getRemoteObject(
                    deviceManager.dbusConnection, nodePath, GattService1::class.java
                )
                val service = BluetoothGattService(
                    serviceInt, gameDevice, nodePath, deviceManager.dbusConnection
                )
                if (service.uuid == BluetoothLEInteractor.service.toString()) {
                    val settings =
                        service.gattCharacteristics.firstOrNull { it.uuid == BluetoothLEInteractor.settings.toString() }
                    requireNotNull(settings)
                    clientMessages =
                        service.gattCharacteristics.firstOrNull { it.uuid == BluetoothLEInteractor.clientMessages.toString() }
                    requireNotNull(clientMessages)
                    serverMessages =
                        service.gattCharacteristics.firstOrNull { it.uuid == BluetoothLEInteractor.serverMessages.toString() }
                    requireNotNull(serverMessages)

                    return@run service
                }
            }
            return@run null
        }

    val logHandler = object : AbstractPropertiesChangedHandler() {
        val listening = listOf(
            GattCharacteristic1::class.java.name,
            GattService1::class.java.name,
            GattDescriptor1::class.java.name,
            Device1::class.java.name
        )

        override fun handle(properties: Properties.PropertiesChanged) {
            if (properties.interfaceName !in listening) return
            properties.propertiesChanged.forEach { property, value ->
                println("${properties.interfaceName}: $property -- $value")
            }
        }
    }
    deviceManager.registerSignalHandler(logHandler)

    requireNotNull(service)
    runBlocking {
        serverMessages!!.onNotify()
    }

    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        /*val joined: ClientJoined = app.announceGame().onEach { state ->
            when (state) {
                is ClientJoined -> {
                    println("client joined")
                }
                is Started -> println("advertising started")
                is Failed -> println("advertising failed")
            }
        }.last() as ClientJoined*/
        val responseChannel = onResponse(serverMessages!!)
        val client = BluetoothLEClientWrapper(clientMessages!!, responseChannel)

        val model = with(gameItem.settings!!) {
            NetworkGameModel(rows, cols, win, this@launch, client)
        }
        //val moveRegister = MoveRegister(model)
        //moveRegister.consumeMoves(scope, winX.moves)
        println(gameItem.settings)
        val player = DummyBot(winX.moves.filterIndexed { index, _ -> (index and 1) == 1 })
        model.setupPlayerO(player)
        model.consumeFlow(scope, winX.moves, winX.endSignal)
    }


    fun closeConnection() {
        deviceManager.unRegisterPropertyHandler(logHandler)
        /*advManager.unregisterAdvertisement(DBusPath(adv.objectPath))
        deviceManager.dbusConnection.unExportObject(adv.objectPath)*/
        //deviceManager.unRegisterPropertyHandler(propertyHandler)
        deviceManager.closeConnection()
    }

    Signal.handle(Signal("INT")) { signal ->
        println("Interrupted by Ctrl+C")
        closeConnection()
        //exitProcess(0)
    }
    println("here in end")
}

/*suspend fun DeviceManager.discoverServices(device: com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice) =
    suspendCancellableCoroutine<Unit> { continuation ->
        val deviceSearchHandler = object : AbstractPropertiesChangedHandler() {
            override fun handle(properties: Properties.PropertiesChanged) {
                properties.onInterface(Device1::class) {
                    propertiesChanged.forEach { key, value ->
                        println("$key: $value")
                    }
                    if (properties.property<Boolean>("ServicesResolved") ?: return) {
                        continuation.resume(Unit) {
                            println("canceled")
                        }
                    }
                }
            }
        }

        registerSignalHandler(deviceSearchHandler)
        device.refreshGattServices()
        //deviceManager.registerPropertyHandler(deviceChangeHandler)
        continuation.invokeOnCancellation {
            println("canceled discover")
            unRegisterPropertyHandler(deviceSearchHandler)
        }
    }*/

fun DeviceManager.searchAdvertisingData(): Flow<BluetoothGameItem> {
    return searchLEDevices(BluetoothLEInteractor.service).map { adv ->
        val settings = adv.serviceData?.get(BluetoothLEInteractor.settings.toString())?.let {
            GameParams.parseFrom(it).toGameSettings().run {
                GameSettings(rows, cols, win, creatorMark)
            }
        }
        BluetoothGameItem(adv.device, settings)
    }
}

/*fun DeviceManager.searchLEDevices(UUIDs: Array<String>) = callbackFlow<BluetoothGameItem> {
    val games = HashMap<String, BluetoothGameItem>()

    setScanFilter(
        mapOf(
            DiscoveryFilter.Transport to DiscoveryTransport.LE,
            DiscoveryFilter.DuplicateData to true,
            DiscoveryFilter.UUIDs to UUIDs,
        )
    )

    val deviceSearchHandler = object : AbstractInterfacesAddedHandler() {
        override fun handle(s: ObjectManager.InterfacesAdded) {
            println("here int")
            s.interfaces.forEach { int, props ->
                println("$int: $props")
            }
            s.onInterface(Device1::class) {
                val devInt = dbusConnection.getBluetoothDeviceInt(signalSource.path)
                val device = BluetoothDevice(devInt, adapter, devInt.objectPath, dbusConnection)
                if (device.uuids.none { uuid -> uuid == BluetoothLEInteractor.service.toString() }) {
                    return
                }

                val data = device.serviceData[BluetoothLEInteractor.settings.toString()]
                val settings = data?.let {
                    GameParams.parseFrom(it).toGameSettings().run {
                        GameSettings(rows, cols, win, !creatorMark)
                    }
                }
                val gameItem = BluetoothGameItem(device, settings)
                games[device.dbusPath] = gameItem

                trySend(gameItem)
            }
            if (s.interfaces[Device1::class.java.name] != null) {
                val int = dbusConnection.getBluetoothDeviceInt(s.signalSource.path)
                val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)
                if (device.uuids.none { uuid -> uuid == BluetoothLEInteractor.service.toString() }) {
                    return
                }

                val data = device.serviceData[BluetoothLEInteractor.settings.toString()]
                val settings = data?.let {
                    GameParams.parseFrom(it).toGameSettings().run {
                        GameSettings(rows, cols, win, !creatorMark)
                    }
                }

                val gameItem = BluetoothGameItem(device, settings)
                games[device.dbusPath] = gameItem

                trySend(gameItem)
            }
        }
    }

    val changed = propertyListener lst@{
        when (interfaceName) {
            Adapter1::class.java.name -> if (property<Boolean>("Discovering") ?: return@lst) close()
            Device1::class.java.name -> {
                val int = dbusConnection.getBluetoothDeviceInt(path)
                val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)
                if (device.uuids.none { uuid -> uuid == BluetoothLEInteractor.service.toString() }) {
                    return@lst
                }
                val serviceData = property<Map<String, Variant<Variant<ByteArray>>>>("ServiceData")

                val settings: GameSettings? = serviceData?.let { sData ->
                    val data =
                        (sData[BluetoothLEInteractor.settings.toString()] as Variant<ByteArray>?)?.value
                            ?: return@let null
                    GameParams.parseFrom(data).toGameSettings().run {
                        GameSettings(rows, cols, win, !creatorMark)
                    }
                }

                if (games[device.dbusPath]?.settings == settings)
            }
        }
    }

    val deviceChangeHandler = object : AbstractPropertiesChangedHandler() {
        override fun handle(props: Properties.PropertiesChanged) {
            println("here prop")
            println(props.interfaceName)
            props.propertiesChanged.forEach { prop, value ->
                println("$prop: $value")
            }
            if (props.interfaceName == Adapter1::class.java.name) {
                props.propertiesChanged["Discovering"]?.let { discovering ->
                    val isDiscovering = (discovering as Variant<Boolean>).value
                    if (!isDiscovering) {
                        close()
                    }
                }
            }

            if (props.interfaceName == Device1::class.java.name) {
                val int = dbusConnection.getBluetoothDeviceInt(props.path)
                val device = BluetoothDevice(int, adapter, int.objectPath, dbusConnection)

                if (device.uuids.none { uuid -> uuid == BluetoothLEInteractor.service.toString() }) {
                    return
                }

                val serviceData =
                    props.propertiesChanged["ServiceData"]?.value as Map<String, Variant<Variant<ByteArray>>>?

                val settings: GameSettings? = serviceData?.let { sData ->
                    val data =
                        (sData[BluetoothLEInteractor.settings.toString()] as Variant<ByteArray>?)?.value
                            ?: return@let null
                    GameParams.parseFrom(data).toGameSettings().run {
                        GameSettings(rows, cols, win, !creatorMark)
                    }
                }


                if (games[device.dbusPath]?.settings == settings) return
                val gameItem = BluetoothGameItem(device, settings)
                games[device.dbusPath] = gameItem

                trySend(gameItem)
            }
        }

    }
    registerPropertyHandler(deviceChangeHandler)
    registerSignalHandler(deviceSearchHandler)

    if (!adapter.startDiscovery()) {
        throw IllegalStateException("can not start service discovery")
    }
    println("discovery started")

    awaitClose {
        unRegisterSignalHandler(deviceSearchHandler)
        unRegisterPropertyHandler(deviceChangeHandler)
        adapter.stopDiscovery()
    }
}*/


