package com.example.controllers.models


import com.example.controllers.BotPlayer
import com.example.controllers.LocalPlayer
import com.example.game.Continues
import com.example.game.Coord
import com.example.game.Game
import com.example.game.GameController
import kotlinx.coroutines.*

class LocalGameModel(rows: Int, cols: Int, win: Int, player1: PlayerType, player2: PlayerType, scope: CoroutineScope) : GameModel(rows, cols, win, player1, player2, scope) {
    private val game = Game(rows, cols, win)
    override val controller: GameController = game
    override val gameLoop: Job
    val players: Array<LocalPlayer>

    companion object {
        const val LGM_TAG = "LocalGameModel"
    }

    init {
        val playerX = when (player1) {
            PlayerType.Human -> clickRegister
            PlayerType.Bot -> BotPlayer(controller)
            else -> throw IllegalArgumentException("expect only local players")
        }

        val playerO = when (player2) {
            PlayerType.Human -> clickRegister
            PlayerType.Bot -> BotPlayer(controller)
            else -> throw IllegalArgumentException("expect only local players")
        }
        players = arrayOf(playerX, playerO)

        gameLoop = scope.launch(start = CoroutineStart.LAZY) {
            var move: Coord
            while (isActive) {
                move = players[controller.curPlayer()].getMove()
                moveTo(move)
                when (val state = game.gameState(move)) {
                    is Continues -> Unit
                    else -> {
                        val end = EndState(state)
                        gameChannel.send(end)
                        gameChannel.close()
                        endSignal = end
                        return@launch
                    }
                }
            }
        }
    }
}