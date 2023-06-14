package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.GameSettings
import com.example.controllers.models.BluetoothServerGameModel
import com.example.controllers.models.GameModel
import com.example.controllers.models.NetworkGameModel
import com.example.controllers.models.PlayerType
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.example.transport.Application
import com.example.transport.Awaiting
import com.example.transport.BluetoothLEInteractor
import com.example.transport.ClientJoined
import com.example.transport.Connected
import com.example.transport.ConnectedGame
import com.example.transport.Connecting
import com.example.transport.ConnectingFailure
import com.example.transport.ConnectionStatus
import com.example.transport.CreatingFailure
import com.example.transport.Failed
import com.example.transport.GameCreateStatus
import com.example.transport.Started
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class GameBluetoothLECreator(
    private val backstack: Backstack, private val handler: BluetoothLEInteractor
) : ScopedServices.Registered {
    private var scope = createScope()
    private var scopeBounded = true
    var game: Application? = null
        private set

    override fun onServiceRegistered() = Unit

    override fun onServiceUnregistered() {
        if (scopeBounded) scope.cancel()
    }

    private fun createScope() = CoroutineScope(Job() + Dispatchers.Main.immediate)

    fun registerBluetoothGame(settings: GameSettings) {
        game?.let { it.settings = settings } ?: handler.createApplication(settings).apply {
            game = this
        }
        game!!.registerApplication()
    }

    fun unregisterBluetoothGame() {
        scope.cancel()
        scope = createScope()
    }

    private fun createLEGame() {
        game?.run {
            scope.launch {
                val state = announceGame().onEach { state ->
                    if (state is Started) println("started announsing")
                }.last()
                val client = when (state) {
                    is ClientJoined -> state
                    is Failed -> {
                        unregisterApplication()
                        return@launch
                    }
                    else -> throw IllegalStateException()
                }

                val config = backstack.lookup<GameConfig>().apply {
                    rows = settings.rows
                    cols = settings.cols
                    win = settings.win
                    player1 = PlayerType.Human
                    player2 = PlayerType.Bluetooth
                    //fromSettings(app.settings)
                    println("rows:$rows, cols:$cols, win:$win, mark:${settings.creatorMark} player1:$player1, player2:$player2")
                }
                val model = with(config) {
                    BluetoothServerGameModel(rows, cols, win, scope, client.server)
                }
                goToGame(model)
            }
        }
    }

    private fun goToGame(model: GameModel) {
        scopeBounded = false
        backstack.goTo(GameScreen(model, scope))
    }
}