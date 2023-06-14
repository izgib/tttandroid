package com.example.transport

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual typealias BluetoothDevice = android.bluetooth.BluetoothDevice
actual typealias BluetoothSocket = android.bluetooth.BluetoothSocket
//actual typealias BluetoothServerSocket = android.bluetooth.BluetoothServerSocket
//actual typealias BluetoothAdapter = android.bluetooth.BluetoothAdapter

actual fun BluetoothSocket.toConnectionWrapper() = object : ConnectionWrapper {
    override val inputStream = this@toConnectionWrapper.inputStream
    override val outputStream = this@toConnectionWrapper.outputStream
    override fun close() = this@toConnectionWrapper.close()
}

suspend fun BluetoothDevice.pairDevice(context: Context) =
    suspendCancellableCoroutine<Boolean> { continuation ->
        val pairingListener = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val pairDev = intent.getParcelableExtra<android.bluetooth.BluetoothDevice>(
                        BluetoothDevice.EXTRA_DEVICE
                    )!!
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val prevBondState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                    when {
                        bondState == BluetoothDevice.BOND_BONDED && pairDev == this@pairDevice -> {
                            onPairing(true)
                        }
                        bondState == BluetoothDevice.BOND_NONE && prevBondState == BluetoothDevice.BOND_BONDING -> {
                            onPairing(false)
                        }
                    }
                }
            }

            fun onPairing(paired: Boolean) {
                context.unregisterReceiver(this)
                continuation.resume(paired)
            }
        }
        val pairFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(pairingListener, pairFilter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            check(this.createBond())
        } //TODO сделать для старых версий https://www.programcreek.com/java-api-examples/?class=android.bluetooth.BluetoothDevice&method=createBond
        continuation.invokeOnCancellation { context.unregisterReceiver(pairingListener) }
    }