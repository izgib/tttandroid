package com.example.game.controllers.models

import com.example.game.controllers.BotPlayer
import com.example.game.controllers.LocalPlayer
import com.example.game.controllers.NetworkServer
import com.example.game.domain.game.Continues
import com.example.game.domain.game.Coord
import com.example.game.domain.game.Game
import com.example.game.domain.game.GameController
import kotlinx.coroutines.*

class BluetoothServerGameModel(rows: Int, cols: Int, win: Int, player1: PlayerType, player2: PlayerType, scope: CoroutineScope, private val server: NetworkServer) : GameModel(rows, cols, win, player1, player2, scope) { //GameModel( scope) {
    private val game = Game(rows, cols, win)
    override val controller: GameController = game
    override val gameLoop: Job

    internal lateinit var localPlayer: LocalPlayer
    internal var localPlayerMask = 0

    companion object {
        const val BSGM_TAG = "BtSrvGameModel"
    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        scope.launch {
            gameChannel.send(GameInterruption((throwable as InterruptionException).reason))
        }
    }

    init {
        arrayOf(player1, player2).forEachIndexed { i, player ->
            if (player == PlayerType.Human || player == PlayerType.Bot) {
                localPlayerMask = i
                localPlayer = when (player) {
                    PlayerType.Human -> clickRegister
                    PlayerType.Bot -> BotPlayer(game)
                    else -> throw IllegalArgumentException("expect local player")
                }
            }
        }

        gameLoop = scope.launch(handler, CoroutineStart.LAZY) {
            while (isActive) {
                var move: Coord
                if (game.curPlayer() == localPlayerMask) {
                    move = localPlayer.getMove()
                    moveTo(move)
                    server.sendMove(move)
                } else {
                    move = server.getMove()
                    if (!game.isValidMove(move)) {
                        sendInterruption(InterruptCause.OppCheating)
                        server.sendInterruption(Interruption(InterruptCause.Cheating))
                        return@launch
                    }
                    moveTo(move)
                }

                val gameState = game.gameState(move)
                server.sendState(gameState)
                when (gameState) {
                    is Continues -> {
                    }
                    else -> {
                        val end = EndState(gameState)
                        gameChannel.send(end)
                        gameChannel.close()
                        endSignal = end
                        return@launch
                    }
                }
            }
        }
    }

    override fun cancel() {
        super.cancel()
        scope.launch {
            server.sendInterruption(Interruption(InterruptCause.Disconnected))
        }
    }

    private suspend fun sendInterruption(cause: InterruptCause) {
        gameChannel.send(GameInterruption(cause))
    }

}
