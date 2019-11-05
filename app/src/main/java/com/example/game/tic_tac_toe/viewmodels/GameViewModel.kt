package com.example.game.tic_tac_toe.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.controllers.*
import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameState
import com.example.game.tic_tac_toe.GameModelBuilder
import com.example.game.tic_tac_toe.utils.ConsumeLiveEvent
import com.example.game.tic_tac_toe.utils.SingleLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import org.koin.core.KoinComponent


@ExperimentalCoroutinesApi
class GameViewModel(gameParamsData: GameParamsData, gameType: GameType, isCreator: Boolean) : ViewModel(), KoinComponent {

    private val player1 = gameParamsData.player1
    private val player2 = gameParamsData.player2
    private val _crossObserver = SingleLiveEvent<Coord>()
    val crossObserver: LiveData<Coord> = _crossObserver
    private val _noughtObserver = SingleLiveEvent<Coord>()
    val noughtObserver: LiveData<Coord> = _noughtObserver
    private val _endedObserver = SingleLiveEvent<GameState>()
    val endedObserver: LiveData<GameState> = _endedObserver
    private val _interruptObserver = SingleLiveEvent<Interruption>()
    val interruptObserver: LiveData<Interruption> = _interruptObserver
    private val model: GameModel = GameModelBuilder().getModel(gameParamsData, gameType, isCreator)
    private var started = false

    init {
        viewModelScope.launch {
            whileSelect {
                model.noughtObserver.onReceive {
                    _noughtObserver.value = it
                    true
                }
                model.crossObserver.onReceive {
                    _crossObserver.value = it
                    true
                }
                model.endedObserver.onReceive {
                    _endedObserver.value = it
                    false
                }
                model.interruptObserver.onReceive {
                    _interruptObserver.value = it
                    false
                }
            }
        }
    }

    fun startGame() {
        model.initPlayers(player1, player2)
        model.start()
        started = true
    }

    fun reloadGame() = model.reload()

    fun isStarted() = started

    fun cancelGame() {
        model.cancel()
    }

    fun haveHumanPlayer() = player1 == PlayerType.Human || player2 == PlayerType.Human

    fun clickRegister(): ClickObserver = object : ClickObserver {
        private val _requestObserver = ConsumeLiveEvent<Unit>()
        override val requestObserver: LiveData<Unit>
            get() {
                viewModelScope.launch {
                    for (lol in model.clickRegister.requestObserver) {
                        _requestObserver.call()
                        Log.d("Model", "get move")
                    }
                }
                return _requestObserver
            }

        override fun moveTo(i: Int, j: Int): Boolean {
            if (model.clickRegister.moveTo(i, j)) {
                _requestObserver.hadConsumed()
                return true
            }
            return false
        }


    }
}

interface ClickObserver {
    val requestObserver: LiveData<Unit>
    // true is move valid
    fun moveTo(i: Int, j: Int): Boolean
}