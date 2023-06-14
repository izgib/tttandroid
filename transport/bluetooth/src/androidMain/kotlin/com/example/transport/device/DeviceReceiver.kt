package com.example.transport.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceReceiver(private val devices: ProducerScope<BluetoothDevice>) : BroadcastReceiver() {
    companion object {
        const val DR_TAG = "DeviceReceiver"
    }

    private var started = false
    var finished = false
        private set


    init {
        Log.d(DR_TAG, "created")
        devices.launch {
            delay(7500)
            if (!started) devices.close(IllegalStateException("device discovery does not start"))
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(DR_TAG, "action ${intent.action}")
        when (intent.action) {
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                started = true
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                this.
                finished = true
                devices.close()
            }
            BluetoothDevice.ACTION_FOUND -> {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)!!
                println("device found - ${device.name}:${device.address}")
                device.uuids?.forEach { uuid ->
                    println(uuid)
                } ?: println("uuids is null")
                devices.trySendBlocking(device)
            }
        }
    }
}