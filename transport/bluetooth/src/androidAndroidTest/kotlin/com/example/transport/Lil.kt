package com.example.transport

//import androidx.multidex.BuildConfig
import android.Manifest
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.transport.test.TestActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.*


@RunWith(AndroidJUnit4::class)
class Lil {
    @get:Rule
    val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

    /*companion object {
        private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()!!
        private val bluetoothDisabled = when (adapter.state) {
            BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> false
            else -> true
        }
        private var locationEnabled by Delegates.notNull<Boolean>()


        @get:ClassRule
        @JvmStatic
        val permissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)!!

        *//*@get:ClassRule
        @JvmStatic
        //val settingsRule = ActivityScenarioRule(PermissionActivity::class.java)
        val settingsRule = ActivityScenarioRule<Activity>(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))*//*
*//*        @get:ClassRule
        @JvmStatic
        val instantRule = InstantTaskExecutorRule()*//*

        private fun locationSettings(timeout: Long?) = ActivityScenario.launch(
            Location::class.java, Bundle().apply {
                if (timeout != null) {
                    putLong(Location.TIMEOUT_KEY, timeout)
                }
            }
        )

        @BeforeClass
        @JvmStatic
        fun setupTesting() {
            adapter.enable()
            val context = InstrumentationRegistry.getInstrumentation().targetContext

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationEnabled = LocationManagerCompat.isLocationEnabled(lm)
                if (locationEnabled) {
                    return
                }
                val scenario = locationSettings(5000)
                try {
                    scenario.moveToState(Lifecycle.State.CREATED)
                        .onActivity { activity ->
                            activity.enableLocationProvider()
                        }
                    when (scenario.result.resultCode) {
                        Activity.RESULT_OK -> Unit
                        Activity.RESULT_CANCELED -> IllegalStateException("location service was not enabled")
                        else -> IllegalArgumentException()
                    }
                } finally {
                    scenario.close()
                }
            }
        }

        @JvmStatic
        @AfterClass
        fun disableAll() {
            if (bluetoothDisabled) {
                adapter.disable()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!locationEnabled) {
                    val scenario = locationSettings(10000)
                    try {
                        scenario.moveToState(Lifecycle.State.CREATED)
                            .onActivity { activity ->
                                activity.enableLocationProvider()
                            }
                    } finally {
                        scenario.close()
                    }
                }
            }
        }
    }*/


/*    @LargeTest
    @Test
    fun h() {
        *//*        InstrumentationRegistry.getArguments().run {
            deviceName = getString("deviceName")!!
            macAddress = getString("macAddress")!!
        }*//*

        val deviceName: String = BuildConfig.deviceName
        val macAddress: String = BuildConfig.macAddress
        //adapter.enable()
        val scope = CoroutineScope(Job() + Dispatchers.Default)
        activityRule.scenario.onActivity { activity ->
            activity.lifecycleScope.launch {
                lol().onEach {
                    println(it)
                }.launchIn(scope)
                delay(7000)
                scope.cancel()
                println("try to close")
            }
//            activity.lifecycleScope.launchWhenStarted {
//                BluetoothAdapter.getDefaultAdapter()
//                activity.btSensor.findDevices().collect { device ->
//                    println("${device.name}: ${device.address}")
//                    activity.addDevice(device)
//                }
//            }
            *//*val wantedDevice = activity.btSensor.findDevices().first { device ->
                device.address == devAddress && device.name == devName
            }*//*

            *//*activity.lifecycleScope.launchWhenStarted {
                val wantedDevice = activity.btSensor.findDevices().first { device ->
                    device.address == macAddress && device.name == deviceName
            }*//*
        }
        Thread.sleep(10000L)
    }

    fun checkState() {
    }

    fun lol() = channelFlow<Int> {
        send(1)
        val uuid = UUID.fromString("1d0bc7f7-7745-4e3a-b292-9b0905b78efb")
        println("uuid: $uuid")


        var serverSocket: BluetoothServerSocket
        var i = 0
        while (true) {
            try {
                if (i.rem(100) == 0) println("$i: trying to create server socket")
                serverSocket = BluetoothAdapter.getDefaultAdapter()
                    .listenUsingRfcommWithServiceRecord("lolikus", uuid)
                send(2)
                break
            } catch (e: IOException) {
                if (e.message == "Try again") {
                    i++
                    continue
                }
                throw e
            }
        }

        invokeOnClose {
            println("closing flow")
            serverSocket.close()
            println("closed")
        }
        var j = 0
        while (true) {
            try {
                GlobalScope.async<BluetoothSocket>(Dispatchers.IO) {
                    if (j.rem(100) == 0) println("$j: trying to create socket")
                    return@async serverSocket.accept(30)
                }.await()
            } catch (e: IOException) {
                if (e.message == "Try again") {
                    j++
                    continue
                }
                println(e)
                throw e
            }
        }


        *//*val socket = async<BluetoothSocket>(Dispatchers.IO) {
            sendBlocking(2)
            var socket: BluetoothSocket
            var i = 0
            while (true) {
                //println("$i: trying to create socket")
                try {
                    socket = serverSocket.accept(30)
                    break
                } catch (e: IOException) {
                    if (e.message == "Try again") {
                        i++
                        continue
                    }
                    println("closed catch")
                    println(e)
                    throw e
                }
            }
            return@async socket
        }*//*

*//*        println("here")
        socket.await()
        send(3)*//*

*//*        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine<BluetoothSocket> { continuation ->
                continuation.resume(
                    run<BluetoothSocket> {
                        sendBlocking(2)
                        var socket: BluetoothSocket
                        while (true) {
                            try {
                                socket = serverSocket.accept(30)
                                break
                            } catch (e: IOException) {
                                if (e.message == "Try again") {
                                    continue
                                }
                                throw e
                            }
                        }
                        return@run socket
                    }
                ) { cause ->
                    println(cause)
                    println("closed")
                }
            }
        }*//*
*//*        runInterruptible(Dispatchers.IO) {
            trySendBlocking(2)
            run<BluetoothSocket> {
                var socket: BluetoothSocket
                var i = 0
                while (true) {
                    try {
                        println("$i: trying to create server socket")
                        socket = serverSocket.accept(2)
                        break
                    } catch (e: IOException) {
                        if (e.message == "Try again") {
                            i++
                            continue
                        }
                        throw e
                    }
                }
                return@run socket
            }
        }*//*
    }

    @LargeTest
    @Test
    fun setupWantedDevice() {

        var wantedDevice: BluetoothDevice
        ActivityScenario.launch(TestActivity::class.java)
            .moveToState(Lifecycle.State.STARTED)
            .onActivity { activity ->
                activity.lifecycleScope.launch {
                    val sensor = activity.btSensor
                    //sensor.getPairedDevices().firstOrNull { device ->
*//*                    val flow = BluetoothAdapter.getDefaultAdapter().run {
                        withTimeout(25000) {
                            InstrumentationRegistry.getInstrumentation().targetContext.testFilterDevices(
                                bondedDevices
                            )
                        }
                    }*//*

                    sensor.getUpdatedPairedDevices().firstOrNull { device ->
                    //flow.firstOrNull { device ->
                        activity.addDevice(device)
                        device.address == BuildConfig.macAddress && device.name == BuildConfig.deviceName
                    }?.let { device ->
                        wantedDevice = device
                        return@launch
                    }
                    val devFound = LinkedHashSet<android.bluetooth.BluetoothDevice>()
                    sensor.findDevices().firstOrNull { device ->
                        activity.addDevice(device)
                        devFound.add(device)
                        if (device.uuids?.any { uuid ->
                                uuid?.uuid == BluetoothInteractor.MY_UUID
                            } ?: return@firstOrNull false) {
                            return@firstOrNull true
                        }
                        return@firstOrNull false
                    }?.let { device ->
                        wantedDevice = device
                        return@launch
                    }
                    val wantedDevice = sensor.testFilterDevices(devFound).first { device ->
                        device.address == BuildConfig.macAddress && device.name == BuildConfig.deviceName
                    }
                }
            }
        Thread.sleep(45000)

    }

    fun Context.testFilterDevices(devices: Set<android.bluetooth.BluetoothDevice>): Flow<android.bluetooth.BluetoothDevice> {
*//*        val filter = IntentFilter(BluetoothDevice.ACTION_UUID)
        var changed = false
        var deviceCount = 0*//*
        println("input deviceCount: ${devices.count()}")

        return channelFlow<android.bluetooth.BluetoothDevice> {
            val filter = IntentFilter(android.bluetooth.BluetoothDevice.ACTION_UUID)
            var deviceCount = devices.count()

            val receiver = object : BroadcastReceiver() {
                val maxRejectCount = 1
                val rejectedDevices = mutableMapOf<android.bluetooth.BluetoothDevice, Int>()

                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == android.bluetooth.BluetoothDevice.ACTION_UUID) {
                        println("UUIDs received")
                        val deviceExtra: android.bluetooth.BluetoothDevice =
                            intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                        val devUuids =
                            intent.getParcelableArrayExtra(android.bluetooth.BluetoothDevice.EXTRA_UUID)

                        if (devUuids == null) {
                            val rejectCount = rejectedDevices.getOrPut(deviceExtra) { 1 }
                            if (rejectCount < maxRejectCount) {
                                deviceExtra.fetchUuidsWithSdp()
                                rejectedDevices[deviceExtra] = rejectCount + 1
                            } else {
                                println("can not get UUIDs ${deviceExtra.name}: ${deviceExtra.address}")
                                deviceCount--
                                completeCheck()
                            }
                            return
                        }
                        println("${deviceExtra.name}: ${deviceExtra.address}")
                        println(buildString {
                            devUuids.forEach { uuid ->
                                this.appendLine((uuid as ParcelUuid).uuid.toString())
                            }
                        })

                        val filtered = devUuids.any { parcelUuid ->
                            (parcelUuid as ParcelUuid).uuid == BluetoothInteractor.MY_UUID
                        }
                        if (filtered) {
                            println("wanted Device ${deviceExtra.name}: ${deviceExtra.address}")
                            trySendBlocking(deviceExtra)
                        }
                        deviceCount--

                        //trySend(BluetoothDeviceFiltered(deviceExtra, filtered))
                        completeCheck()
                    }
                }

                fun completeCheck() {
                    if (deviceCount <= 0) {
                        close()
                    }
                }
            }

            registerReceiver(receiver, filter)
            println("registered")
            if (!devices.all { it.fetchUuidsWithSdp() }) {
                println("can not fetch")
                close()
            }
            awaitClose {
                unregisterReceiver(receiver)
            }
        }
    }*/
}