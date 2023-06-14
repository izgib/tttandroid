package com.example.transport.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.ParcelUuid
import com.example.transport.BluetoothInteractor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.time.ExperimentalTime


sealed class BluetoothCommand {
    data class Enable(val respChannel: SendChannel<Boolean>) : BluetoothCommand()
    data class MakeDiscoverable(val timeSec: Int, val respChannel: SendChannel<Boolean>) :
        BluetoothCommand()
}

class BluetoothSensor(private val context: Context) {
    private val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val haveBt = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    private val _commandFlow = MutableStateFlow<BluetoothCommand?>(null)
    val commandFlow: StateFlow<BluetoothCommand?>
        get() = _commandFlow

    fun commandExecuted() {
        _commandFlow.value = null
    }

    fun bluetoothEnabled() = mBluetoothAdapter!!.isEnabled

    fun getPairedDevices(): LinkedHashSet<BluetoothDevice> { //= mBluetoothAdapter!!.bondedDevices.run{
        val bonded = mBluetoothAdapter!!.bondedDevices

        val devices = LinkedHashSet<BluetoothDevice>(bonded.size)
        val checkUUID = ParcelUuid(BluetoothInteractor.MY_UUID)
        println("checked: $checkUUID")
        bonded.forEach { device ->
            device.uuids.forEach { uuid ->
                println("${uuid.uuid}")
            }
            /*if (device.uuids.any { it.uuid == BluetoothInteractor.MY_UUID }) {
                devices.add(device)
            }*/
            devices.add(device)
        }

        return devices
    }

    fun getUpdatedPairedDevices(): Flow<BluetoothDevice> {
        println("updated")
        return testFilterDevices(mBluetoothAdapter!!.bondedDevices, retries = 3)
    }


    suspend fun requestEnable(): Boolean {
        val respChannel = Channel<Boolean>()
        _commandFlow.value = BluetoothCommand.Enable(respChannel)
        return respChannel.receive()
    }


    suspend fun requestMakeDiscoverable(): Boolean {
        val respChannel = Channel<Boolean>()
        _commandFlow.value = BluetoothCommand.MakeDiscoverable(120, respChannel)
        return respChannel.receive()
    }

    fun findDevices(): Flow<BluetoothDevice> {
        println("finding devices")

        return channelFlow {
            val deviceReceiver = DeviceReceiver(this)
            //val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            //context.registerReceiver(deviceReceiver, filter)
            context.run {
                registerReceiver(
                    deviceReceiver,
                    IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
                )
                registerReceiver(
                    deviceReceiver,
                    IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                )
                registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            }

            println("isEnabled: ${mBluetoothAdapter!!.isEnabled}")
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            println("isGpsEnabled: $isGpsEnabled")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                println("bluetooth: ${context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED}")
                println("bluetooth_admin: ${context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED}")
                println("background: ${context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED}")
                println("file_location: ${context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED}")
            }
            println("isDiscovering: ${mBluetoothAdapter.startDiscovery()}")
            println("isDiscoveringProp: ${mBluetoothAdapter.isDiscovering}")
            if (!mBluetoothAdapter.isDiscovering) {
                println("isDiscovering: ${mBluetoothAdapter.startDiscovery()}")
                println("isDiscoveringProp: ${mBluetoothAdapter.isDiscovering}")
            }

            awaitClose {
                if (deviceReceiver.finished) return@awaitClose
                println("closing broadcast received")
                mBluetoothAdapter.cancelDiscovery()
                context.unregisterReceiver(deviceReceiver)
            }
        }
    }

    fun filterDevices(devices: LinkedHashSet<BluetoothDevice>, callback: (Boolean) -> Unit) {
        println("filter devices:")
        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        var changed = false
        var deviceCount = 0

        val receiver = object : BroadcastReceiver() {
            val maxRejectCount = 3
            val rejectedDevices = mutableMapOf<BluetoothDevice, Int>()

            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothDevice.ACTION_UUID) {
                    println("UUIDs received")
                    val deviceExtra: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val devUuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)

                    if (devUuids == null) {
                        val rejectCount = rejectedDevices.getOrPut(deviceExtra) { 1 }
                        if (rejectCount < maxRejectCount) {
                            deviceExtra.fetchUuidsWithSdp()
                            rejectedDevices[deviceExtra] = rejectCount + 1
                        } else {
                            println("can not get UUIDs ${deviceExtra.name}: ${deviceExtra.address}")
                            devices.remove(deviceExtra)
                            changed = true
                            deviceCount--
                            completeCheck()
                        }
                        return
                    }
                    println("${deviceExtra.name}: ${deviceExtra.address}")
                    println(buildString {
                        devUuids.forEach { uuid ->
                            this.appendLine((uuid as ParcelUuid).uuid.toString())
                        }
                    })

                    if (devUuids.none { parcelUuid ->
                            (parcelUuid as ParcelUuid).uuid == BluetoothInteractor.MY_UUID
                        }) {
                        devices.remove(deviceExtra)
                        changed = true
                    } else {
                        println("wanted Device ${deviceExtra.name}: ${deviceExtra.address}")
                    }
                    deviceCount--

                    completeCheck()
                }
            }

            fun completeCheck() {
                if (deviceCount <= 0) {
                    context.unregisterReceiver(this)
                    callback(changed)
                }
            }
        }

        context.registerReceiver(receiver, filter)

        devices.forEach { dev ->
            if (dev.fetchUuidsWithSdp()) {
                deviceCount++
            }
        }
        println("deviceCount: $deviceCount")
        if (deviceCount == 0) {
            callback(changed)
            println("not fetched anything")
            context.unregisterReceiver(receiver)
        }
    }

    suspend fun rescanServices(device: BluetoothDevice, retries: Int = 1): Array<ParcelUuid>? =
        suspendCancellableCoroutine { cont ->
            val filter = IntentFilter(BluetoothDevice.ACTION_UUID)

            var r = retries
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action != BluetoothDevice.ACTION_UUID) throw IllegalStateException("WTF")
                    val deviceExtra: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    println("array: ${intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)}")
                    println(
                        "arrayList: ${
                            intent.getParcelableArrayListExtra<ParcelUuid>(
                                BluetoothDevice.EXTRA_UUID
                            )
                        }"
                    )
                    println("parcelable: ${intent.getParcelableExtra<ParcelUuid>(BluetoothDevice.EXTRA_UUID)}")
                    val uuids =
                        intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)// as Array<ParcelUuid>?
                    println("uuids: $uuids")
                    if (uuids == null) {
                        println("null uuids: $r")
                        r--
                        if (r > 1) deviceExtra.fetchUuidsWithSdp()
                        return
                    }

                    cont.resume(uuids as Array<ParcelUuid>) {
                        context.unregisterReceiver(this)
                    }
                }
            }
            context.registerReceiver(receiver, filter)
            if (!device.fetchUuidsWithSdp()) cont.resume(null) {
                context.unregisterReceiver(receiver)
            }
            cont.invokeOnCancellation {
                context.unregisterReceiver(receiver)
            }
        }


    fun testFilterDevices(devices: Set<BluetoothDevice>, retries: Int = 1): Flow<BluetoothDevice> {
        println("input deviceCount: ${devices.count()}")

        return channelFlow<BluetoothDevice> {
            val filter = IntentFilter(BluetoothDevice.ACTION_UUID).apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
            }

            val devIterator = devices.iterator()

            val receiver = object : BroadcastReceiver() {
                val maxRejectCount = retries
                var prevDevice: BluetoothDevice? = null
                var tries = 0

                init {
                    println("max tries: $maxRejectCount")
                }

                override fun onReceive(context: Context, intent: Intent) {

                    val deviceExtra: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val builder =
                        StringBuilder("device ${deviceExtra.name}:${deviceExtra.address} ")
                    when (intent.action) {
                        BluetoothDevice.ACTION_ACL_CONNECTED -> builder.append("ACL connected")
                        BluetoothDevice.ACTION_ACL_DISCONNECTED -> builder.append("ACL disconnected")
                        BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> builder.append("ACL disconnect requested")
                    }
                    if (intent.action != BluetoothDevice.ACTION_UUID) {
                        println(builder)
                        return
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                    }


                    if (deviceExtra == null) throw IllegalStateException("WTF")
                    if (deviceExtra == prevDevice) {
                        tries++
                    } else {
                        prevDevice = deviceExtra
                        tries = 1
                    }

                    val devUuids = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    if (devUuids == null) {
                        if (maxRejectCount > tries) {
                            println("try: ${tries + 1}")
                            if (!deviceExtra.fetchUuidsWithSdp()) {
                                println("can not fetch uuids: ${deviceExtra.name}::${deviceExtra.address}")
                            } else return
                        }
                        nextDevice()
                        return
                    }

                    println("${deviceExtra.name}: ${deviceExtra.address}")
                    println(buildString
                    {
                        devUuids.forEach { uuid ->
                            this.appendLine((uuid as ParcelUuid).uuid.toString())
                        }
                    })

                    val filtered = devUuids.any { parcelUuid ->
                        (parcelUuid as ParcelUuid).uuid == BluetoothInteractor.MY_UUID
                    }
                    if (filtered) {
                        println("wanted Device ${deviceExtra.name}: ${deviceExtra.address}")
                        trySendBlocking(deviceExtra)
                    }
                    nextDevice()
                }

                fun nextDevice() {
                    object : BluetoothProfile.ServiceListener {
                        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                            BluetoothProfile.SAP
                        }

                        override fun onServiceDisconnected(profile: Int) {
                            TODO("Not yet implemented")
                        }

                    }

                    if (devIterator.hasNext()) {
                        val dev = devIterator.next()
                        println("getting next device ${dev.name}:${dev.address}")

                        if (!dev.fetchUuidsWithSdp()) {
                            println(println("can not fetch ${dev.name}:${dev.address}"))
                        }
                    } else close()
                }

            }

            context.registerReceiver(receiver, filter)
            println("registered")
            receiver.nextDevice()

            awaitClose {
                println("unregistered")
                context.unregisterReceiver(receiver)
            }
        }
    }
}