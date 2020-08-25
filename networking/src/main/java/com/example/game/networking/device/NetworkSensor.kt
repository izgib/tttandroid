package com.example.game.networking.device

import android.annotation.TargetApi
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.sendBlocking

class NetworkSensor(private val context: Context) {
    private val conManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Suppress("DEPRECATION")
    fun networkListener(): ReceiveChannel<Boolean> {
        val chan = Channel<Boolean>(Channel.CONFLATED)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> conManager.registerDefaultNetworkCallback(getConnectivityManagerCallback(chan))
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> lollipopNetworkAvailableRequest(chan)
            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                    context.registerReceiver(NetworkReceiver(chan), filter)
                }
            }
        }
        return chan
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lollipopNetworkAvailableRequest(connectivityChan: SendChannel<Boolean>) {
        val builder = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        conManager.registerNetworkCallback(builder.build(), getConnectivityManagerCallback(connectivityChan))
    }

    private fun getConnectivityManagerCallback(connectivityChan: SendChannel<Boolean>): ConnectivityManager.NetworkCallback {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    connectivityChan.sendBlocking(true)
                }

                override fun onLost(network: Network?) {
                    connectivityChan.sendBlocking(false)
                }
            }
        } else {
            throw IllegalAccessError("Should not happened")
        }
    }

    fun haveInternet(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            conManager.activeNetworkInfo?.let {
                return (it.isConnected && (it.type == ConnectivityManager.TYPE_WIFI || it.type == ConnectivityManager.TYPE_MOBILE))
            }
        } else {
            conManager.activeNetwork?.let {
                val nc = conManager.getNetworkCapabilities(it)
                return nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        }
        return false
    }
}