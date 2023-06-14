package com.example.bluetooth_gradle

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.util.concurrent.TimeUnit

abstract class BluetoothInfoAndroid : BluetoothInfo, DefaultTask() {

    @get:Internal
    abstract val bridge: Property<AndroidDebugBridge>

    @get:Input
    abstract val deviceSearchMs: Property<Long>

    @get:Input
    abstract val enableLocationManager: Property<Boolean>

    @get:Input
    abstract val grantPairingPermission: Property<Boolean>

    init {
        // default device search timeout is 5000 ms
        deviceSearchMs.convention(60000)
        enableLocationManager.convention(false)
        grantPairingPermission.convention(false)
    }

    @Internal
    final override lateinit var deviceName: String
        //get() = btInfo.deviceName
        private set

    @Internal
    final override lateinit var macAddress: String
        //get() = btInfo.macAddress
        private set

    private fun enableLocation(device: IDevice) {
        val receiver = CollectingOutputReceiver()
        val command = "SDK=\$(getprop ro.build.version.sdk); if [ \$SDK -ge 29 ]; then settings put secure location_mode 3; fi"
        device.executeShellCommand(command, receiver)
        println("location enabling - ${receiver.output}")
        receiver.cancel()
    }

    private fun grantPermission(device: IDevice) {
        val receiver = CollectingOutputReceiver()
        val command = "pm grant com.example.transport.test android.permission.BLUETOOTH_PRIVILEGED"
        device.executeShellCommand(command, receiver)
        println("permission granted - ${receiver.output}")
        receiver.cancel()
    }


    private fun receiveBluetoothData(device: com.android.ddmlib.IDevice) {
        println("isOnline: ${device.isOnline}")
        val receiver = CollectingOutputReceiver()
        /*device.executeShellCommand(
            "settings get secure bluetooth_address",
            receiver,
            5L,
            TimeUnit.SECONDS
        )*/
        device.executeShellCommand("settings get secure bluetooth_address", receiver)
        device.executeShellCommand("settings get secure bluetooth_name", receiver)
        val compl = receiver.awaitCompletion(30, TimeUnit.SECONDS)
        println("completed: $compl - ${receiver.output}")


        receiver.output.removeSuffix("\n").split("\n", limit = 2).also { (address, name) ->
            if (address == "null") {
                throw IllegalStateException("device: \"$device\" does not support Bluetooth")
            }
            deviceName = name
            macAddress = address
        }
        receiver.cancel()
    }


    private fun deviceFlow() = channelFlow<IDevice> {
        AndroidDebugBridge.IDebugBridgeChangeListener { }
        val deviceListener = object : AndroidDebugBridge.IDeviceChangeListener {
            override fun deviceConnected(device: IDevice): Unit =
                trySendBlocking(device).getOrThrow()

            override fun deviceDisconnected(device: IDevice) = Unit
            override fun deviceChanged(device: IDevice, state: Int) {
                if (state == IDevice.CHANGE_STATE) trySendBlocking(device)
            }
        }
        AndroidDebugBridge.addDeviceChangeListener(deviceListener)
        println("listener")
        awaitClose { AndroidDebugBridge.removeDeviceChangeListener(deviceListener) }
    }

    private fun IDevice.isSuited() = !isEmulator && isOnline && !isBootLoader


    @TaskAction
    fun getInfo() {
/*        val conTimeout = 30L
        val discTimeout = 10L
        val timeoutUnit = TimeUnit.SECONDS*/

        //println("here")
        //AndroidDebugBridge.initIfNeeded(false)

        /*val bridge = AndroidDebugBridge.createBridge(
            android.adbExecutable.path, false,
            conTimeout, timeoutUnit
        )*/
        val device: IDevice = bridge.get().run {
            // wait for device initialization
            Thread.sleep(50)
            if (hasInitialDeviceList()) {
                println("has devices")
                return@run devices.firstOrNull { it.isSuited() }
            }
            return@run null
        } ?: runBlocking {
            val time = deviceSearchMs.get()
            withTimeoutOrNull(time) {
                deviceFlow().onEach {
                    logger.info("found device ${it.serialNumber}: ${it.state?.state}}")
                }.first<IDevice> { it.isSuited() }
            } ?: throw IllegalStateException("could not find suited device for ${time}ms")
        }

/*        val device: IDevice = bridge.get().devices.let { devices ->
            if (devices.isEmpty()) {
                throw IllegalStateException("devices must be connected")
            }

            var isEmulator = false
            var isInaccessible = false
            for (dev in devices) {
                when {
                    dev.isEmulator -> isEmulator = true
                    !dev.isOnline -> isInaccessible = true
                    dev.isOnline -> return@let dev
                }
            }
            if (isEmulator) {
                if (isInaccessible) {
                    throw IllegalStateException("connected devices are emulators or currently inaccessible")
                }
                throw IllegalStateException("connected device is emulator")
            }
            if (isInaccessible) {
                val pllForm = if (devices.count() > 1) "devices are" else "device is"
                throw IllegalStateException("connected $pllForm currently inaccessible")
            }
            throw IllegalStateException()
        }*/
        receiveBluetoothData(device)

        if (enableLocationManager.get()) {
            enableLocation(device)
        }
        if (grantPairingPermission.get()) {
            grantPermission(device)
        }

/*        if (!AndroidDebugBridge.disconnectBridge(discTimeout, timeoutUnit)) {
            println("WTF")
        }*/
        val discTimeout = 5L
        val timeoutUnit = TimeUnit.SECONDS
        if (!AndroidDebugBridge.disconnectBridge(discTimeout, timeoutUnit)) {
            logger.log(LogLevel.INFO, "can not disconnect debug bridge")
        }
    }
}