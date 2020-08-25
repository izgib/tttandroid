package com.example.game.tic_tac_toe

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.game.networking.device.BluetoothCommand
import com.example.game.networking.device.BluetoothSensor
import com.example.game.networking.device.NetworkSensor
import com.example.game.tic_tac_toe.databinding.ViewHierarchyRootBinding
import com.example.game.tic_tac_toe.navigation.base.FragmentStateChanger
import com.example.game.tic_tac_toe.navigation.base.ServiceProvider
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogService
import com.example.game.tic_tac_toe.navigation.screens.MainScreen
import com.example.game.tic_tac_toe.notifications.NotificationsManager
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.navigator.Navigator
import kotlinx.android.synthetic.main.view_hierarchy_root.*
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

class MainActivity : AppCompatActivity() {
    private lateinit var btSensor: BluetoothSensor
    private lateinit var fragmentStateChanger: FragmentStateChanger

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
        const val TAG = "MainActivity"
        private const val REQUEST_MAKE_DISCOVERABLE_BT: Int = 2
    }

    @ExperimentalTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ViewHierarchyRootBinding.inflate(layoutInflater).getRoot())

        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, root.id)

        Navigator.configure()
                .setStateChanger(fragmentStateChanger)
                .setScopedServices(ServiceProvider())
                .setGlobalServices { backstack ->
                    GlobalServices.builder()
                            .add(DialogService(backstack))
                            .add(BluetoothSensor(applicationContext).also { sensor ->
                                btSensor = sensor
                            })
                            .add(NetworkSensor(applicationContext))
                            .add(NotificationsManager(application))
                            .build()
                }
                .install(this, root, History.of(MainScreen()))

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        supportActionBar?.hide()
        lifecycleScope.launchWhenStarted {
            btSensor.commandFlow.filterNotNull().collect { command ->
                when (command) {
                    is BluetoothCommand.Enable -> enableBluetooth()
                    is BluetoothCommand.MakeDiscoverable -> makeDiscoverable(command.discoveryTime)
                }
            }
        }
    }

    @ExperimentalTime
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            val request = btSensor.commandFlow.value!! as BluetoothCommand.Enable
            val enabled = when (resultCode) {
                RESULT_OK -> true
                RESULT_CANCELED -> false
                else -> throw IllegalArgumentException("WTF")
            }
            request.respChannel.sendBlocking(enabled)
            request.respChannel.close()
            btSensor.commandExecuted()
        }
        if (requestCode == REQUEST_MAKE_DISCOVERABLE_BT) {
            val request = btSensor.commandFlow.value!! as BluetoothCommand.MakeDiscoverable
            val discoverable = when (resultCode) {
                RESULT_CANCELED -> false
                else -> resultCode == request.discoveryTime.toInt(DurationUnit.SECONDS)
            }
            request.respChannel.sendBlocking(discoverable)
            request.respChannel.close()
            btSensor.commandExecuted()
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    // duration is seconds
    @ExperimentalTime
    private fun makeDiscoverable(discoveryTime: Duration) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, discoveryTime.toInt(DurationUnit.SECONDS))
        }
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE_BT)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "activity started")
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


