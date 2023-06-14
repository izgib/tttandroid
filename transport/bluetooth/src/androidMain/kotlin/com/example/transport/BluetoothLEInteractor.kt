package com.example.transport

import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import com.example.controllers.ClientResponse
import com.example.controllers.GameSettings
import com.example.controllers.models.Response
import com.example.transport.BluetoothLEInteractor.Companion.service
import com.example.transport.device.*
import com.example.transport.extensions.toGameSettings
import com.example.transport.extensions.toMark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback as ScanCallbackCompat
import no.nordicsemi.android.support.v18.scanner.ScanFilter as ScanFilterCompat
import no.nordicsemi.android.support.v18.scanner.ScanSettings as ScanSettingsCompat


@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEInteractorImpl(private val context: Context) : BluetoothLEInteractor {
    private var bluetoothManager: BluetoothManager? = null
    val haveBluetoothLE: Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        } else false

    override fun createService(params: GameSettings): Flow<ServiceStatus> = channelFlow {
        if (!haveBluetoothLE) throw IllegalStateException("does not have bluetooth low energy")

        val serverCallback = object : BluetoothGattServerCallback() {
            val settings = SettingsCharacteristic(params)
            val clientMessages = ClientMessageCharacteristic()
            val serverMessages = ServerMessageCharacteristic()
            val serverNotifications = ServerMessageNotifications()
            val service = GameService(settings, clientMessages, serverMessages)

            private val clientRsp = Channel<ClientResponse>()

            lateinit var server: BluetoothGattServer

            override fun onConnectionStateChange(
                device: android.bluetooth.BluetoothDevice,
                status: Int,
                newState: Int
            ) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> println("$device: connected")
                    BluetoothProfile.STATE_DISCONNECTED -> println("$device: disconnected")
                }
            }

            override fun onServiceAdded(status: Int, service: BluetoothGattService) {
                require(service == this.service)
                require(status == BluetoothGatt.GATT_SUCCESS) { "unexpected status: ${status}" }
            }

            override fun onCharacteristicReadRequest(
                device: android.bluetooth.BluetoothDevice,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic
            ) {
                require(characteristic == settings) { "unexpected characteristic: $characteristic" }
                server.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset, settings.value
                )
            }

            override fun onCharacteristicWriteRequest(
                device: android.bluetooth.BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                require(characteristic == clientMessages)
                require(!preparedWrite)
                println("responseNeeded: $responseNeeded")
                println("served: $server")
                ClientMessage.parseFrom(value)
                server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }

            override fun onDescriptorWriteRequest(
                device: android.bluetooth.BluetoothDevice,
                requestId: Int,
                descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                require(descriptor.uuid == serverNotifications.uuid) { "unexpected descriptor: $descriptor" }
                require(descriptor.characteristic == serverMessages) { "unexpected characteristic: ${descriptor.characteristic}" }
                require(!preparedWrite)
                println("responseNeeded: $responseNeeded")

                if (value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                    trySendBlocking(
                        ServiceInstance(
                            BluetoothLEServerWrapper(
                                device,
                                server,
                                serverMessages,
                                clientRsp
                            )
                        )
                    )
                } else {
                    server.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, offset, null)
                    throw IllegalStateException("unexpected value: $value")
                }
            }

            override fun onNotificationSent(
                device: android.bluetooth.BluetoothDevice,
                status: Int
            ) {
                require(status == BluetoothGatt.GATT_SUCCESS)
            }
        }

        try {
            send(Initialized)


            val server = bluetoothManager!!.openGattServer(context, serverCallback).apply {
                if (addService(serverCallback.service)) {
                    serverCallback.server = this
                } else {
                    throw IllegalStateException()
                }
            }

        } catch (e: Throwable) {
            send(InitializationFailure)
        }
    }

    override fun connectGame(device: BluetoothDevice, gameParams: GameSettings?) =
        channelFlow<ConnectionStatus> {
            var dontClose = false

            println("before connect LE")
            val connectObject = object : BluetoothGattCallback() {
                var params: GameSettings? = gameParams
                var settings: BluetoothGattCharacteristic? = null
                var clientMessages: BluetoothGattCharacteristic? = null
                var serverMessages: BluetoothGattCharacteristic? = null
                var serverNotifications: BluetoothGattDescriptor? = null

                val operationEnd = Channel<Unit>()
                val serverRsp = Channel<Response>()


                private fun setupMsgNotification(gatt: BluetoothGatt) {
                    println("connecting message characteristic")
                    serverNotifications!!.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                    gatt.writeDescriptor(serverNotifications)
                }

                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    println(
                        "status: $status[${
                            String.format(
                                "%02x",
                                status
                            )
                        }], newState: $newState"
                    )
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                if (gatt.discoverServices()) {
                                    println("discovering started")
                                } else {
                                    println("failed to start discovery")
                                }
                            }
                            return
                        }

                        BluetoothGatt.GATT_CONNECTION_CONGESTED -> {

                        }

                        BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {

                        }
                        // GATT_ERROR
                        133 -> {
                            println("error 133")
                        }

                        BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {

                        }
                    }
                    close(IllegalStateException("with status: $status and state: $newState"))

                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> {
                            println("discovered success")

                            gatt.getService(service)?.let { service ->
                                settings = service.getCharacteristic(BluetoothLEInteractor.settings)
                                clientMessages =
                                    service.getCharacteristic(BluetoothLEInteractor.clientMessages)
                                serverMessages =
                                    service.getCharacteristic(BluetoothLEInteractor.serverMessages)
                                        .also {
                                            serverNotifications =
                                                it.getDescriptor(UUID16Bit(0x2902u).toUUID())
                                        }
                                if (settings == null) throw IllegalStateException()
                                if (serverMessages == null) throw IllegalStateException()
                                if (clientMessages == null) throw IllegalStateException()
                                if (serverNotifications == null) throw IllegalStateException()
                                println("service: ${service.uuid}")
                                service.characteristics.forEach { char ->
                                    println("    characteristic: ${char.uuid}")
                                    char.descriptors.forEach { desc ->
                                        println("        descriptor: ${desc.uuid}")
                                    }
                                }
                            }

                            if (params == null) {
                                gatt.readCharacteristic(settings)
                            }
                            setupMsgNotification(gatt)
                            return
                        }


                        BluetoothGatt.GATT_CONNECTION_CONGESTED -> {

                        }

                        BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {

                        }
                        // GATT_ERROR
                        133 -> {
                            println("error 133")

                        }

                        BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {

                        }
                    }
                    close(IllegalStateException("with status: $status"))
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (characteristic != settings) throw IllegalStateException()

                    params = with(GameParams.parseFrom(characteristic.value)) {
                        GameSettings(rows, cols, win, mark.toMark())
                    }
                    setupMsgNotification(gatt)
                }

                override fun onCharacteristicWrite(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    when (status) {
                        BluetoothGatt.GATT_SUCCESS -> operationEnd.trySendBlocking(Unit)
                        else -> throw IllegalStateException("$status")
                    }
                }


                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic
                ) {
                    println("got notification")
                    if (characteristic != serverMessages) throw IllegalStateException("require $serverMessages characteristic, but got $characteristic")
                    serverRsp.trySend(
                        BluetoothCreatorMsg.parseFrom(characteristic.value).toResponse()
                    )
                }

                override fun onDescriptorRead(
                    gatt: BluetoothGatt,
                    descriptor: BluetoothGattDescriptor,
                    status: Int
                ) {

                }

                override fun onDescriptorWrite(
                    gatt: BluetoothGatt,
                    descriptor: BluetoothGattDescriptor,
                    status: Int
                ) {
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        println("something went wrong: try to retry: ${descriptor.characteristic.uuid}: ${descriptor.uuid}")
                        //setupMsgNotification(gatt)
                        return
                    }
                    println("written successfully")

                    require(descriptor == serverNotifications)
                    gatt.setCharacteristicNotification(serverMessages, true)
                    println("notifying request")
                    dontClose = true
                    val wrapper =
                        BluetoothLEClientWrapper(
                            gatt,
                            clientMessages!!,
                            operationEnd,
                            serverRsp
                        )
                    if (trySend(
                            ConnectedGame(
                                params!!,
                                wrapper
                            )
                        ).isFailure
                    ) throw IllegalStateException("WTF")
                    println("game sent")
                    close()
                }
            }

            send(Connecting)
            var gatt: BluetoothGatt? = null
            try {
                gatt = withContext(Dispatchers.Main) {
                    device.connectGatt(context, false, connectObject)
                }
                //gatt = device.connectGatt(context, false, connectObject)
                /*                device.connectGatt(context, false, connectObject, TRANSPORT_LE, android.bluetooth.BluetoothDevice.PHY_LE_1M)
                                device.connectGatt(context, false, connectObject, TRANSPORT_LE, android.bluetooth.BluetoothDevice.PHY_LE_1M, Handler())
                                gatt = device.connectGatt(context, false, connectObject, TRANSPORT_LE)*/
            } catch (e: Throwable) {
                println(e)
                send(ConnectingFailure)
                close(e)
            }
            println("here")

            awaitClose {
                println("closing")
                if (dontClose) return@awaitClose
                runBlocking(Dispatchers.Main) {
                    gatt?.close()
                }
            }
        }

    override fun createApplication(params: GameSettings): Application {
        return Application(context)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getDeviceList(): Flow<BluetoothGameItem> = channelFlow {
        val compatCallback: ScanCallbackCompat = object : ScanCallbackCompat() {
            override fun onScanResult(
                callbackType: Int,
                result: no.nordicsemi.android.support.v18.scanner.ScanResult
            ) {
                println("get scan callback")
                result.scanRecord?.let { record ->
                    if (record.serviceUuids!!.none { it.uuid == service }) return

                    with(result.device) { println("$name:$address") }
                    println("flag: ${record.advertiseFlags}")
                    println("services: ${record.serviceUuids?.joinToString(", ")}")
                    val settings = record.serviceData.let { data ->
                        if (data!!.isEmpty()) {
                            println("serviceData: empty")
                            return@let null
                        }
                        data.forEach { (k, v) ->
                            println("$k: $v")
                        }
                        return@let GameParams.parseFrom(data[ParcelUuid(BluetoothLEInteractor.settings)])
                            .toGameSettings()
                    }
                    trySend(BluetoothGameItem(result.device, settings))
                }
            }

            override fun onBatchScanResults(results: MutableList<no.nordicsemi.android.support.v18.scanner.ScanResult>) {
                println("get batch of scan: $results")
            }

            override fun onScanFailed(errorCode: Int) {
                println(
                    when (errorCode) {
                        SCAN_FAILED_ALREADY_STARTED -> "SCAN_FAILED_ALREADY_STARTED "
                        SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"
                        SCAN_FAILED_FEATURE_UNSUPPORTED -> "SCAN_FAILED_FEATURE_UNSUPPORTED"
                        SCAN_FAILED_INTERNAL_ERROR -> "SCAN_FAILED_INTERNAL_ERROR"
                        SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> "SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES"
                        SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> "SCAN_FAILED_SCANNING_TOO_FREQUENTLY"
                        else -> throw IllegalStateException("WTF: $errorCode")
                    }
                )
                throw IllegalStateException("failed")
            }
        }

        val scanner = BluetoothLeScannerCompat.getScanner()
        val filters: MutableList<ScanFilterCompat> = ArrayList()
        val settings: ScanSettingsCompat = ScanSettingsCompat.Builder()
            .setLegacy(true)
            .setScanMode(ScanSettingsCompat.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettingsCompat.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettingsCompat.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettingsCompat.MATCH_NUM_MAX_ADVERTISEMENT)
            .build()
        filters.add(ScanFilterCompat.Builder().setServiceUuid(ParcelUuid(service)).build())
        scanner.startScan(filters, settings, compatCallback)
        awaitClose {
            scanner.stopScan(compatCallback)
            println("scan stopped")
        }
    }
}