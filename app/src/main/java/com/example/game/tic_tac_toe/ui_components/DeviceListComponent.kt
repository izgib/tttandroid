package com.example.game.tic_tac_toe.ui_components

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import com.example.game.tic_tac_toe.databinding.BtDeviceBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DeviceListComponent(private val container: ListView, arrayList: ArrayList<BluetoothDevice>) : UIComponent<Int> {
    private val adapter: BaseAdapter
    private val callbacks: Flow<Int>
    override fun getUserInteractionEvents(): Flow<Int> = callbacks

    init {
        adapter = object : AbstractListAdapter<BluetoothDevice>(arrayList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val binding = if (convertView == null) {
                    BtDeviceBinding.inflate(LayoutInflater.from(container.context), parent, false)
                } else {
                    BtDeviceBinding.bind(convertView)
                }
                return binding.apply {
                    deviceName.text = objects[position].name
                    deviceMacAddress.text = objects[position].address
                }.root
            }
        }
        container.adapter = adapter

        callbacks = callbackFlow<Int> {
            container.setOnItemClickListener { _, _, position, _ ->
                sendBlocking(position)
            }
            awaitClose()
        }
    }

    fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }
}
