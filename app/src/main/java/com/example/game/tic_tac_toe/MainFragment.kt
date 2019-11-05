package com.example.game.tic_tac_toe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.game.controllers.GameType
import com.example.game.tic_tac_toe.databinding.GameChoiceBinding
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.example.game.tic_tac_toe.viewmodels.SensorsViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MainFragment : Fragment() {
    private val GSViewModel: GameSetupViewModel by sharedViewModel()
    private val sensors: SensorsViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = GameChoiceBinding.inflate(layoutInflater, container, false)
        binding.typeChooser = SetupGameType
        binding.setup = GSViewModel
        binding.sensors = sensors
        return binding.root
    }
}

object SetupGameType {
    fun chooseType(view: View, gameType: GameType, setup: GameSetupViewModel, sensors: SensorsViewModel) {
        setup.setupGameType(gameType)
        setup.setupPlayers()
        val action = when (gameType) {
            GameType.Local -> R.id.action_myFragment_to_gameSetup
            GameType.Bluetooth -> R.id.action_myFragment_to_networkGame
            GameType.Network -> {
                if (!sensors.networkEnabled()) {
                    val snack = Snackbar.make(view, "Включите Интернет", Snackbar.LENGTH_LONG)
                    snack.show()
                    return
                }
                R.id.action_myFragment_to_networkGame
            }
        }
        view.findNavController().navigate(action)
    }
}