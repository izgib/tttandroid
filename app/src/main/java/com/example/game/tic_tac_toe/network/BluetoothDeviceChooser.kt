package com.example.game.tic_tac_toe.network

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.game.controllers.GameInitStatus
import com.example.game.tic_tac_toe.databinding.BtDialogBinding
import com.example.game.tic_tac_toe.navigation.base.BaseFragment
import com.example.game.tic_tac_toe.navigation.base.backstack
import com.example.game.tic_tac_toe.navigation.base.bluetooth
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.BluetoothInitializer
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.fromSettings
import com.example.game.tic_tac_toe.ui_components.ButtonProgressComponent
import com.example.game.tic_tac_toe.ui_components.ButtonProgressState
import com.example.game.tic_tac_toe.ui_components.LabeledListComponent
import com.example.game.tic_tac_toe.ui_components.LabeledListState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime
import kotlin.time.seconds


@ExperimentalTime
class DeviceChooser : BaseFragment() {
    private val config by lazy<GameConfig> { lookup() }
    private val btSensor by lazy { backstack.bluetooth }
    private val initializer by lazy<BluetoothInitializer> { lookup() }

    private lateinit var button: ButtonProgressComponent
    private lateinit var pairedList: LabeledListComponent
    private lateinit var foundList: LabeledListComponent

    private lateinit var devPaired: ArrayList<BluetoothDevice>
    private val devFound: ArrayList<BluetoothDevice> = ArrayList()

    companion object {
        const val DC_TAG = "DeviceChooser"
    }

    private fun connect2Device(device: BluetoothDevice) {
        viewLifecycleOwner.lifecycleScope.launch {
            initializer.joinDevice(device).collect { state ->
                when (state) {
                    is GameInitStatus.Awaiting -> Log.d("LOL HERE BT", "типа анимация загрузки")
                    is GameInitStatus.OppConnected -> {
                        config.fromSettings(state.gameSettings)
                        //Log.d("LOL HERE BT", "mark: ${s.mark}")
                    }
                    is GameInitStatus.Failure -> Log.d("LOL HERE BT", "got error")
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = BtDialogBinding.inflate(inflater, container, false)
        button = ButtonProgressComponent(button = binding.btFind, progress = binding.btFindProgress, state = ButtonProgressState(
                100, 12.seconds
        ))

        devPaired = ArrayList(btSensor.getPairedDevices())

        val pairedState = if (devPaired.isEmpty()) {
            LabeledListState("", false, devPaired)
        } else {
            LabeledListState("Сопряженные устройства", true, devPaired)
        }

        pairedList = LabeledListComponent(label = binding.btDevicesStatus, list = binding.btPairedList, state = pairedState)
        foundList = LabeledListComponent(label = binding.foundDevs, list = binding.btFoundList, state = LabeledListState(
                "Найденные устройства", false, devFound)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            button.getUserInteractionEvents().collect {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1001)
                }
                foundList.setLabelVisibility(true)
                btSensor.findDevices().collect { device ->
                    devFound.add(device)
                    foundList.notifyDataSetChanged()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            foundList.getUserInteractionEvents().collect { position ->
                connect2Device(devFound[position])
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            pairedList.getUserInteractionEvents().collect { position ->
                connect2Device(devPaired[position])
            }
        }
    }
}
