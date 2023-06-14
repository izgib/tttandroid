package com.example.transport.device

import android.bluetooth.*
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import com.example.controllers.ClientResponse
import com.example.controllers.GameSettings
import com.example.game.Mark
import com.example.transport.*
import com.example.transport.extensions.toClientResponse
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class Application(private val context: Context) : com.example.transport.Application {
    private val manager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val settingsChar = SettingsCharacteristic(GameSettings(3, 3, 3, Mark.Cross))

    override var settings: GameSettings
        get() = settingsChar.params
        set(value) {
            settingsChar.params = value
        }

    private val clientMessages = ClientMessageCharacteristic()
    private val serverMessages = ServerMessageCharacteristic()
    private val service = GameService(settingsChar, clientMessages, serverMessages)
    private var server: BluetoothGattServer? = null

    private val serverCallback = object : BluetoothGattServerCallback() {
        val clientRsp = Channel<ClientResponse>()

        var onIndicate: ((device: BluetoothDevice, notify: Boolean) -> Unit)? = null

        override fun onConnectionStateChange(
            device: BluetoothDevice, status: Int, newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> println("$device: connected")
                BluetoothProfile.STATE_DISCONNECTED -> println("$device: disconnected")
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService) {
            require(service == service)
            require(status == BluetoothGatt.GATT_SUCCESS) { "unexpected status: $status" }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            require(characteristic == settingsChar) { "unexpected characteristic: $characteristic" }
            server!!.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS, offset, settingsChar.value
            )
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
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
            println("offset: $offset")
            println("served: $server")
            clientRsp.trySend(ClientMessage.parseFrom(value).toClientResponse())

            server!!.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            require(descriptor == serverMessages.notifications ) { "unexpected descriptor: $descriptor" }
            require(descriptor.characteristic == serverMessages) { "unexpected characteristic: ${descriptor.characteristic}" }
            require(!preparedWrite)
            println("responseNeeded: $responseNeeded")
            when {
                value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE) -> {
                    onIndicate?.let { it(device, true) }
                    server!!.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        null
                    )
                }
                value.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE) -> {
                    onIndicate?.let { it(device, false) }
                    server!!.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        null
                    )
                }
                else -> {
                    onIndicate?.let { it(device, false) }
                    server!!.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        offset,
                        null
                    )
                    onIndicate = null
                    val strinByte = value.joinToString(separator = "") { String.format("%02x", it) }
                    throw IllegalStateException("unexpected value: $strinByte")
                }
            }
        }

        override fun onNotificationSent(
            device: BluetoothDevice, status: Int
        ) {
            require(status == BluetoothGatt.GATT_SUCCESS)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun legacyLolipopAdvertising(data: AdvertiseData) {
        val advertiser = manager.adapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setConnectable(true)
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {

            }

            override fun onStartFailure(errorCode: Int) {
                throw IllegalStateException("can not start advertising")
            }
        }
        advertiser.startAdvertising(settings, data, callback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val lolipopAdvertising = object {
        private lateinit var callback: AdvertiseCallback

        fun startAdvertising(data: AdvertiseData) {
            val settings = AdvertiseSettings.Builder()
                .setConnectable(true)
                .build()

            callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {

                }

                override fun onStartFailure(errorCode: Int) {
                    throw IllegalStateException("can not start advertising")
                }
            }
            manager.adapter.bluetoothLeAdvertiser.startAdvertising(settings, data, callback)
        }

        fun stopAdvertising() {
            manager.adapter.bluetoothLeAdvertiser.stopAdvertising(callback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val oreoAdvertising = object {
        private lateinit var callback: AdvertisingSetCallback

        fun startAdvertising(data: AdvertiseData) {
            val advParams = AdvertisingSetParameters.Builder()
                .setConnectable(true)
                .setScannable(true)
                .setLegacyMode(true)
                .build()

            val responseData = AdvertiseData.Builder()
                .addServiceData(ParcelUuid(BluetoothLEInteractor.settings), settingsChar.value)
                .build()

            callback = object : AdvertisingSetCallback() {
                override fun onAdvertisingSetStarted(
                    advertisingSet: AdvertisingSet, txPower: Int, status: Int
                ) {
                    println("advertising started: $advertisingSet - status: $status")
                    advertisingSet.setScanResponseData(responseData)
                }

                override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet) {
                    println("advertising stopped")
                }

                override fun onAdvertisingEnabled(
                    advertisingSet: AdvertisingSet,
                    enable: Boolean,
                    status: Int
                ) {
                    println("onAdvertisingEnabled - status: $status - enable: $enable")
                }

                override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {
                    println("onAdvertisingDataSet - status: $status")
                }

                override fun onScanResponseDataSet(advertisingSet: AdvertisingSet, status: Int) {
                    println("scan response: $advertisingSet - status: $status")
                }
            }

            //There is scanResponse is null because of
            manager.adapter.bluetoothLeAdvertiser.startAdvertisingSet(
                advParams, data, null, null, null, callback
            )
        }

        fun stopAdvertising() {
            manager.adapter.bluetoothLeAdvertiser.stopAdvertisingSet(callback)
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startAdvertising() {
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(BluetoothLEInteractor.service))
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            oreoAdvertising.startAdvertising(data)
            return
        }
        lolipopAdvertising.startAdvertising(data)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopAdvertising() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            oreoAdvertising.stopAdvertising()
            return
        }
        lolipopAdvertising.stopAdvertising()
    }

    /*@RequiresApi(Build.VERSION_CODES.O)
    private fun legacyOreoAdvertising(data: AdvertiseData) {
        val advertiser = manager.adapter.bluetoothLeAdvertiser

        val advParams = AdvertisingSetParameters.Builder()
            .setConnectable(true)
            .setScannable(true)
            .setLegacyMode(true)
            .build()

        val responseData = AdvertiseData.Builder()
            .addServiceData(ParcelUuid(BluetoothLEInteractor.settings), settingsChar.value)
            .build()

        val callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet, txPower: Int, status: Int
            ) {
                println("advertising started: $advertisingSet - status: $status")
                advertisingSet.setScanResponseData(responseData)
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet) {
                println("advertising stopped")
            }

            override fun onAdvertisingEnabled(
                advertisingSet: AdvertisingSet,
                enable: Boolean,
                status: Int
            ) {
                println("onAdvertisingEnabled - status: $status - enable: $enable")
            }

            override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {
                println("onAdvertisingDataSet - status: $status")
            }

            override fun onScanResponseDataSet(advertisingSet: AdvertisingSet, status: Int) {
                println("scan response: $advertisingSet - status: $status")
            }
        }

        *//*
        There is scanResponse is null because of
         *//*
        advertiser.startAdvertisingSet(
            advParams, data, null, null, null, callback
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun serviceAdvertising() {
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(BluetoothLEInteractor.service))
            .build()
        val responseData = AdvertiseData.Builder()
            .addServiceData(ParcelUuid(BluetoothLEInteractor.settings), ByteArray(0x01))
            .build()

        val callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet,
                txPower: Int,
                status: Int
            ) {
                println("advertising started: $advertisingSet - status: $status")
                advertisingSet.setScanResponseData(responseData)
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                println("advertising stopped")
            }

            override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {
                println("onAdvertisingDataSet - status: $status")
            }

            override fun onScanResponseDataSet(advertisingSet: AdvertisingSet, status: Int) {
                println("scan response: $advertisingSet - status: $status")
            }
        }

        advertiser.startAdvertisingSet(advParams, data, null, null, null, callback)
        kotlinx.coroutines.delay(60000)
        advertiser.stopAdvertisingSet(callback)
    }*/

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun announceGame(): Flow<Announcement> = callbackFlow {
        try {
            serverCallback.onIndicate = { device: BluetoothDevice, notify: Boolean ->
                println("notify called: $notify")
                if (notify) {
                    trySend(
                        ClientJoined(
                            BluetoothLEServerWrapper(
                                device,
                                server!!,
                                serverMessages,
                                serverCallback.clientRsp
                            )
                        )
                    )
                    close()
                }
            }
            startAdvertising()
            send(Started)
        } catch (e: Throwable) {
            send(Failed)
            close()
        }

        awaitClose {
            stopAdvertising()
            println("stop advertising")
        }
    }

    override fun registerApplication() {
        server = manager.openGattServer(context, serverCallback).apply {
            addService(service)
        }
    }

    override fun unregisterApplication() {
        println("unregistering application")
        server!!.close()
        server = null
    }
}