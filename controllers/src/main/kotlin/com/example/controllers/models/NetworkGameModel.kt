package com.example.controllers.models

import com.example.controllers.BotPlayer
import com.example.controllers.LocalPlayer
import com.example.controllers.NetworkClient
import com.example.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.trySendBlocking
import java.io.Closeable


class NetworkGameModel(
    rows: Int,
    cols: Int,
    win: Int,
    player1: PlayerType,
    player2: PlayerType,
    scope: CoroutineScope,
    private val client: NetworkClient
) : GameModel(rows, cols, win, player1, player2, scope) { // GameModel( scope) {
    private val externalController = ExternalController(rows, cols, win)
    override val controller: GameController = externalController
    override val gameLoop: Job

    companion object {
        const val LGM_TAG = "LocalGameModel"
    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        println("get error")
        val interruption = throwable as InterruptionException
        println("try to send error")
        gameChannel.trySendBlocking(GameInterruption(interruption.reason))
        gameChannel.close()
    }

    internal lateinit var localPlayer: LocalPlayer
    internal var localPlayerMask = 0

    init {
        arrayOf(player1, player2).forEachIndexed { i, player ->
            if (player == PlayerType.Human || player == PlayerType.Bot) {
                localPlayerMask = i
                localPlayer = when (player) {
                    PlayerType.Human -> clickRegister
                    PlayerType.Bot -> BotPlayer(controller)
                    else -> throw IllegalArgumentException("expect local player")
                }
            }
        }

        gameLoop = scope.launch(handler, CoroutineStart.LAZY) {
            while (controller.turn < cols * rows && isActive) {
                var move: Coord
                if (controller.curPlayer() == localPlayerMask) {
                    move = localPlayer.getMove()
                    client.sendMove(move)
                } else {
                    move = client.getMove()
                }
                moveTo(move)

                val condition = client.getState()
                externalController.sendState(condition)

                when (condition) {
                    is Continues -> {}
                    else -> {
                        val end = EndState(condition)
                        gameChannel.send(end)
                        gameChannel.close()
                        endSignal = end
                        return@launch
                    }
                }
            }
        }.apply {
            invokeOnCompletion {
                if (client is Closeable) client.close()
            }
        }
    }

    override fun cancel() {
        super.cancel()
        client.cancelGame()
    }
}


sealed class ServerResponse
data class GameMove(override val row: Int, override val col: Int) : ICoord, ServerResponse()
data class State(val state: GameState) : ServerResponse()
data class Interruption(val cause: InterruptCause) : ServerResponse()

enum class InterruptCause(val code: Int) {
    Disconnected(1000), OppLeave(1001), OppCheating(1002), Cheating(1003)
}