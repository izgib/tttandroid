package com.example.game.controllers

//import android.util.Log
import com.example.game.domain.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.EmptyCoroutineContext

class NetworkGameModel(rows: Int, cols: Int, win: Int, private val client: NetworkClient) : GameController(rows, cols, win), GameModel {
    override val crossObserver: Channel<Coord> = Channel()
    override val noughtObserver: Channel<Coord> = Channel()
    override val endedObserver: Channel<GameState> = Channel()
    override val interruptObserver: Channel<Interruption> = Channel()
    private val plObservers = arrayOf(crossObserver, noughtObserver)

    override val clickRegister: ClickRegister by lazy { ClickRegister(this::isValidMove) }
    private val marks: Array<Mark> = arrayOf(Mark.Cross, Mark.Nought)

    companion object {
        const val NGM_TAG = "NtGameModel"
    }

    private var gameLoop = GlobalScope.launch(EmptyCoroutineContext, CoroutineStart.LAZY) {
        //Log.d(NGM_TAG, "GameLoop started")

        while (turn < cols * rows && isActive) {
            //Log.d(NGM_TAG, "move: $turn")

            var move: Coord
            if (curPlayer() == localPlayerMask) {
                move = localPlayer.getMove()
                client.sendMove(move)?.let {
                    interruptObserver.send(it)
                    return@launch
                }
            } else {
                when (val resp = client.getMove()) {
                    is Success -> move = resp.value
                    is Failure -> {
                        interruptObserver.send(resp.value)
                        return@launch
                    }
                }
            }

            when (val condition = client.getState()) {
                is Success -> {
                    moveTo(move)
                    when (condition.value) {
                        is Continues -> turn++
                        else -> {
                            endedObserver.send(condition.value)
                            return@launch
                        }
                    }
                }
                is Failure -> {
                    interruptObserver.send(condition.value)
                    return@launch
                }
            }
        }
    }

    //override fun getLifecycle(): Lifecycle = lifecycleRegistry

    private lateinit var localPlayer: LocalPlayer

    private var localPlayerMask = 0

    private suspend fun moveTo(move: Coord) {
        gameField[move.i][move.j] = marks[curPlayer()]
        plObservers[curPlayer()].send(move)
    }

    override fun initPlayers(player1: PlayerType, player2: PlayerType) {
        arrayOf(player1, player2).forEachIndexed { i, player ->
            if (player == PlayerType.Human || player == PlayerType.Bot) {
                localPlayerMask = i
                localPlayer = when (player) {
                    PlayerType.Human -> clickRegister
                    PlayerType.Bot -> BotPlayer(this)
                    else -> throw IllegalArgumentException("expect local player")
                }
            }
        }
    }

    override fun start() {
        gameLoop.start()
        GlobalScope.launch {
            gameLoop.join()
        }
    }

    override fun reload(): MarkLists {
        val crosses = ArrayList<Coord>(turn / 2 + curPlayer())
        val nougths = ArrayList<Coord>(turn / 2)
        gameField.forEachIndexed { rowInd, row ->
            row.forEachIndexed { colInd, mark ->
                when (mark) {
                    Mark.Cross -> crosses.add(Coord(rowInd, colInd))
                    Mark.Nought -> nougths.add(Coord(rowInd, colInd))
                    Mark.Empty -> {
                    }
                }
            }
        }
        return MarkLists(crosses, nougths)
    }

    override fun cancel() {
        gameLoop.cancel()
        runBlocking { client.CancelGame() }
    }
}

sealed class ServerResponse
data class GameMove(override val i: Int, override val j: Int) : ICoord, ServerResponse()
data class State(val state: GameState) : ServerResponse()
data class Interruption(val cause: InterruptCause) : ServerResponse()

enum class InterruptCause {
    OppDisconnected, Disconnected, OppCheating, Cheating
}