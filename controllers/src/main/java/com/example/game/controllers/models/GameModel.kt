package com.example.game.controllers.models

import com.example.game.controllers.ClickRegister
import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameController
import com.example.game.domain.game.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow

enum class PlayerType {
    Bot, Human, Bluetooth, Network
}


abstract class GameModel(val rows: Int, val cols: Int, val win: Int, player1: PlayerType, player2: PlayerType, internal val scope: CoroutineScope) {
    protected abstract val controller: GameController
    abstract val gameLoop: Job
    internal val gameChannel = Channel<GameSignal>(2)
    internal var endSignal: GameSignal? = null
    val gameFlow
        get() = gameChannel.receiveAsFlow().onStart {
            if (endSignal != null) {
                emit(endSignal!!)
            }
        }

    val clickRegister: ClickRegister by lazy { ClickRegister(controller::isValidMove) }


    internal suspend fun moveTo(move: Coord) {
        controller.moveTo(move)
        gameChannel.send(when (controller.curPlayer()) {
            0 -> Cross(move)
            1 -> Nought(move)
            else -> throw IllegalStateException("expected only 0 or 1")
        })
    }

    fun start() {
        gameLoop.start()
    }

    open fun cancel() {
        gameLoop.cancel()
    }

    fun reload() = controller.getMarks()
}

enum class GameType {
    Local, Bluetooth, Network
}

data class GameParamsData(val rows: Int, val cols: Int, val win: Int, val player1: PlayerType, val player2: PlayerType)
data class ParamRange(override val start: Int, override val end: Int) : Range


interface Range {
    val start: Int
    val end: Int
}

class InterruptionException(val reason: InterruptCause) : Exception()


sealed class GameSignal
data class Cross(val move: Coord) : GameSignal()
data class Nought(val move: Coord) : GameSignal()
data class EndState(val state: GameState) : GameSignal()
data class GameInterruption(val cause: InterruptCause) : GameSignal()

