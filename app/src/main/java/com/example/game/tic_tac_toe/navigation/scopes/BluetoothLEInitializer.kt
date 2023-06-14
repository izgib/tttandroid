package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.models.GameModel
import com.example.controllers.models.NetworkGameModel
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.example.transport.BluetoothGameItem
import com.example.transport.BluetoothLEInteractor
import com.example.transport.ConnectedGame
import com.example.transport.ConnectingFailure
import com.example.transport.ConnectionStatus
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class BluetoothLEInitializer(private val backstack: Backstack) : ScopedServices.Registered {
    private val leInteractor = backstack.lookup<BluetoothLEInteractor>()

    private var scope = createScope()
    private var scopeBound = true
    private val _started = MutableStateFlow<Boolean>(false)
    val started: StateFlow<Boolean> = _started

    val gameFound = mutableListOf<BluetoothGameItem>()

    private fun createScope() = CoroutineScope(Job() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() = Unit

    override fun onServiceUnregistered() {
        if (scopeBound) scope.cancel()
    }

    private fun cancelAndRecreate(): Unit = scope.let { oldScope ->
        games?.let { oldGames ->
            games = null
            oldGames.close()
        }
        scope = createScope()
        _started.value = false
        oldScope.cancel()
    }

    private var games: Channel<Unit>? = null

    // Return flow that emits Unit when found device append into deviceFoundList, flow gets canceled after device
    fun findGames(): Flow<Unit> {
        if (games == null) {
            _started.value = true
            games = Channel(Channel.CONFLATED)
            scope.launch {
                leInteractor.getDeviceList().onEach {
                    gameFound.add(it)
                    println("trying to send: $it")
                    games!!.trySend(Unit)
                }.collect()
                games?.let { oldGames ->
                    games = null
                    _started.value = false
                    oldGames.close()
                }
            }
        }
        return games!!.receiveAsFlow()
    }

    private val jState = MutableStateFlow<ConnectionStatus?>(null)
    fun joinGame(game: BluetoothGameItem): Flow<ConnectionStatus> {
        if (jState.value == null) {
            scope.launch {
                val final = leInteractor.connectGame(game.device, game.settings).onEach {
                    jState.value = it
                }.last()

                when (final) {
                    is ConnectedGame -> {
                        val gameConfig = backstack.lookup<GameConfig>().apply {
                            fromSettings(final.params)
                        }
                        with(gameConfig) {
                            goToGame(NetworkGameModel(rows, cols, win, scope, final.client))
                        }
                    }

                    ConnectingFailure -> println("failure")
                    else -> throw IllegalStateException()
                }
                jState.value = null
            }
        }
        return jState.takeWhile { it != null } as Flow<ConnectionStatus>
    }

    fun goToGame(model: GameModel) {
        scopeBound = false
        backstack.goTo(GameScreen(model, scope))
    }

    fun cancel() {
        cancelAndRecreate()
    }
}