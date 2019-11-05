package com.example.game.tic_tac_toe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.game.controllers.GameType
import com.example.game.tic_tac_toe.databinding.NetworkGameBinding
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.example.game.tic_tac_toe.viewmodels.SensorsViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


const val TAG = "InetGame"

class NetworkGame : Fragment() {
    private val sensors: SensorsViewModel by sharedViewModel()
    private val GSViewModel: GameSetupViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = NetworkGameBinding.inflate(inflater, container, false)
        binding.setup = GSViewModel
        binding.sensors = sensors
        binding.setupInitializer = GameSetupInitializer
        binding.gameFinder = GameFindHandler
        binding.lifecycleOwner = this
        return binding.root
    }
}

object GameSetupInitializer {
    fun startInitialization(view: View, setup: GameSetupViewModel) {
        when (setup.getGameType()) {
            GameType.Bluetooth -> {
                setup.isCreator = true
            }
            GameType.Network -> {
            }
            else -> throw IllegalArgumentException("expected network type only")
        }
        view.findNavController().navigate(R.id.action_networkGame_to_gameSetup)
    }
}

object GameFindHandler {
    fun findGame(view: View, setup: GameSetupViewModel, sensors: SensorsViewModel) {
        when (setup.getGameType()) {
            GameType.Bluetooth -> findBluetoothGames(view, setup, sensors)
            GameType.Network -> findNetworkGame(view, sensors)
            else -> throw IllegalArgumentException("expect only network games")
        }
    }

    private fun findBluetoothGames(view: View, setup: GameSetupViewModel, sensors: SensorsViewModel) {
        setup.isCreator = false
        if (!sensors.bluetoothEnabled()) {
            Snackbar.make(view, "Включите Bluetooth", Snackbar.LENGTH_LONG).show()
        } else {
            view.findNavController().navigate(R.id.action_networkGame_to_deviceChooser)
        }
    }

    private fun findNetworkGame(view: View, sensors: SensorsViewModel) {
        view.findNavController().navigate(R.id.action_networkGame_to_gameList)
    }
}

