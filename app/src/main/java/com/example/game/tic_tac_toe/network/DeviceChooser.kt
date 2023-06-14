package com.example.game.tic_tac_toe.network

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.companion.CompanionDeviceManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.controllers.GameSettings
import com.example.controllers.NetworkClient
import com.example.controllers.PlayerAction
import com.example.controllers.models.Response
import com.example.game.Continues
import com.example.game.Coord
import com.example.game.Mark
import com.example.game.not
import com.example.game.tic_tac_toe.databinding.BtDialogBinding
import com.example.game.tic_tac_toe.navigation.base.*
import com.example.game.tic_tac_toe.navigation.scopes.BluetoothInitializer
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.ui_components.*
import com.example.transport.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.ExperimentalTime


@ExperimentalTime
class DeviceChooser : BaseFragment() {
    private val config by lazy<GameConfig> { lookup() }
    private val btSensor by lazy { backstack.bluetooth }
    private val initializer by lazy<BluetoothInitializer> { lookup() }
    //private val leInteractor by lazy { BluetoothLEInteractorImpl(requireContext()) }

    private lateinit var deviceChan: Channel<BluetoothDevice?>
    private lateinit var deviceManager: CompanionDeviceManager
    private lateinit var deviceChooserLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var pairLauncher: ActivityResultLauncher<Intent>

    private lateinit var button: ButtonProgressComponent
    private lateinit var pairedList: LabeledDeviceSetComponent
    private lateinit var foundListComponent: DeviceSetComponent
    private lateinit var foundList: LabeledDeviceSetComponent


    private lateinit var devPaired: LinkedHashSet<BluetoothDevice>
    //private val leDevFound = LinkedHashSet<BluetoothDevice>()

    companion object {
        const val DC_TAG = "DeviceChooser"
    }

    private suspend fun isPaired(context: Context, device: BluetoothDevice) =
        suspendCancellableCoroutine<Boolean> { continuation ->
            val pairingListener = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                        val pairDev = intent.getParcelableExtra<BluetoothDevice>(
                            BluetoothDevice.EXTRA_DEVICE
                        )!!
                        val bondState =
                            intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                        val prevBondState =
                            intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                        when {
                            bondState == BluetoothDevice.BOND_BONDED && pairDev == device -> {
                                onPairing(true)
                            }

                            bondState == BluetoothDevice.BOND_NONE && prevBondState == BluetoothDevice.BOND_BONDING -> {
                                onPairing(false)
                            }

                        }
                    }
                }

                fun onPairing(paired: Boolean) {
                    context.unregisterReceiver(this)
                    continuation.resume(paired)
                }
            }
            val pairFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(pairingListener, pairFilter)
            continuation.invokeOnCancellation { context.unregisterReceiver(pairingListener) }
        }

    private fun connect2Device(device: BluetoothDevice) {
        viewLifecycleOwner.lifecycleScope.launch {
            initializer.joinDevice(device).collect { state ->
                when (state) {
                    is Loading -> Log.d("LOL HERE BT", "типа анимация загрузки")
                    is Joined -> return@collect
                    is NeedsPairing -> {
                        if (device.createBond()) {
                            if (isPaired(requireContext(), device)) {
                                println("paired")
                            }
                        }
                    }
                    is JoinFailure -> Toast.makeText(
                        requireContext(),
                        "got error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalStdlibApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BtDialogBinding.inflate(inflater, container, false)

        button = ButtonProgressComponent(
            button = binding.btFind,
            progress = binding.btFindProgress,
            state = DeterminateProgress(
                false, 100, 12L * 1000L, "Остановить поиск", "Поиск устройств"
            )
        )

        devPaired = btSensor.getPairedDevices()

        val pairedState =
            LabeledSetState("Сопряженные устройства", devPaired.isNotEmpty(), devPaired)

        pairedList = LabeledDeviceSetComponent(
            label = binding.btDevicesStatus,
            list = binding.btPairedList,
            state = pairedState
        )


        foundList = LabeledDeviceSetComponent(
            label = binding.foundDevs, list = binding.btFoundList, state = LabeledSetState(
                "Найденные устройства", false, initializer.deviceFound
            )
        )

        return binding.root
    }

    /*    @RequiresApi(Build.VERSION_CODES.O)
        suspend fun lul(): BluetoothDevice? {
            val deviceFilter = BluetoothDeviceFilter.Builder().run {
                addServiceUuid(ParcelUuid(BluetoothInteractor.MY_UUID), null)
                build()
            }

            val pairingRequest = AssociationRequest.Builder().run {
                addDeviceFilter(deviceFilter)
                setSingleDevice(false)
                build()
            }

            val deviceManager = requireContext().getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

            val deviceChan = Channel<BluetoothDevice?>()
            val deviceChooserLauncher =
                registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val pairDevice =
                            result.data?.getParcelableExtra<BluetoothDevice>(CompanionDeviceManager.EXTRA_DEVICE)
                    }
                }

            deviceManager.associate(pairingRequest, object : CompanionDeviceManager.Callback() {
                override fun onDeviceFound(chooserLauncher: IntentSender) {
                    deviceChooserLauncher.launch(IntentSenderRequest.Builder(chooserLauncher).build())
                }

                override fun onFailure(error: CharSequence) {
                    println("error: $error")
                }
            }, null)


            return select<BluetoothDevice?> {
                deviceChan.onReceiveCatching { result ->
                    result.getOrNull()
                }
            }
        }*/


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                result.forEach { (permission, isGranted) ->
                    println("$permission: $isGranted")
                }
            }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        } else*/ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            println("check version")
            val canNotAccessLocationOnBackground = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

            val canNotAccessLocation = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            println("canNotAccessLocationOnBackground: $canNotAccessLocationOnBackground, canNotAccessLocation: $canNotAccessLocation")

            if (canNotAccessLocationOnBackground && canNotAccessLocation) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    )
                )
            }

            val lm =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val isPassiveEnabled = lm.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)
            println("isGpsEnabled: $isGpsEnabled")
            println("isNetEnabled: $isNetEnabled")
            println("isPassiveEnabled: $isPassiveEnabled")
            if (!isGpsEnabled) {
                val locationPermission =
                    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        println("locationGranted: $result")
                    }
                locationPermission.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            button.getUserInteractionEvents().collect { searching ->
                if (searching) {
                    initializer.findDevices().collect {
                        foundList.notifyDataSetChanged()
                    }
                    return@collect
                }
                initializer.cancel()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            initializer.started.collect {
                button.enabled = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            foundList.getUserInteractionEvents().collect { device ->
                connect2Device(device)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            pairedList.getUserInteractionEvents().collect { device ->
                println("command to connect: $device")
                connect2Device(device)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deviceManager =
                requireContext().getSystemService(CompanionDeviceManager::class.java)
            deviceChan = Channel()

            deviceChooserLauncher =
                registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        deviceChan.trySendBlocking(
                            result.data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
                        )
                    }
                }

            pairLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        }
    }

    private suspend fun BluetoothDevice.createBond(pairingVariant: Int): Boolean {
        println("here lol")
        when (pairingVariant) {
            BluetoothDevice.PAIRING_VARIANT_PIN -> {}
            BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION -> {}
            else -> throw IllegalArgumentException()
        }

        val intent = Intent(BluetoothDevice.ACTION_PAIRING_REQUEST).apply {
            putExtra(BluetoothDevice.EXTRA_DEVICE, this)
            putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, pairingVariant)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        pairLauncher.launch(intent)
        return isPaired(requireContext(), this)
    }
}

private fun testGameItemFlow(): Flow<BluetoothGameItem> = channelFlow {
    val set = BluetoothAdapter.getDefaultAdapter().bondedDevices

    var rows = 3
    var mark = Mark.Cross
    set.forEach { device ->
        delay(200)
        send(BluetoothGameItem(device, GameSettings(rows, rows, rows, mark)))
        rows++
        mark = !mark
    }
}

private fun testBluetoothGame(): Flow<ConnectionStatus> = channelFlow {
    trySend(Connecting)
    val wrapper = object : NetworkClient {
        override suspend fun getResponse(): Response {
            return Response(Coord(1, 1), Continues)
        }

        override suspend fun sendMove(move: Coord) {
            println("sending Move: $move")
        }

        override suspend fun sendAction(action: PlayerAction) {
            TODO("Not yet implemented")
        }

    }
    trySend(ConnectedGame(GameSettings(3, 3, 3, Mark.Nought), wrapper))
}

