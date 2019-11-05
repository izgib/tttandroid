package com.example.game.networking.device

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

class DeviceReceiver(private val deviceChannel: SendChannel<BluetoothDevice>) : BroadcastReceiver() {

    companion object {
        const val DR_TAG = "DeviceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(DR_TAG, "action ${intent.action}")
        if (intent.action == BluetoothDevice.ACTION_FOUND) {
            runBlocking {
                deviceChannel.send(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE))
            }
        }
    }
}