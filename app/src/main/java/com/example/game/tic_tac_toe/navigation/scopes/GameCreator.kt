package com.example.game.tic_tac_toe.navigation.scopes

import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.controllers.Created
import com.example.controllers.CreationFailure
import com.example.controllers.GameID
import com.example.controllers.GameInitializer
import com.example.controllers.GameSettings
import com.example.controllers.models.BluetoothServerGameModel
import com.example.controllers.models.GameModel
import com.example.controllers.models.LocalGameModel
import com.example.controllers.models.NetworkGameModel
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.base.notifications
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.example.transport.Application
import com.example.transport.BluetoothInteractor
import com.example.transport.BluetoothInteractorImpl
import com.example.transport.BluetoothLEInteractor
import com.example.transport.ClientJoined
import com.example.transport.Connected
import com.example.transport.CreatingFailure
import com.example.transport.Failed
import com.example.transport.Started
import com.example.transport.service.NetworkInteractor
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class GameCreator(
    private val backstack: Backstack,
) : ScopedServices.Registered {
    private var scope = createScope()
    private var scopeBounded = true

    private val _started = MutableStateFlow<Boolean>(false)

    val network by lazy { Network(NetworkInteractor.newInstance()) }
    val bluetoothClassic: Creator by lazy { BluetoothClassic(BluetoothInteractorImpl) }
    val bluetoothLE: Creator by lazy { BluetoothLE(backstack.lookup()) }

    override fun onServiceUnregistered() {
        if (scopeBounded) scope.cancel()
    }

    override fun onServiceRegistered() = Unit
    private fun createScope() = CoroutineScope(Job() + Dispatchers.Main.immediate)

    private fun cancelAndRecreateScope(): Unit = scope.let { oldScope ->
        scope = createScope()
        _started.value = false
        oldScope.cancel()
    }

    fun createLocalGame(config: GameConfig) {
        val model = with(config) {
            LocalGameModel(rows, cols, win, scope)
        }
        goToGame(model)
    }


    /*fun createNetworkGame(config: GameConfig): Flow<GameCreationStatus> = with(config) {
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
                            NetworkGameModel(rows, cols, win, scope, state.client)
                        }
                        goToGame(model)
                    }

                    is CreationFailure -> notManager.creationFailed()
                }
            }.onCompletion { netFlow = null }
        }
        return@with netFlow!!
    }*/

    /*fun createBluetoothGame(config: GameConfig): Flow<GameCreateStatus> {
        check(btHandler != null) { "bluetooth Handler is not initialized" }
        return btHandler.createGame(config.toSettings()).onEach { state ->
            when (state) {
                is Awaiting -> {
                }

                is Connected -> {
                    val model = with(config) {
                        BluetoothServerGameModel(rows, cols, win, scope, state.server)
                    }
                    goToGame(model)
                }

                CreatingFailure -> println("can not create game")
            }
        }
    }*/

    fun goToGame(model: GameModel) {
        scopeBounded = false
        backstack.goTo(GameScreen(model, scope))
    }

    private inner class BluetoothClassic(private val handler: BluetoothInteractor) : Creator {
        override val started: StateFlow<Boolean> = _started
        override fun create(settings: GameSettings) {
            scope.launch {
                val final = handler.createGame(settings).onStart {
                    _started.value = true
                }.last()
                _started.value = false
                when (final) {
                    is Connected -> with(settings) {
                        goToGame(BluetoothServerGameModel(rows, cols, win, scope, final.server))
                    }

                    CreatingFailure -> return@launch
                    else -> throw IllegalStateException()
                }
            }
        }

        override fun cancel() = cancelAndRecreateScope()
    }

    inner class Network internal constructor(private val handler: NetworkInteractor) :
        Creator {
        private val _gameID = MutableStateFlow<Int?>(null)
        val gameID: StateFlow<Int?> = _gameID
        private var initializer: GameInitializer? = null
        private val notManager = backstack.notifications
        override val started: StateFlow<Boolean> = _started
        override fun create(settings: GameSettings) {
            initializer = handler.CreateGame(scope)
            notManager.setupNotification(initializer!!)
            scope.launch {
                val final = initializer!!.sendCreationRequest(settings).onStart {
                    _started.value = true
                }.onEach { state ->
                    when (state) {
                        is GameID -> {
                            _gameID.value = state.ID
                            notManager.setupGameID(state)
                        }

                        else -> return@onEach
                    }
                }.last()
                _started.value = false

                when (final) {
                    is Created -> {
                        notManager.gameCreated()
                        with(settings) {
                            goToGame(NetworkGameModel(rows, cols, win, scope, final.client))
                        }
                    }

                    CreationFailure -> notManager.creationFailed()
                    else -> throw IllegalStateException()
                }
            }
        }

        override fun cancel() {
            initializer?.let {
                it.cancelGame()
                println("initalizer canceling game")
            }
            cancelAndRecreateScope()
        }
    }

    private inner class BluetoothLE(private val handler: BluetoothLEInteractor) : Creator {
        var game: Application? = null
        override val started: StateFlow<Boolean> = _started

        init {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BluetoothAdapter.getDefaultAdapter().apply {
                    println("advertisingSupport: $isMultipleAdvertisementSupported")
                    println("advertiser: $bluetoothLeAdvertiser")
                }
            }
        }

        override fun create(settings: GameSettings) {
            game?.let { it.settings = settings } ?: handler.createApplication(settings).apply {
                game = this
            }
            game!!.registerApplication()

            scope.launch {
                val final = game!!.announceGame().onStart {
                    _started.value = true
                }.onEach { state ->
                    if (state is Started) println("started announsing")
                }.last()
                _started.value = false

                when (final) {
                    is ClientJoined -> with(settings) {
                        goToGame(BluetoothServerGameModel(rows, cols, win, scope, final.server))
                    }

                    Failed -> {
                        game!!.unregisterApplication()
                        return@launch
                    }

                    else -> {
                        game!!.unregisterApplication()
                        throw IllegalStateException()
                    }
                }
            }
        }

        override fun cancel() = cancelAndRecreateScope()
    }

    interface Creator {
        val started: StateFlow<Boolean>
        fun create(settings: GameSettings)
        fun cancel(): Unit
    }
}