package com.example.game.networking.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


sealed class BluetoothCommand {
    data class Enable(val respChannel: SendChannel<Boolean>) : BluetoothCommand()
    data class MakeDiscoverable @ExperimentalTime constructor(val discoveryTime: Duration, val respChannel: SendChannel<Boolean>) : BluetoothCommand()
}

class BluetoothSensor(private val context: Context) {
    private val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val haveBt = mBluetoothAdapter != null
    private val _commandFlow = MutableStateFlow<BluetoothCommand?>(null)
    val commandFlow: StateFlow<BluetoothCommand?>
        get() = _commandFlow

    fun commandExecuted() {
        _commandFlow.value = null
    }

    fun bluetoothEnabled() = mBluetoothAdapter!!.isEnabled

    fun getPairedDevices(): Set<BluetoothDevice> = mBluetoothAdapter!!.bondedDevices

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun requestEnable(): Boolean {
        val respChannel = Channel<Boolean>()
        _commandFlow.value = BluetoothCommand.Enable(respChannel)
        return respChannel.receive()
    }

    @ExperimentalTime
    suspend fun requestMakeDiscoverable(): Boolean {
        val respChannel = Channel<Boolean>()
        _commandFlow.value = BluetoothCommand.MakeDiscoverable(120.seconds, respChannel)
        return respChannel.receive()
    }

    fun findDevices(): Flow<BluetoothDevice> {
        val respChannel = Channel<BluetoothDevice>(Channel.UNLIMITED)
        val deviceReceiver = DeviceReceiver(respChannel)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(deviceReceiver, filter)

        return respChannel.receiveAsFlow()
    }
}