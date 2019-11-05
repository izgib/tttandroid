package com.example.game.tic_tac_toe.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.game.controllers.GameType
import com.example.game.controllers.PlayerType
import com.example.game.domain.game.GameRules
import com.example.game.domain.game.Mark


class GameSetupViewModel : ViewModel() {
    val rows = MutableLiveData<Int>().apply { value = GameRules.ROWS_MIN }
    var cols = MutableLiveData<Int>().apply { value = GameRules.COLS_MIN }
    var win = MutableLiveData<Int>().apply { value = GameRules.WIN_MIN }
    val winMax = MutableLiveData<Int>().apply { value = GameRules.WIN_MIN }
    var mark: Mark = Mark.Cross
    var player1: PlayerType = PlayerType.Human
    var player2: PlayerType = PlayerType.Human
    var isCreator: Boolean = true
    var player1Pos = MutableLiveData<Int>().apply { value = getSpinPosition(player1) }
    var player2Pos = MutableLiveData<Int>().apply { value = getSpinPosition(player2) }

    private var gameType: GameType = GameType.Local

    init {
        rows.observeForever {
            if (minOf(it, cols.value!!, GameRules.WIN_MAX) - GameRules.WIN_MIN != winMax.value!!) {
                winMax.value = it - GameRules.WIN_MIN
            }
        }
        cols.observeForever {
            if (minOf(it, rows.value!!, GameRules.WIN_MAX) - GameRules.WIN_MIN != winMax.value!!) {
                winMax.value = it - GameRules.WIN_MIN
            }
        }

        player1Pos.observeForever {
            player1 = setPlayerType(it)
        }
        player2Pos.observeForever {
            player2 = setPlayerType(it)
        }
    }

    fun setupGameType(gameType: GameType) {
        this.gameType = gameType
    }

    fun setupPlayers() {
        player1 = PlayerType.Human
        player1Pos.value = getSpinPosition(player1)
        player2 = when (gameType) {
            GameType.Local -> PlayerType.Bot
            GameType.Bluetooth -> PlayerType.Bluetooth
            GameType.Network -> PlayerType.Network
        }
        player2Pos.value = getSpinPosition(player2)
    }

    fun sizeSetup() {
        rows.apply { value = GameRules.ROWS_MIN }
        cols.apply { value = GameRules.COLS_MIN }
        win.apply { value = GameRules.WIN_MIN }
        winMax.apply { value = 0 }
    }

    fun getGameType(): GameType = gameType

    fun gameConfigured(): Boolean {
        var locPlayersCount = 0
        for (pl in arrayOf(player1, player2)) {
            when (pl) {
                PlayerType.Human, PlayerType.Bot -> locPlayersCount++
                else -> {
                }
            }
        }
        return when (gameType) {
            GameType.Local -> locPlayersCount == 2
            GameType.Bluetooth -> locPlayersCount == 1 && (player1 == PlayerType.Bluetooth || player2 == PlayerType.Bluetooth)
            GameType.Network -> locPlayersCount == 1 && (player1 == PlayerType.Network || player2 == PlayerType.Network)
        }
    }

    private fun getSpinPosition(playerType: PlayerType): Int {
        return when (playerType) {
            PlayerType.Bot -> 0
            PlayerType.Human -> 1
            PlayerType.Bluetooth, PlayerType.Network -> 2
        }
    }

    private fun setPlayerType(position: Int): PlayerType {
        return when (position) {
            0 -> PlayerType.Bot
            1 -> PlayerType.Human
            2 -> if (gameType == GameType.Bluetooth) {
                PlayerType.Bluetooth
            } else {
                PlayerType.Network
            }
            else -> throw IllegalArgumentException("accept only digits in 0..2")
        }
    }
}