package com.example.controllers.models


import com.example.controllers.LocalPlayer
import com.example.controllers.PlayerInitializer
import com.example.game.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocalGameModel(rows: Int, cols: Int, win: Int, scope: CoroutineScope) :
    GameModel(rows, cols, win, scope) {
    private val game = Game(rows, cols, win)
    override val controller: GameController = game
    override var gameChannel = Channel<GameSignal>(2)
        private set
    override lateinit var gameLoop: Job
        private set

    private val players = arrayOfNulls<LocalPlayer?>(2)


    companion object {
        const val LGM_TAG = "LocalGameModel"
    }

    private fun CoroutineScope.cleanUp(state: GameState) {
        val signal = EndState(state)
        gameChannel.trySend(signal)
        val oldChannel = gameChannel
        endSignal = signal
        players.fill(null)
        gameChannel = Channel<GameSignal>(2)
        oldChannel.close()
    }

    override fun start() {
        endSignal = null
        check(players.none { it == null }) { "players not initialized" }
        gameLoop = scope.launch() {
            var move: Coord?
            while (isActive) {
                move = players[controller.curPlayer()]!!.getMove()
                if (move == null) {
                    cleanUp(
                        Win(
                            EndWinLine(
                                Mark.values()[controller.otherPlayer()], null, null
                            )
                        )
                    )
                    return@launch
                }
                moveTo(move)
                when (val state = game.gameState(move)) {
                    is Continues -> Unit
                    else -> {
                        cleanUp(state)
                        return@launch
                    }
                }
            }
        }
    }

    override fun setupPlayerX(player: LocalPlayer) {
        players[0] = player
    }

    override fun setupPlayerO(player: LocalPlayer) {
        players[1] = player
    }
}