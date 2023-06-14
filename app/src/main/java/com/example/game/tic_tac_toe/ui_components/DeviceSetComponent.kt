package com.example.game.tic_tac_toe.ui_components

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.game.tic_tac_toe.databinding.BtDeviceBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DeviceSetComponent(
    private val container: RecyclerView,
    private val set: LinkedHashSet<BluetoothDevice>
) : UIComponent<BluetoothDevice> {
    private val callbacks: Flow<BluetoothDevice>
    override fun getUserInteractionEvents(): Flow<BluetoothDevice> = callbacks

    private val adapter = object : ListComponentAdapter<BluetoothDevice>() {
        private val devices = set.toList()
        var onDeviceClick: ((BluetoothDevice) -> Unit)? = null

        override fun getComponentForList(viewType: Int): UIComponentForList<BluetoothDevice> {
            return DeviceItemComponent(
                BtDeviceBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    false
                )
            ).apply {
                binding.root.setOnClickListener { _ ->
                    onDeviceClick?.let { click ->
                        println("clicked")
                        click(set.asIterable().elementAt(adapterPosition))
                    }
                }
            }
        }

        fun getItem(position: Int): BluetoothDevice {
            println("position: $position")
            return devices[position]
        }

        override fun getItemCount(): Int {
            println("size: ${set.size}")
            return devices.size
        }

        override fun onBindViewHolder(holder: UIComponentForList<BluetoothDevice>, position: Int) {
            (holder as DeviceItemComponent).device = getItem(position)
        }
    }

    init {
        container.layoutManager = LinearLayoutManager(container.context)
        container.adapter = adapter

        callbacks = callbackFlow<BluetoothDevice> {
            adapter.onDeviceClick = { device ->
                trySend(device)
            }
            awaitClose()
        }
    }

    fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    class DeviceItemComponent(val binding: BtDeviceBinding) :
        UIComponentForList<BluetoothDevice>(binding.root) {
        var device: BluetoothDevice? = null
            set(value) {
                requireNotNull(value)
                binding.deviceName.text = value.name
                binding.deviceMacAddress.text = value.address
            }
    }
}


