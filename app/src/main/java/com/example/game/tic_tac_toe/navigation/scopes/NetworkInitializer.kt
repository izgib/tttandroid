package com.example.game.tic_tac_toe.navigation.scopes

import com.example.game.controllers.Created
import com.example.game.controllers.GameCreationStatus
import com.example.game.controllers.GameItem
import com.example.game.controllers.NetworkInteractor
import com.example.game.controllers.models.GameModel
import com.example.game.controllers.models.NetworkGameModel
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

class NetworkInitializer(private val handler: NetworkInteractor, private val backstack: Backstack) : ScopedServices.Registered, ScopedServices.Activated {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var scopeBounded = true
    val gameList: MutableList<GameItem> = ArrayList()

    private var connectFlow: Flow<GameCreationStatus>? = null
    private var findFlow: Flow<Unit>? = null


    override fun onServiceUnregistered() {
        if (scopeBounded) scope.cancel()
    }

    override fun onServiceRegistered() = Unit

/*    private fun CoroutineScope.findGames(settings: GameFindConfig) = produce<GameItem>(capacity = Channel.CONFLATED) {
        with(settings) {
            return@with handler.GameList(rows, cols, win, mark).buffer()
        }
    }
        with(settings) {
            handler.GameList(rows, cols, win, mark).buffer().onEach { game -> gameList.add(game) }
        }.catch { t -> close(t) }.collect { send(Unit) }
        invokeOnClose { findFlow = null }
    }.receiveAsFlow()*/

    fun findGames(settings: GameFindConfig): Flow<GameItem> {
        return with(settings) {
            handler.GameList(rows, cols, win, mark).buffer()
        }
    }
    /*if (findFlow == null) {
        findFlow = scope.findGames(settings)
    } else {
        findFlow
    }

    return findFlow!!
}*/

    private fun CoroutineScope.connectToServer(config: GameItem) = produce<GameCreationStatus>(capacity = Channel.CONFLATED) {
        handler.JoinGame(scope, config.ID).collect { state ->
            send(state)
            if (state is Created) {
                val gameConfig = backstack.lookup<GameConfig>().apply {
                    fromSettings(config.settings)
                }
                val model = with(gameConfig) {
                    NetworkGameModel(rows, cols, win, player1, player2, scope, state.client)
                }
                goToGame(model)
            }
        }
        invokeOnClose { connectFlow = null }
    }.receiveAsFlow()

    fun joinGame(settings: GameItem): Flow<GameCreationStatus> {
        if (connectFlow == null) {
            connectFlow = scope.connectToServer(settings)
        } else {
            connectFlow
        }

        return connectFlow!!
    }

    override fun onServiceInactive() = gameList.clear()
    override fun onServiceActive() = Unit

    private fun goToGame(model: GameModel) {
        scopeBounded = false
        backstack.goTo(GameScreen(model, scope))
    }
}