package com.example.controllers.models

import com.example.controllers.LocalPlayer
import com.example.controllers.NetworkClient
import com.example.controllers.PlayerAction
import com.example.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking


class NetworkGameModel(
    rows: Int,
    cols: Int,
    win: Int,
    scope: CoroutineScope,
    private val client: NetworkClient
) : GameModel(rows, cols, win, scope) { // GameModel( scope) {
    private val externalController = ExternalController(rows, cols, win)
    override val controller: GameController = externalController
    override var gameChannel = Channel<GameSignal>(2)
        private set

    override lateinit var gameLoop: Job
        private set

    private val handler = CoroutineExceptionHandler { _, throwable ->
        println("get error")
        val interruption = throwable as InterruptionException
        println("try to send error net")
        val signal = GameInterruption(interruption.reason)
        gameChannel.trySend(signal)
        endSignal = signal
        gameChannel.close()
    }

    private var localPlayer: LocalPlayer? = null
    private var localPlayerMask = 0

    override fun start() {
        controller.clearField()
        endSignal = null
        check(localPlayer != null) { "local player not initialized" }
        gameLoop = scope.launch(handler) {
            while (isActive) {
                var move: Coord? = null
                if (controller.curPlayer() == localPlayerMask) {
                    move = localPlayer!!.getMove()
                    if (move == null) {
                        client.sendAction(PlayerAction.GiveUp)
                    } else {
                        client.sendMove(move)
                    }
                }
                val resp = client.getResponse()
                move = move ?: resp.move
                if (move != null) moveTo(move)

                val condition = resp.state
                externalController.sendState(condition)

                when (condition) {
                    is Continues -> {}
                    else -> {
                        val signal = EndState(condition)
                        gameChannel.trySend(signal)
                        gameChannel.close()
                        endSignal = signal
                        localPlayer = null
                        gameChannel = Channel<GameSignal>(2)
                        return@launch
                    }
                }
            }
        }
    }

    override fun setupPlayerX(player: LocalPlayer) {
        require(localPlayer == null) { "player initialization violation: player already initialized" }
        localPlayer = player
        localPlayerMask = 0
    }

    override fun setupPlayerO(player: LocalPlayer) {
        require(localPlayer == null) { "player initialization violation: player already initialized" }
        localPlayer = player
        localPlayerMask = 1
    }

    override fun cancel() {
        runBlocking { client.sendAction(PlayerAction.Leave) }
        super.cancel()
    }

    companion object {
        const val LGM_TAG = "LocalGameModel"
    }
}


sealed class ServerResponse
class Response(val move: Coord? = null, val state: GameState) : ServerResponse()
class Interruption(val cause: InterruptCause) : ServerResponse()

enum class InterruptCause(val code: Int) {
    Disconnected(1000),
    Leave(1001),
    InvalidMove(1002),
    Internal(1003)
}