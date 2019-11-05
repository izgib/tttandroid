package com.example.game.tic_tac_toe.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.game.networking.device.BluetoothCommand
import com.example.game.networking.device.BluetoothSensor
import com.example.game.networking.device.NetworkSensor
import com.example.game.tic_tac_toe.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class SensorsViewModel(application: Application) : AndroidViewModel(application), KoinComponent {
    private val btSensor = BluetoothSensor(application)
    private val ntSensor = NetworkSensor(application)


    fun bluetoothListener(): LiveData<BluetoothCommand> {
        val listener = SingleLiveEvent<BluetoothCommand>()
        viewModelScope.launch {
            for (command in btSensor.commandListener()) {
                launch(Dispatchers.Main.immediate) {
                    listener.value = command
                }
            }
        }
        return listener
    }

    fun haveBluetooth() = btSensor.haveBt

    fun bluetoothEnabled() = btSensor.bluetoothEnabled()

    fun networkEnabled(): Boolean {
        return ntSensor.haveInternet()
    }

    fun getPairedDevices() = btSensor.getPairedDevices()

    fun enableBluetooth(): LiveData<Boolean> {
        val resp = SingleLiveEvent<Boolean>()
        viewModelScope.launch {
            val enabled = btSensor.requestEnable()
            resp.value = enabled
        }
        return resp
    }

    fun makeBluetoothDiscoverable(): LiveData<Boolean> {
        val resp = SingleLiveEvent<Boolean>()
        viewModelScope.launch {
            val discoverable = btSensor.requestMakeDiscoverable()
            resp.value = discoverable
        }
        return resp
    }

    fun findDevices(): LiveData<BluetoothDevice> {
        val devices = SingleLiveEvent<BluetoothDevice>()
        viewModelScope.launch {
            for (device in btSensor.getDevices()) {
                devices.value = device
            }
        }
        return devices
    }

    fun connectivityStatus(): LiveData<Boolean> {
        val resp = SingleLiveEvent<Boolean>()
        viewModelScope.launch {
            val connected = ntSensor.networkListener()
            for (status in connected) {
                resp.value = status
            }
        }
        return resp
    }
}