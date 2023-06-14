package com.example.controllers.models

import com.example.controllers.LocalPlayer
import com.example.game.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow

enum class PlayerType {
    Bot, Human, Bluetooth, Network
}


abstract class GameModel(val rows: Int, val cols: Int, val win: Int, internal val scope: CoroutineScope) {
    internal abstract val controller: GameController
    internal abstract val gameChannel: Channel<GameSignal>
    abstract val gameLoop: Job
    internal var endSignal: GameSignal? = null
    val gameFlow
        get() = gameChannel.receiveAsFlow()

    internal suspend fun moveTo(move: Coord) {
        controller.moveTo(move)
        gameChannel.send(when (controller.curPlayer()) {
            0 -> Cross(move)
            1 -> Nought(move)
            else -> throw IllegalStateException("expected only 0 or 1")
        })
    }

    abstract fun start()


    // Field must be cleared before players setup
    fun clearField() {
        controller.clearField()
    }

    abstract fun setupPlayerX(player: LocalPlayer)

    abstract fun setupPlayerO(player: LocalPlayer)

    open fun cancel() {
        gameChannel.close()
        gameLoop.cancel()
    }

    fun reload() = controller.getMarks()
}

enum class GameType {
    Local, BluetoothClassic, BluetoothLE, Network
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

