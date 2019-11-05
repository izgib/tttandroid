package com.example.game.controllers

//import android.util.Log
import com.example.game.domain.game.Continues
import com.example.game.domain.game.Coord
import com.example.game.domain.game.Game
import com.example.game.domain.game.GameState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.EmptyCoroutineContext

class BluetoothServerGameModel(rows: Int, cols: Int, win: Int, server: NetworkServer) : GameModel {
    override val crossObserver: Channel<Coord> = Channel()
    override val noughtObserver: Channel<Coord> = Channel()
    override val endedObserver: Channel<GameState> = Channel()
    override val interruptObserver: Channel<Interruption> = Channel()
    /*override val crossObserver: MutableLiveData<Coord> = SingleLiveEvent()
    override val noughtObserver: MutableLiveData<Coord> = SingleLiveEvent()
    override val endedObserver: MutableLiveData<GameState> = SingleLiveEvent()
    override val interruptObserver: MutableLiveData<Interruption> = SingleLiveEvent()*/
    private val plObservers = arrayOf(crossObserver, noughtObserver)

    private val game = Game(rows, cols, win)

    private val gameLoop: Job

    override val clickRegister: ClickRegister by lazy { ClickRegister(game::isValidMove) }
    private lateinit var localPlayer: LocalPlayer

    private var localPlayerMask = 0

    companion object {
        const val BSGM_TAG = "BtSrvGameModel"
    }

    init {
        gameLoop = GlobalScope.launch(EmptyCoroutineContext, CoroutineStart.LAZY) {
            //Log.d(BSGM_TAG, "GameLoop started")

            while (isActive) {
                var move: Coord
                if (game.curPlayer() == localPlayerMask) {
                    move = localPlayer.getMove()
                    moveTo(move)
                    server.sendMove(move)?.let {
                        interruptObserver.send(Interruption(InterruptCause.OppDisconnected))
                        return@launch
                    }
                } else {
                    when (val resp = server.getMove()) {
                        is Success -> {
                            move = resp.value
                        }
                        is Failure -> {
                            interruptObserver.send(resp.value)
                            return@launch
                        }
                    }
                    if (!game.isValidMove(move)) {
                        val interruption = Interruption(InterruptCause.OppCheating)
                        interruptObserver.send(interruption)
                        server.sendInterruption(interruption)
                        return@launch
                    }
                    moveTo(move)
                }

                val gameState = game.gameState(move)
                server.sendState(gameState)?.let {
                    interruptObserver.send(it)
                    return@launch
                }
                when (gameState) {
                    is Continues -> {
                    }
                    else -> {
                        endedObserver.send(gameState)
                        return@launch
                    }
                }
            }
        }
        //Log.d(BSGM_TAG, "initialized")
        //lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        //Log.d("GModel", "rows=$rows, cols=$cols, win=$win")

    }

    //override fun getLifecycle(): Lifecycle = lifecycleRegistry

    override fun initPlayers(player1: PlayerType, player2: PlayerType) {
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
    }

    private suspend fun moveTo(move: Coord) {
        game.moveTo(move)
        plObservers[game.curPlayer()].send(move)
    }

    override fun start() {
        gameLoop.start()
        GlobalScope.launch {
            gameLoop.join()
            //Log.d(BSGM_TAG, "game ended")
            //lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    override fun reload() = game.getMarks()

    override fun cancel() {
        gameLoop.cancel()
    }
}
