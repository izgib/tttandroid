package com.example.game.tic_tac_toe

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.game.controllers.GameInitStatus
import com.example.game.controllers.PlayerType
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.databinding.BtDeviceBinding
import com.example.game.tic_tac_toe.databinding.BtDialogBinding
import com.example.game.tic_tac_toe.viewmodels.GameInitializerModel
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.example.game.tic_tac_toe.viewmodels.SensorsViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class BtDevicesAdapter(
        context: Context,
        resource: Int,
        private val devices: ArrayList<BluetoothDevice>) :
        ArrayAdapter<BluetoothDevice>(context, resource, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(parent!!.context)
        val binding: BtDeviceBinding = DataBindingUtil.inflate(inflater, R.layout.bt_device, parent, false)
        binding.device = devices[position]
        return binding.root
    }
}


class DeviceChooser : DialogFragment(), AdapterView.OnItemClickListener {
    private val sensors: SensorsViewModel by sharedViewModel()
    private val GIModel: GameInitializerModel by sharedViewModel()
    private val GSViewModel: GameSetupViewModel by sharedViewModel()

    private lateinit var devPaired: ArrayList<BluetoothDevice>
    private lateinit var devFound: ArrayList<BluetoothDevice>

    companion object {
        const val DC_TAG = "DeviceChooser"
    }

    @ExperimentalCoroutinesApi
    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        Log.d(DC_TAG, "button clicked")

        val device: BluetoothDevice = when (parent.id) {
            R.id.bt_paired_list -> {
                Log.d(DC_TAG, "paired device $position")
                devPaired[position]
            }
            R.id.bt_found_list -> {
                Log.d(DC_TAG, "found device $position")
                devFound[position]
            }
            else -> throw IllegalArgumentException("invalid id")
        }

        GIModel.joinBt(device).observe(this, Observer { state ->
            when (state) {
                is GameInitStatus.Awaiting -> Log.d("LOL HERE BT", "типа анимация загрузки")
                is GameInitStatus.OppConnected -> {
                    val s = state.gameSettings
                    GSViewModel.apply {
                        isCreator = false
                        rows.apply { value = s.rows }
                        cols.apply { value = s.cols }
                        win.apply { value = s.win }
                        when (s.mark) {
                            Mark.Cross -> {
                                player1 = PlayerType.Bluetooth
                                player2 = PlayerType.Human
                            }
                            Mark.Nought -> {
                                player1 = PlayerType.Human
                                player2 = PlayerType.Bluetooth
                            }
                        }
                    }
                    Log.d("LOL HERE BT", "mark: ${s.mark}")
                    findNavController().navigate(R.id.action_deviceChooser_to_gameFragment)
                }
                is GameInitStatus.Failure -> Log.d("LOL HERE BT", "got error")
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = BtDialogBinding.inflate(inflater, container, false)
        devPaired = ArrayList(sensors.getPairedDevices())
        devFound = ArrayList()
        binding.apply {
            pairedAdapter = BtDevicesAdapter(context!!, R.layout.bt_device, devPaired)
            foundAdapter = BtDevicesAdapter(context!!, R.layout.bt_device, devFound).apply {
                setNotifyOnChange(true)
            }
            btPairedList.onItemClickListener = this@DeviceChooser
            btFoundList.onItemClickListener = this@DeviceChooser

            btFind.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1001)
                }
                if (!foundDevs.isShown) {
                    foundDevs.visibility = View.VISIBLE
                }
                btFindProgress.visibility = View.VISIBLE
                GlobalScope.launch(Dispatchers.Main) {
                    repeat(100) {
                        btFindProgress.incrementProgressBy(1)
                        delay(120)
                    }
                    btFindProgress.visibility = View.INVISIBLE
                    btFindProgress.progress = 0
                }

                sensors.findDevices().observe(this@DeviceChooser, Observer<BluetoothDevice> { device ->
                    devFound.add(device!!)
                })
            }
        }

        return binding.root
    }
}
