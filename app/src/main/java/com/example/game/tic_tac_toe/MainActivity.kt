package com.example.game.tic_tac_toe

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.game.tic_tac_toe.databinding.ViewHierarchyRootBinding
import com.example.game.tic_tac_toe.navigation.base.FragmentStateChanger
import com.example.game.tic_tac_toe.navigation.base.ServiceProvider
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.bluetooth
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogService
import com.example.game.tic_tac_toe.navigation.screens.MainScreen
import com.example.game.tic_tac_toe.notifications.NotificationsManager
import com.example.game.tic_tac_toe.sensors.NetworkSensor
import com.example.transport.BluetoothLEInteractor
import com.example.transport.BluetoothLEInteractorImpl
import com.example.transport.device.BluetoothCommand
import com.example.transport.device.BluetoothLESensor
import com.example.transport.device.BluetoothSensor
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.navigator.Navigator
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val btSensor by lazy { Navigator.getBackstack(this).bluetooth }
    private lateinit var fragmentStateChanger: FragmentStateChanger

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
        const val TAG = "MainActivity"
        private const val REQUEST_MAKE_DISCOVERABLE_BT: Int = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = ViewHierarchyRootBinding.inflate(layoutInflater).getRoot()
        setContentView(root)

        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, root.id)

        Navigator.configure()
            .setStateChanger(fragmentStateChanger)
            .setScopedServices(ServiceProvider())
            .setGlobalServices { backstack ->
                GlobalServices.builder()
                    .add(DialogService(backstack))
                    .add(BluetoothSensor(applicationContext))
                    .add(NetworkSensor(applicationContext))
                    .add(BluetoothLESensor(applicationContext))
                    .add(NotificationsManager(application))
                    .add<BluetoothLEInteractor>(BluetoothLEInteractorImpl(applicationContext))
                    .build()
            }
            .install(this, root, History.of(MainScreen()))

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        supportActionBar?.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            val request = btSensor.commandFlow.value!! as BluetoothCommand.Enable
            val enabled = when (resultCode) {
                RESULT_OK -> true
                RESULT_CANCELED -> false
                else -> throw IllegalArgumentException("WTF")
            }
            request.respChannel.trySendBlocking(enabled)
            request.respChannel.close()
            btSensor.commandExecuted()
        }
        if (requestCode == REQUEST_MAKE_DISCOVERABLE_BT) {
            val request = btSensor.commandFlow.value!! as BluetoothCommand.MakeDiscoverable
            val discoverable = when (resultCode) {
                RESULT_CANCELED -> false
                else -> resultCode == request.timeSec
            }
            request.respChannel.trySendBlocking(discoverable)
            request.respChannel.close()
            btSensor.commandExecuted()
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    // duration is seconds
    private fun makeDiscoverable(timeSec: Int) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeSec * 1000)
        }
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE_BT)
    }

    override fun onStart() {
        super.onStart()
        if (btSensor.haveBt) {
            lifecycleScope.launchWhenStarted {
                btSensor.commandFlow.filterNotNull().collect { command ->
                    when (command) {
                        is BluetoothCommand.Enable -> enableBluetooth()
                        is BluetoothCommand.MakeDiscoverable -> makeDiscoverable(command.timeSec)
                    }
                }
            }
        }

        Log.d(TAG, "activity started")
        if (Navigator.isNavigatorAvailable(this)) {
            println("navigator available")
        }
        /*        lifecycleScope.launch {
                    btSensor.commandFlow.filterNotNull().collect { command ->
                        when (command) {
                            is BluetoothCommand.Enable -> enableBluetooth()
                            is BluetoothCommand.MakeDiscoverable -> makeDiscoverable(command.timeSec)
                        }
                    }
                }*/
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "activity resumed")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "activity paused")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "activity stopped")
    }

    override fun onBackPressed() {
        if (!Navigator.onBackPressed(this)) {
            super.onBackPressed()
        }
    }
}


