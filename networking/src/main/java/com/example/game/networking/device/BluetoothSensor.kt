package com.example.game.networking.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

sealed class BluetoothCommand {
    data class Enable(val respChannel: SendChannel<Boolean>) : BluetoothCommand()
    data class MakeDiscoverable(val seconds: Int, val respChannel: SendChannel<Boolean>) : BluetoothCommand()
}

class BluetoothSensor(private val context: Context) {
    private val mBluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val haveBt = mBluetoothAdapter != null

    private val commandChan = Channel<BluetoothCommand>()

    fun bluetoothEnabled() = mBluetoothAdapter!!.isEnabled

    fun getPairedDevices(): Set<BluetoothDevice> = mBluetoothAdapter!!.bondedDevices

    suspend fun requestEnable(): Boolean {
        val respChannel = Channel<Boolean>()
        commandChan.send(BluetoothCommand.Enable(respChannel))
        return respChannel.receive()
    }

    suspend fun requestMakeDiscoverable(): Boolean {
        val respChannel = Channel<Boolean>()
        commandChan.send(BluetoothCommand.MakeDiscoverable(120, respChannel))
        return respChannel.receive()
    }

    fun getDevices(): ReceiveChannel<BluetoothDevice> {
        val respChannel = Channel<BluetoothDevice>(Channel.UNLIMITED)
        val deviceReceiver = DeviceReceiver(respChannel)

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(deviceReceiver, filter)

        GlobalScope.launch {
            for (device in respChannel) {
                respChannel.send(device)
            }
        }
        return respChannel
    }

    fun commandListener(): ReceiveChannel<BluetoothCommand> = commandChan

}