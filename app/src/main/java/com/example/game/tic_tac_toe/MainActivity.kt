package com.example.game.tic_tac_toe

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.game.networking.device.BluetoothCommand
import com.example.game.tic_tac_toe.databinding.MainscreenBinding
import com.example.game.tic_tac_toe.viewmodels.SensorsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val sensors: SensorsViewModel by viewModel()
    private lateinit var lastCommand: BluetoothCommand

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
        const val MA_TAG = "MainActivity"
        private const val REQUEST_MAKE_DISCOVERABLE_BT: Int = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainscreenBinding>(this, R.layout.mainscreen)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        sensors.bluetoothListener().observe(this, Observer {
            lastCommand = it
            Log.d(MA_TAG, "got command: $it")
            when (it) {
                is BluetoothCommand.Enable -> {
                    enableBluetooth()
                }
                is BluetoothCommand.MakeDiscoverable -> {
                    makeDiscoverable(it.seconds)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            val request = lastCommand as BluetoothCommand.Enable
            val enabled = when (resultCode) {
                RESULT_OK -> true
                RESULT_CANCELED -> false
                else -> throw IllegalArgumentException("WTF")
            }
            GlobalScope.launch {
                request.respChannel.send(enabled)
                request.respChannel.close()
            }
        }
        if (requestCode == REQUEST_MAKE_DISCOVERABLE_BT) {
            val request = lastCommand as BluetoothCommand.MakeDiscoverable
            val discoverable = when (resultCode) {
                RESULT_CANCELED -> false
                else -> resultCode == request.seconds
            }
            GlobalScope.launch {
                request.respChannel.send(discoverable)
                request.respChannel.close()
            }
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    // duration is seconds
    private fun makeDiscoverable(duration: Int) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        }
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE_BT)
    }
}


