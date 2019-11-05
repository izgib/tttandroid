package com.example.game.tic_tac_toe.viewmodels

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.game.controllers.*
import com.example.game.domain.game.GameRules
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinComponent
import org.koin.core.inject


class GameInitializerModel : ViewModel(), KoinComponent {
    private val btInteractor: BluetoothInteractor by inject()
    private val ntInteractor: NetworkInteractor by inject()

    val rowsLeft = MutableLiveData<Int>().apply { value = GameRules.ROWS_MIN }
    val rowsRight = MutableLiveData<Int>().apply { value = GameRules.ROWS_MAX }

    var colsLeft = MutableLiveData<Int>().apply { value = GameRules.COLS_MIN }
    val colsRight = MutableLiveData<Int>().apply { value = GameRules.COLS_MAX }

    var winLeft = MutableLiveData<Int>().apply { value = GameRules.WIN_MIN }
    var winRight = MutableLiveData<Int>().apply { value = GameRules.WIN_MAX }

    val winMax = MutableLiveData<Int>().apply { value = GameRules.WIN_MAX }
    private var rowsHighBound = rowsLeft.value!!
    private var colsHighBound = rowsLeft.value!!

    private var mark: Byte = Mark.Cross.mark

    init {
        rowsLeft.observeForever {
            rowsHighBound = maxOf(it, rowsRight.value!!)
            updateWinMax()
        }
        rowsRight.observeForever {
            rowsHighBound = maxOf(it, rowsLeft.value!!)
            updateWinMax()
        }
        colsLeft.observeForever {
            colsHighBound = maxOf(it, colsRight.value!!)
            updateWinMax()
        }
        colsRight.observeForever {
            colsHighBound = maxOf(it, colsLeft.value!!)
            updateWinMax()
        }
    }

    private fun updateWinMax() {
        var updatedValue = minOf(rowsHighBound, colsHighBound, GameRules.WIN_MAX)
        if (updatedValue == GameRules.WIN_MIN) {
            winLeft.value = GameRules.WIN_MIN
            winRight.value = GameRules.WIN_MIN
            updatedValue = GameRules.WIN_MIN + 1
        }
        if (winMax.value != updatedValue) {
            if (winLeft.value!! > updatedValue) {
                winLeft.value = updatedValue
            }
            if (winRight.value!! > updatedValue) {
                winRight.value = updatedValue
            }
            winMax.value = updatedValue
        }
    }

    fun changeMark(id: Int) {
        mark = when (id) {
            R.id.crossMark -> Mark.Cross
            R.id.noughtMark -> Mark.Nought
            R.id.anyMark -> Mark.Empty
            else -> throw IllegalArgumentException("wrong id")
        }.mark
        println("значение знака: $mark")
    }

    private fun getRangeParam(left: LiveData<Int>, right: LiveData<Int>): ParamRange {
        return ParamRange(minOf(left.value!!, right.value!!).toShort(), maxOf(left.value!!, right.value!!).toShort())
    }


/*    @ExperimentalCoroutinesApi
    fun createNt(rows: Int, cols: Int, win: Int, mark: Mark): LiveData<GameCreationStatus> {
        return liveData {
            emit(Loading)
            for (state in ntInteractor.CreateGame(rows.toShort(), cols.toShort(), win.toShort(), mark.mark)) {
                emit(state)
            }
        }
    }*/

    fun CreateNt() = ntInteractor.CreateGame()

    /*@ExperimentalCoroutinesApi
    fun testCreateNt(rows: Int, cols: Int, win: Int, mark: Mark) = ntInteractor.CreateGame(rows.toShort(), cols.toShort(), win.toShort(), mark.mark)*/


/*    @ExperimentalCoroutinesApi
    fun joinNt(game: GameItem): LiveData<GameCreationStatus> {
        return liveData {
            emit(Loading)
            for (state in ntInteractor.JoinGame(game.ID)) {
                emit(state)
            }
        }
    }*/

    @ExperimentalCoroutinesApi
    fun testJoinNt(game: GameItem) = ntInteractor.JoinGame(game.ID)

    @ExperimentalCoroutinesApi
    fun listNt(): Flow<GameItem> {
        return ntInteractor.GameList(
                getRangeParam(rowsLeft, rowsRight),
                getRangeParam(colsLeft, colsRight),
                getRangeParam(winRight, winLeft),
                mark)
    }

    fun cancelGame() {
        Log.d("INIT", "cancel game")
    }

    @ExperimentalCoroutinesApi
    fun createBt(rows: Int, cols: Int, win: Int, mark: Mark): LiveData<GameInitStatus> {
        return liveData {
            for (state in btInteractor.CreateGame(rows.toShort(), cols.toShort(), win.toShort(), mark.mark)) {
                emit(state)
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun joinBt(device: BluetoothDevice): LiveData<GameInitStatus> {
        return liveData {
            emit(GameInitStatus.Awaiting)
            for (state in btInteractor.JoinGame(device)) {
                emit(state)
            }
        }
    }
}

