package com.example.controllers.models

import com.example.controllers.*
import com.example.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.cancellation.CancellationException

class BluetoothServerGameModel(
    rows: Int,
    cols: Int,
    win: Int,
    scope: CoroutineScope,
    private val server: NetworkServer
) : GameModel(rows, cols, win, scope) { //GameModel( scope) {
    private val game = Game(rows, cols, win)
    override val controller: GameController = game
    internal var localPlayer: LocalPlayer? = null
    internal var localPlayerMask = 0

    override var gameChannel = Channel<GameSignal>(2)
        private set
    override lateinit var gameLoop: Job
        private set


    companion object {
        const val BSGM_TAG = "BtSrvGameModel"
    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        println("get error: $throwable")
        val interruption = throwable as InterruptionException
        println("try to send error")
        val signal = GameInterruption(interruption.reason)
        gameChannel.trySend(signal)
        endSignal = signal
        localPlayer = null
        gameChannel.close()
    }

    override fun start() {
        controller.clearField()
        endSignal = null
        check(localPlayer != null) { "local player not initialized" }
        gameLoop = scope.launch(handler) {
            while (isActive) {
                var sendMove: Coord? = null
                val playerMove: Coord? = if (controller.curPlayer() == localPlayerMask) {
                    sendMove = localPlayer!!.getMove()
                    sendMove
                } else {
                    when (val resp = server.getResponse()) {
                        is ClientMove -> resp.move
                        is ClientAction -> when (resp.action) {
                            PlayerAction.Leave -> {
                                val cause = InterruptCause.Leave
                                sendInterruption(cause)
                                val signal = GameInterruption(cause)
                                gameChannel.trySend(signal)
                                gameChannel.close()
                                localPlayer = null
                                endSignal = signal
                                return@launch
                            }
                            PlayerAction.GiveUp -> null
                        }
                    }
                }
                if (playerMove == null) {
                    val giveUpState = Win(
                        EndWinLine(
                            Mark.values()[controller.otherPlayer()], null, null
                        )
                    )
                    server.sendTurn(state = giveUpState)
                    cleanUp(giveUpState)
                    return@launch
                }
                if (!game.isValidMove(playerMove)) {
                    val cause = InterruptCause.InvalidMove
                    server.sendInterruption(cause)
                    val signal = GameInterruption(cause)
                    sendInterruption(cause)
                    gameChannel.close()
                    localPlayer = null
                    endSignal = signal
                    return@launch
                }

                moveTo(playerMove)
                val condition = game.gameState(playerMove)
                server.sendTurn(sendMove, condition)

                when (condition) {
                    is Continues -> {}
                    else -> {
                        cleanUp(condition)
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
        runBlocking { server.sendInterruption(InterruptCause.Leave) }
        super.cancel()
    }

    private suspend fun sendInterruption(cause: InterruptCause) {
        gameChannel.send(GameInterruption(cause))
    }

    private fun CoroutineScope.cleanUp(state: GameState) {
        val signal = EndState(state)
        gameChannel.trySend(signal)
        gameChannel.close()
        endSignal = signal
        localPlayer = null
        gameChannel = Channel<GameSignal>(2)
    }
}
