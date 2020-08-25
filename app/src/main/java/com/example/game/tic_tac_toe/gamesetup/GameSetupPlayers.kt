package com.example.game.tic_tac_toe.gamesetup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.game.controllers.Created
import com.example.game.controllers.CreationFailure
import com.example.game.controllers.GameID
import com.example.game.controllers.GameInitStatus
import com.example.game.controllers.models.GameType
import com.example.game.tic_tac_toe.databinding.RootConstraintLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.*
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameCreator
import com.example.game.tic_tac_toe.ui_components.*
import com.google.android.material.snackbar.Snackbar
import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime


class GameSetupPlayers : Fragment(), Step {
    private val config by lazy<GameConfig> { lookup() }
    private val initializer by lazy<GameCreator> { lookup() }

    private val notifications by lazy { backstack.notifications }

    companion object {
        const val TAG = "GameSetupPlayers"
    }

    @ExperimentalTime
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RootConstraintLayoutBinding.inflate(inflater, container, false)
        val gameSetup = GameSetupPlayersComponent(binding.root, GameSetupPlayersState(
                config.gameType, config.player1, config.player2
        ))
        viewLifecycleOwner.lifecycleScope.launch {
            gameSetup.getUserInteractionEvents().collect { settings ->
                when (settings) {
                    is PlayerX -> config.player1 = settings.player
                    is PlayerO -> config.player2 = settings.player
                    is Reshuffle -> {
                        config.reshuffle()
                        gameSetup.setPlayerX(config.player1)
                        gameSetup.setPlayerO(config.player2)
                    }
                    is CreateGame -> createGame(config.gameType)
                }
            }
        }

        return binding.getRoot()
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    private fun createGame(gameType: GameType) {
        if (!config.gameConfigured()) {
            Snackbar.make(requireView(), "Настройте игру", Snackbar.LENGTH_SHORT).show()
            return
        }

        when (gameType) {
            GameType.Local -> initializer.createLocalGame(config)
            GameType.Bluetooth -> startBluetoothGame()
            GameType.Network -> startNetworkGame()
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    private fun startBluetoothGame() {
        if (!backstack.bluetooth.bluetoothEnabled()) {
            Snackbar.make(requireView(), "Телефон нельзя обнаружить", Snackbar.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            initializer.createBluetoothGame(config).collect { state ->
                when (state) {
                    is GameInitStatus.Awaiting -> Log.d("LOL BT", "типа анимация загрузки")
                    is GameInitStatus.Connected -> {
                    }
                    is GameInitStatus.Failure -> Log.e("LOL BT", "got error")
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun startNetworkGame() {
        if (!backstack.network.haveInternet()) {
            Snackbar.make(requireView(), "Включите Интернет", Snackbar.LENGTH_LONG).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            initializer.createNetworkGame(config).collect { state ->
                when (state) {
                    is GameID -> {
                    }
                    is CreationFailure -> {
                    }
                    is Created -> {
                    }
                }
            }
        }
    }

    override fun onSelected() {
    }

    override fun verifyStep(): VerificationError {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(error: VerificationError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}