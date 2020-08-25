package com.example.game.tic_tac_toe.navigation.scopes

import com.example.game.controllers.models.GameModel
import com.example.game.domain.game.Coord
import com.example.game.tic_tac_toe.navigation.base.dialogs
import com.example.game.tic_tac_toe.navigation.screens.dialogs.DualResponse
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameExit
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GameController(private val backstack: Backstack, private val model: GameModel, private val scope: CoroutineScope) : ScopedServices.Registered, ScopedServices.HandlesBack {
    private var onBackHandler: Job? = null
    var started = false
        private set

    override fun onServiceUnregistered() {
        scope.cancel()
    }

    override fun onServiceRegistered() = Unit

    val gameFlow = model.gameFlow

    fun startGame() {
        model.start()
        started = true
    }

    fun reloadGame() = model.reload()

    fun isStarted() = started

    fun cancelGame() {
        model.cancel()
    }

    //fun haveHumanPlayer() = player1 == PlayerType.Human || player2 == PlayerType.Human

    fun clickRegister(): ClickObserver = object : ClickObserver {
        override val listenerStateFlow: Flow<Boolean> = model.clickRegister.listenerState
        override fun moveTo(move: Coord) {
            model.clickRegister.moveChannel.offer(move)
        }
    }

    override fun onBackEvent(): Boolean {
        println("here")
        if (onBackHandler == null) {
            onBackHandler = scope.launch {
                println("inside job")
                when (backstack.dialogs.showTest(GameExit())) {
                    DualResponse.Yes -> backstack.jumpToRoot()
                    DualResponse.No -> backstack.dialogs.goBack(false)
                }
                onBackHandler = null
            }
        }
        return true
    }
}

interface ClickObserver {
    val listenerStateFlow: Flow<Boolean>
    fun moveTo(move: Coord)
}