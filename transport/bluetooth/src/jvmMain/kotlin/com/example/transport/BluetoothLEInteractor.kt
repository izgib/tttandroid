package com.example.transport

import com.example.controllers.GameSettings
import com.example.game.not
import com.example.transport.bluez.property
import com.example.transport.bluez.propertyListener
import com.example.transport.extensions.toGameSettings
import com.example.transport.impl.gattManager
import com.example.transport.service.Application
import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattService
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import org.bluez.Device1
import org.bluez.GattCharacteristic1
import org.bluez.GattService1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.freedesktop.dbus.interfaces.Properties
import kotlin.coroutines.resume

class BluetoothLEInteractorImpl(private val deviceManager: DeviceManager) : BluetoothLEInteractor {
    override fun connectGame(
        device: BluetoothDevice,
        gameParams: GameSettings?
    ) = channelFlow<ConnectionStatus> {
        send(Connecting)
        if (!device.isConnected) {
            println("not connected")
            device.connect()
        }
        println("resolved: ${device.isServicesResolved}")

        if (!device.isServicesResolved) {
            println("not resolved")
            runBlocking {
                deviceManager.discoverServices(device)
            }
        } else {
            println("resolved")
        }

        var gameSettings: GameSettings? = null
        var clientMessages: BluetoothGattCharacteristic? = null
        var serverMessages: BluetoothGattCharacteristic? = null
        val service: BluetoothGattService? =
            DbusHelper.findNodes(deviceManager.dbusConnection, device.dbusPath).run {
                forEach { node ->
                    val prefix = "service"
                    if (!node.startsWith(prefix)) return@forEach
                    node.substring(prefix.length).toIntOrNull() ?: return@forEach
                    val nodePath = "${device.dbusPath}/$node"
                    val serviceInt = DbusHelper.getRemoteObject(
                        deviceManager.dbusConnection, nodePath, GattService1::class.java
                    )
                    val service = BluetoothGattService(
                        serviceInt, device, nodePath, deviceManager.dbusConnection
                    )
                    if (service.uuid == BluetoothLEInteractor.service.toString()) {
                        var settings =
                            service.gattCharacteristics.firstOrNull { it.uuid == BluetoothLEInteractor.settings.toString() }
                        requireNotNull(settings)
                        clientMessages =
                            service.gattCharacteristics.firstOrNull { it.uuid == BluetoothLEInteractor.clientMessages.toString() }
                        requireNotNull(clientMessages)
                        serverMessages =
                            service.gattCharacteristics.firstOrNull { it.uuid == BluetoothLEInteractor.serverMessages.toString() }
                        requireNotNull(serverMessages)
                        gameSettings = gameParams ?: GameParams.parseFrom(settings!!.value).toGameSettings().run {
                            GameSettings(rows, cols, win, !creatorMark)
                        }


                        return@run service
                    }
                }
                return@run null
            }

        requireNotNull(service)
        runBlocking {
            serverMessages!!.onNotify()
        }

        val scope = CoroutineScope(Dispatchers.Default)
        val responses = scope.onResponse(serverMessages!!)
        send(ConnectedGame(gameSettings!!, BluetoothLEClientWrapper(clientMessages!!, responses)))
        close()
    }


    override fun createService(params: GameSettings): Flow<ServiceStatus> = channelFlow {
        var app: Application? = null
        try {
            app = Application(deviceManager)
            app.service.settings.settings = params
            deviceManager.adapter.gattManager()
                .registerApplication(DBusPath(app.objectPath), mapOf())
        } catch (e: Throwable) {
            trySendBlocking(InitializationFailure)
            deviceManager.adapter.gattManager().unregisterApplication(DBusPath(app!!.objectPath))
            close(e)
        }
    }

    override fun createApplication(params: GameSettings): com.example.transport.Application {
        return Application(deviceManager).apply {
            settings = params
        }

    }

    override fun getDeviceList(): Flow<BluetoothGameItem> = callbackFlow<BluetoothGameItem> {
    }
}

suspend fun DeviceManager.discoverServices(device: BluetoothDevice) =
    suspendCancellableCoroutine<Unit> { continuation ->
        val discover = object : AbstractPropertiesChangedHandler() {
            override fun handle(s: Properties.PropertiesChanged)  {
                if (s.interfaceName == Device1::class.java.name)
                    if (s.property<Boolean>("ServicesResolved") ?: return) {
                        println("in resolved block")
                        unRegisterPropertyHandler(this)
                        continuation.resume(Unit)
                    }
            }
        }
        if (device.isServicesResolved) {
            println("resolved check")
            continuation.resume(Unit)
            continuation.cancel()
            return@suspendCancellableCoroutine
        }
        continuation.invokeOnCancellation {
            println("discovery closed")
            unRegisterPropertyHandler(discover)
        }
        registerPropertyHandler(discover)

        device.refreshGattServices()
/*        continuation.invokeOnCancellation {
            unRegisterPropertyHandler(tDiscover)
        }
        registerPropertyHandler(tDiscover)*/
    }

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun BluetoothGattCharacteristic.onNotify() = suspendCancellableCoroutine { continuation ->
    val notify = object : AbstractPropertiesChangedHandler() {
        override fun handle(s: Properties.PropertiesChanged) {
            if (s.interfaceName == GattCharacteristic1::class.java.name) {
                if (s.property<Boolean>("Notifying") ?: return) {
                    dbusConnection.removeSigHandler(implementationClass, this)
                    continuation.resume(Unit) {}
                }
            }
        }

    }

    dbusConnection.addSigHandler(notify.implementationClass, notify)
    startNotify()
    println("started notifying")
    //val cccd = getGattDescriptorByUuid(UUID16Bit(0x2902u).toUUID().toString())!!
    continuation.invokeOnCancellation {
        if (it == null) {
            stopNotify()
        }
        dbusConnection.removeSigHandler(notify.implementationClass, notify)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineScope.onResponse(characteristic: BluetoothGattCharacteristic) = produce(capacity = 3) {
    require(characteristic.uuid == BluetoothLEInteractor.serverMessages.toString())
    val responses = propertyListener lst@{
        if (interfaceName == GattCharacteristic1::class.java.name) {
            val msg = BluetoothCreatorMsg.parseFrom(property<ByteArray>("Value") ?: return@lst)
            try {
                val resp = msg.toResponse()
                if (!trySend(resp).isSuccess) println("can not send response")
            } catch (e: Throwable) {
                close(e)
            }
        }
    }
    characteristic.dbusConnection.addSigHandler(
        responses.implementationClass, responses
    )

    awaitClose {
        println("stop notifying")
        characteristic.stopNotify()
        characteristic.dbusConnection.removeSigHandler(responses.implementationClass, responses)
    }
}
