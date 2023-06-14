package com.example.game.tic_tac_toe.network

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.game.tic_tac_toe.databinding.BtLeGamesBinding
import com.example.game.tic_tac_toe.navigation.base.BaseFragment
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.BluetoothLEInitializer
import com.example.game.tic_tac_toe.ui_components.ButtonProgressComponent
import com.example.game.tic_tac_toe.ui_components.GameListComponent
import com.example.game.tic_tac_toe.ui_components.IndeterminateProgress
import com.example.transport.ConnectedGame
import com.example.transport.Connecting
import com.example.transport.ConnectingFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class GameChooser : BaseFragment() {
    //private val btLESensor by lazy { backstack.bluetoothLE }
    private val leInitializer by lazy { lookup<BluetoothLEInitializer>() }

    private lateinit var list: GameListComponent
    private lateinit var searchComponent: ButtonProgressComponent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BtLeGamesBinding.inflate(inflater, container, false)

        list = GameListComponent(binding.btLeFoundList, leInitializer.gameFound)

        searchComponent = ButtonProgressComponent(
            binding.leFind,
            binding.btLeFindProgress,
            IndeterminateProgress(false, 100, "Остановить поиск", "Найти игру")
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                result.forEach { (permission, isGranted) ->
                    println("$permission: $isGranted")
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val canNotRunScan = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (canNotRunScan) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

        with(viewLifecycleOwner.lifecycleScope) {
            launch {
                leInitializer.started.collect {
                    searchComponent.enabled = it
                }
            }
            launch {
                searchComponent.getUserInteractionEvents().collect { searching ->
                    if (searching)
                        this@with.launch {
                            leInitializer.findGames().collect {
                                list.notifyDataSetChanged()
                            }
                        }
                    else leInitializer.cancel()
                }
            }

            launch {
                list.getUserInteractionEvents().collect { game ->
                    if (leInitializer.started.value) leInitializer.cancel()
                    leInitializer.joinGame(game).collect { state ->
                        when (state) {
                            is ConnectedGame -> return@collect
                            Connecting -> println("connecting")
                            ConnectingFailure -> Toast.makeText(
                                requireContext(),
                                "can not connect",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

}