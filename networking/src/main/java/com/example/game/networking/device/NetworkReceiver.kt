package com.example.game.networking.device

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking

class NetworkReceiver(private val connectionChannel: SendChannel<Boolean>) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isAirplane = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
        //intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE)
        if (intent.action == BluetoothDevice.ACTION_FOUND) {
            runBlocking {
                connectionChannel.send(!isAirplane)
            }
        }
    }
}
