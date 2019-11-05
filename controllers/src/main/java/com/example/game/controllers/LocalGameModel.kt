package com.example.game.controllers

//import android.util.Log
import com.example.game.domain.game.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.EmptyCoroutineContext


class LocalGameModel(rows: Int, cols: Int, win: Int) : GameModel {
    override val crossObserver: Channel<Coord> = Channel()
    override val noughtObserver: Channel<Coord> = Channel()
    override val endedObserver: Channel<GameState> = Channel()
    override val interruptObserver: Channel<Interruption> = Channel()
    /*    override val crossObserver: MutableLiveData<Coord> = SingleLiveEvent()
        override val noughtObserver: MutableLiveData<Coord> = SingleLiveEvent()
        override val endedObserver: MutableLiveData<GameState> = SingleLiveEvent()
        override val interruptObserver: MutableLiveData<Interruption> = SingleLiveEvent()*/
    private val plObservers = arrayOf(crossObserver, noughtObserver)

    private val game = Game(rows, cols, win)

    private val gameLoop: Job
    private lateinit var players: Array<LocalPlayer>
    //private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override val clickRegister: ClickRegister by lazy { ClickRegister(game::isValidMove) }

    companion object {

        const val LGM_TAG = "LocalGameModel"
    }

    init {
        //Log.d(LGM_TAG, "initialized")
        //lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        gameLoop = GlobalScope.launch(EmptyCoroutineContext, CoroutineStart.LAZY) {
            //Log.d(LGM_TAG, "GameLoop started")
            var move: Coord

            while (isActive) {
                move = players[game.curPlayer()].getMove()
                moveTo(move)
                when (val state = game.gameState(move)) {
                    is Continues -> {
                    }
                    else -> {
                        endedObserver.send(state)
                        return@launch
                    }
                }
            }
        }
    }

    override fun initPlayers(player1: PlayerType, player2: PlayerType) {
        val playerX = when (player1) {
            PlayerType.Human -> clickRegister
            PlayerType.Bot -> BotPlayer(game)
            else -> throw IllegalArgumentException("expect only local players")
        }

        val playerO = when (player2) {
            PlayerType.Human -> clickRegister
            PlayerType.Bot -> BotPlayer(game)
            else -> throw IllegalArgumentException("expect only local players")
        }
        players = arrayOf(playerX, playerO)
    }

    private suspend fun moveTo(move: Coord) {
        game.moveTo(move)
        plObservers[game.curPlayer()].send(move)
    }

    override fun start() {
        gameLoop.start()
        GlobalScope.launch {
            gameLoop.join()
            //Log.d(LGM_TAG, "game ended")
            //lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
    }

    override fun reload(): MarkLists = game.getMarks()

    override fun cancel() {
        gameLoop.cancel()
    }
}

