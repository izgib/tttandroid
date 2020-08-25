package com.example.game.tic_tac_toe.navigation.scopes

import android.bluetooth.BluetoothDevice
import com.example.game.controllers.BluetoothInteractor
import com.example.game.controllers.GameInitStatus
import com.example.game.controllers.models.GameModel
import com.example.game.controllers.models.NetworkGameModel
import com.example.game.networking.device.BluetoothSensor
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class BluetoothInitializer(private val sensor: BluetoothSensor, private val handle: BluetoothInteractor, private val backstack: Backstack) : ScopedServices.Registered {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var scopeBounded = true

    override fun onServiceUnregistered() {
        if (scopeBounded) scope.cancel()
    }

    override fun onServiceRegistered() = Unit

    fun joinDevice(device: BluetoothDevice): Flow<GameInitStatus> {
        return handle.joinGame(device).receiveAsFlow().onEach { state ->
            when (state) {
                is GameInitStatus.OppConnected -> {
                    val config = backstack.lookup<GameConfig>().apply {
                        fromSettings(state.gameSettings)
                    }
                    val model = with(config) {
                        NetworkGameModel(rows, cols, win, player1, player2, scope, state.client)
                    }
                    goToGame(model)
                }
            }
        }
    }

    private fun goToGame(model: GameModel) {
        scopeBounded = false
        backstack.goTo(GameScreen(model, scope))
    }
}