package com.example.game.tic_tac_toe.ui_components

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.game.tic_tac_toe.databinding.BtDeviceBinding
import kotlinx.coroutines.flow.Flow

class LabeledListComponent(private val label: TextView, private val list: ListView, state: LabeledListState) : UIComponent<Int> {
    private val listComponent: DeviceListComponent
    override fun getUserInteractionEvents(): Flow<Int> = listComponent.getUserInteractionEvents()

    init {
        label.text = state.label
        label.visibility = if (state.labelVisibility) {
            View.VISIBLE
        } else {
            View.GONE
        }
        listComponent = DeviceListComponent(list, state.devices)
    }

    fun setLabelVisibility(visible: Boolean) {
        label.visibility = if (visible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun notifyDataSetChanged() {
        listComponent.notifyDataSetChanged()
    }
}


data class LabeledListState(val label: String, val labelVisibility: Boolean, val devices: ArrayList<BluetoothDevice>)

class BtDevicesAdapter(
        context: Context,
        resource: Int,
        private val devices: ArrayList<BluetoothDevice>) :
        ArrayAdapter<BluetoothDevice>(context, resource, devices) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            BtDeviceBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            BtDeviceBinding.bind(convertView)
        }
        return binding.apply {
            devices[position].apply {
                deviceName.text = name
                deviceMacAddress.text = address
            }
        }.root
    }
}