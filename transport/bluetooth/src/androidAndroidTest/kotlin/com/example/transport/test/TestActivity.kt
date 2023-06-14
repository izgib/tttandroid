package com.example.transport.test

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.transport.BluetoothDevice
import com.example.transport.BluetoothInteractor
import com.example.transport.device.BluetoothCommand
import com.example.transport.device.BluetoothSensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TestActivity : ComponentActivity() {
    val btSensor: BluetoothSensor by lazy { BluetoothSensor(applicationContext) }

    private lateinit var adapter: ArrayAdapter<BluetoothDevice>
    lateinit var info: TextView

    companion object {
        const val REQUEST_ENABLE_BT: Int = 1
        const val TAG = "MainActivity"
        private const val REQUEST_MAKE_DISCOVERABLE_BT: Int = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(
            LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL

                adapter = object : ArrayAdapter<BluetoothDevice>(
                    context, android.R.layout.two_line_list_item, ArrayList<BluetoothDevice>()
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val inflater = LayoutInflater.from(context)
                        val view: View = (convertView ?: inflater.inflate(
                            android.R.layout.two_line_list_item,
                            parent,
                            false
                        ))

                        val line1 = view.findViewById<TextView>(android.R.id.text1).apply {
                            setTextColor(Color.WHITE)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        }
                        val line2 = view.findViewById<TextView>(android.R.id.text2).apply {
                            setTextColor(Color.WHITE)
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        }
                        getItem(position)!!.apply {
                            line1.text = name
                            line2.text = address
                        }

                        return view
                    }
                }

                info = TextView(this@TestActivity).apply {
                    addView(this)
                    setTextColor(Color.WHITE)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                }
                addView(ListView(this@TestActivity).apply {
                    adapter = this@TestActivity.adapter
                })
            }
        )

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
//            val request = btSensor.commandFlow.value!! as BluetoothCommand.MakeDiscoverable

            setResult(resultCode)
            finish()
            return
            /*val discoverable = when (resultCode) {
                RESULT_CANCELED -> false
                else -> resultCode == request.timeSec
            }
            request.respChannel.trySendBlocking(discoverable)
            request.respChannel.close()
            btSensor.commandExecuted()*/
        }
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    // duration is seconds
    private fun makeDiscoverable(timeSec: Int) {
        println("making discoverable: $timeSec")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, timeSec)
        }
        startActivityForResult(discoverableIntent, REQUEST_MAKE_DISCOVERABLE_BT)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "activity started")
        lifecycleScope.launch {
            btSensor.commandFlow.filterNotNull().collect { command ->
                when (command) {
                    is BluetoothCommand.Enable -> enableBluetooth()
                    is BluetoothCommand.MakeDiscoverable -> makeDiscoverable(command.timeSec)
                }
            }
        }

        intent.action?.let {
            println("action: $it")
            if (it == BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE) {
                val seconds = intent.extras?.getInt(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION)
                if (seconds != 0) makeDiscoverable(seconds!!)
                return
            }
        }
        val seconds = intent.extras?.getInt(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION)
        if (seconds != 0) {
            println("discoverable: $seconds")
            makeDiscoverable(seconds!!)
            /*lifecycleScope.launch {
                val result =
                    if (btSensor.requestMakeDiscoverable()) Activity.RESULT_OK else Activity.RESULT_CANCELED
                withContext(Dispatchers.Main.immediate) {
                    setResult(result)
                    finish()
                }
            }*/
            return
        }


        intent.getParcelableExtra<BluetoothDevice>("device")?.let { device ->
            info.text = "testing"
            addDevice(device)
            return
        }

        val paired = btSensor.getPairedDevices()

        paired.forEach { device ->
            addDevice(device)
        }
//        val name = intent.getStringExtra("name") ?: throw IllegalStateException()
        val name = intent.getStringExtra("name") ?: return
        val address = intent.getStringExtra("address") ?: throw IllegalStateException()
        info.text = "paired: ${paired.count()}"

        info.text = "finding: ${paired.count()}+"
        lifecycleScope.launch {
            val device = run {
                for (t in 1..3) {
                    println("scanning devices: $t try")
                    val device = btSensor.findDevices().firstOrNull() { device ->
                        addDevice(device)
                        device.address == address && device.name == name
                    } ?: continue
                    return@run device
                }
                return@run null
            } ?: run {
                withContext(Dispatchers.Main.immediate) {
                    setResult(RESULT_CANCELED)
                    finish()
                }
                return@launch
            }
            device.uuids ?: run {
                val uuids = btSensor.rescanServices(device)
                    ?: throw IllegalStateException("can not fetch services")
                if (uuids.none { uuid -> uuid.uuid == BluetoothInteractor.MY_UUID }) throw IllegalStateException(
                    "does not have game service"
                )
                println("setting activity result")
                withContext(Dispatchers.Main.immediate) {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("device", device)
                    })
                    finish()
                }
                return@launch
            }
            if (device.uuids.none { uuid -> uuid.uuid == BluetoothInteractor.MY_UUID }) {
                println("does not have game service")
            }
            withContext(Dispatchers.Main.immediate) {
                setResult(RESULT_OK, Intent().apply {
                    putExtra("device", device)
                })
                finish()
            }
        }
    }

    fun addDevice(device: BluetoothDevice) {
        adapter.add(device)
        adapter.notifyDataSetChanged()
    }
}