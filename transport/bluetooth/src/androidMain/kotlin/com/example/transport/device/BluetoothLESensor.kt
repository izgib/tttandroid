package com.example.transport.device

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import com.example.transport.BluetoothLEInteractor.Companion.service
import com.example.transport.BluetoothLEInteractor.Companion.settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.util.*


class BluetoothLESensor(private val context: Context) {
    private val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private var bluetoothManager: BluetoothManager? = null
    val haveBluetoothLE: Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        } else false

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun findDevices(): Flow<BluetoothDevice> {
        println("finding devices LE")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lollipopScan()
        } else {
            preLollipopScan()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun advertisingScan() = channelFlow<BluetoothDevice> {
        val advertiser = mBluetoothAdapter!!.bluetoothLeAdvertiser
        val advParams = AdvertisingSetParameters.Builder()
            .setLegacyMode(true)
            .setConnectable(true)
            .setScannable(true)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(service))
            .build()

        val callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet?,
                txPower: Int,
                status: Int
            ) {
                println("advertising started")
                advertisingSet
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                println("advertising stopped")
            }

            override fun onAdvertisingDataSet(advertisingSet: AdvertisingSet, status: Int) {
                println("onAdvertisingDataSet - status: $status")
            }

            override fun onScanResponseDataSet(advertisingSet: AdvertisingSet?, status: Int) {
                super.onScanResponseDataSet(advertisingSet, status)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopScan() = channelFlow<BluetoothDevice> {
        val scanner = mBluetoothAdapter!!.bluetoothLeScanner

        val leScanCallback: ScanCallback = object : ScanCallback() {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                result.scanRecord?.let { record ->
                    with(result.device) { println("$name:$address") }
                    println("flag: ${record.advertiseFlags}")
                    println("services: ${record.serviceUuids?.joinToString(", ")}")
                    record.serviceData.let { data ->
                        if (data.isEmpty()) {
                            println("serviceData: empty")
                            return@let
                        }
                        data.forEach { (k, v) ->
                            println("$k: $v")
                        }
                    }

                    record.serviceUuids?.let { uuids ->
                        if (uuids.any { it.uuid == service }) {
                            scanner.stopScan(this)
                            trySendBlocking(result.device)
                            close()
                        }
                    }
                }
                //trySendBlocking(result.device)
            }

            override fun onScanFailed(errorCode: Int) {
                println(
                    when (errorCode) {
                        SCAN_FAILED_ALREADY_STARTED -> "SCAN_FAILED_ALREADY_STARTED "
                        SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED"
                        SCAN_FAILED_FEATURE_UNSUPPORTED -> "SCAN_FAILED_FEATURE_UNSUPPORTED"
                        else -> throw IllegalStateException("WTF: $errorCode")
                    }
                )
                throw IllegalStateException("failed")
            }
        }
        println("scannCallbackObject: ${leScanCallback.hashCode()}")

        val scanFilter = ScanFilter.Builder()
            //.setServiceUuid(ParcelUuid(MY_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .apply {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@apply
                setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@apply
                //setLegacy(true)
                //setLegacy(true)
            }
            .build()
        scanner.startScan(listOf(scanFilter), settings, leScanCallback)
        awaitClose {
            scanner.stopScan(leScanCallback)
        }
    }/*.retry(10) { error ->
        println("error: $error")
        true.also {
            delay(100)
        }
    }.catch {
        println(it.toString())
    }*/


/*    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private val preLollipopScanCallback = object : BluetoothAdapter.LeScanCallback {
        override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
            deviceList.add(device)
        }
    }*/

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun preLollipopScan() = channelFlow<BluetoothDevice> {
        val scanCallback = object : BluetoothAdapter.LeScanCallback {
            override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
                trySendBlocking(device)
            }
        }

        mBluetoothAdapter!!.startLeScan(arrayOf(service), scanCallback)
        awaitClose {
            mBluetoothAdapter!!.stopLeScan(scanCallback)
        }
    }

/*connectGatt(context, false, object :
BluetoothGattCallback() {
override fun onConnectionStateChange(
    gatt: BluetoothGatt,
    status: Int,
    newState: Int
) {
    println("connectionStateChange: $status:$newState");

    if (newState == BluetoothProfile.STATE_CONNECTED) {
        if (gatt.discoverServices()) {
            println("discovering started")
        } else {
            println("failed to start discovery")
        }
    }
}

override fun onServicesDiscovered(
    gatt: BluetoothGatt,
    status: Int
) {
    println("services discovered: $status")
    gatt.services.forEach {
        println("${it.uuid} service")
    }
    gatt.getService(BluetoothLESensor.MY_UUID)?.let { service ->
*//*                                service.characteristics.forEachIndexed { index, bluetoothGattCharacteristic ->
                                    println("char$index: ${bluetoothGattCharacteristic.value}")
                                }*//*
                    service.characteristics.firstOrNull{ it.uuid == BluetoothLESensor.settingsUUID }?.let { char ->
                        val data = char.value?.map { String.format("%02x", it) }?.joinToString(separator = "")
                        println("${char.uuid}: $data")
                        gatt.readCharacteristic(char)
                    }

                } ?: println("null service")
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                println("onCharRead - char: $characteristic:${characteristic.uuid} - status:$status")
                println(characteristic.value?.map { String.format("%02x", it) }?.joinToString(separator = ""))
                //super.onCharacteristicRead(gatt, characteristic, status)
            }

        }, BluetoothDevice.TRANSPORT_AUTO).apply {
            //println("starting discovering: ${discoverServices()}")
            //val char = LolChar()
        }*/

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private val server = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: BluetoothDevice,
            status: Int,
            newState: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        println("connected")
                    }
                }
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> {
                    return
                }
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                    return
                }
                // GATT_ERROR
                133 -> {
                    return
                }
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                    return
                }
            }
            println("connected")
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService) {
            println("service add operation")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    println("succes")
                }
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> {
                    return
                }
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION -> {
                    return
                }
                // GATT_ERROR
                133 -> {
                    return
                }
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> {
                    return
                }
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
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
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
        }

        override fun onExecuteWrite(
            device: BluetoothDevice,
            requestId: Int,
            execute: Boolean
        ) {
            super.onExecuteWrite(device, requestId, execute)
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun serviceAdvertising() {
        val advertiser = mBluetoothAdapter!!.bluetoothLeAdvertiser
        val advParams = AdvertisingSetParameters.Builder()
            .setConnectable(true)
            .setScannable(true)
            .setLegacyMode(true)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(service))
            .build()
        val responseData = AdvertiseData.Builder()
            .addServiceData(ParcelUuid(settings), ByteArray(0x01))
            .build()

        val callback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet?,
                txPower: Int,
                status: Int
            ) {
                println("advertising started: $advertisingSet - status: $status")
                advertisingSet?.setScanResponseData(responseData)
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
        delay(60000)
        advertiser.stopAdvertisingSet(callback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun initService() {
        /*bluetoothManager!!.openGattServer(context, server).apply {
            addService(GameService())
        }
        serviceAdvertising()*/
    }

    companion object {
        const val SCAN_PERIOD: Long = 10000
    }

}

