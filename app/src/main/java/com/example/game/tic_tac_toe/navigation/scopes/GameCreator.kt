package com.example.game.tic_tac_toe.navigation.scopes

import com.example.game.controllers.*
import com.example.game.controllers.models.BluetoothServerGameModel
import com.example.game.controllers.models.GameModel
import com.example.game.controllers.models.LocalGameModel
import com.example.game.controllers.models.NetworkGameModel
import com.example.game.tic_tac_toe.navigation.base.notifications
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

class GameCreator(private val backstack: Backstack, private val ntHandler: NetworkInteractor? = null, private val btHandler: BluetoothInteractor? = null) : ScopedServices.Registered {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var scopeBounded = true

    override fun onServiceUnregistered() {
        if (scopeBounded) scope.cancel()
    }

    override fun onServiceRegistered() = Unit

    private var netFlow: Flow<GameCreationStatus>? = null

    private var btFlow: Flow<GameInitStatus>? = null

    fun createLocalGame(config: GameConfig) {
        val model = with(config) {
            LocalGameModel(rows, cols, win, player1, player2, scope)
        }
        goToGame(model)
    }

    fun createNetworkGame(config: GameConfig): Flow<GameCreationStatus> = with(config) {
        check(ntHandler != null) { "network Handler is not initialized" }
        if (netFlow == null) {
            val initializer = ntHandler.CreateGame(scope)
            val notManager = backstack.notifications
            notManager.setupNotification(initializer)
            netFlow = initializer.sendCreationRequest(config.toSettings()).onEach { state ->
                when (state) {
                    is GameID -> {
                        notManager.setupGameID(state)
                    }
                    is Created -> {
                        notManager.gameCreated()
                        val model = with(config) {
                            NetworkGameModel(rows, cols, win, player1, player2, scope, state.client)
                        }
                        goToGame(model)
                    }
                    is CreationFailure -> notManager.creationFailed()
                }
            }.onCompletion { netFlow = null }
        }
        return@with netFlow!!
    }

    fun createBluetoothGame(config: GameConfig): Flow<GameInitStatus> {
        check(btHandler != null) { "bluetooth Handler is not initialized" }
        if (btFlow == null) {
            btFlow = btHandler.createGame(config.toSettings()).receiveAsFlow().onEach { state ->
                when (state) {
                    is GameInitStatus.Awaiting -> {
                    }
                    is GameInitStatus.Connected -> {
                        val model = with(config) {
                            BluetoothServerGameModel(rows, cols, win, player1, player2, scope, state.server)
                        }
                        goToGame(model)
                    }
                }
            }
        }
        return btFlow!!
    }

    private fun goToGame(model: GameModel) {
        scopeBounded = false
        backstack.goTo(GameScreen(model, scope))
    }
}