package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.GameSettings
import com.example.controllers.models.GameType
import com.example.controllers.models.PlayerType
import com.example.game.GameRules
import com.example.game.Mark
import com.zhuinden.simplestack.Bundleable
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.statebundle.StateBundle

class GameConfig(private val type: TypeStorage) : Bundleable, ScopedServices.Activated {
    var rows = GameRules.ROWS_MIN
    var cols = GameRules.COLS_MIN
    var win = GameRules.WIN_MIN
    var player1: PlayerType = PlayerType.Human
    var player2: PlayerType = PlayerType.Human
    val gameType: GameType
        get() = type.gameType

    private fun setupPlayers() {
        player1 = PlayerType.Human
        player2 = when (type.gameType) {
            GameType.Local -> PlayerType.Bot
            GameType.BluetoothClassic, GameType.BluetoothLE -> PlayerType.Bluetooth
            GameType.Network -> PlayerType.Network
        }
    }

    fun reshuffle() {
        player1 = player2.also { player2 = player1 }
    }

    fun gameConfigured(): Boolean {
        var locPlayersCount = 0
        for (pl in arrayOf(player1, player2)) {
            when (pl) {
                PlayerType.Human, PlayerType.Bot -> locPlayersCount++
                else -> {
                }
            }
        }
        return when (type.gameType) {
            GameType.Local -> locPlayersCount == 2
            GameType.BluetoothClassic, GameType.BluetoothLE -> locPlayersCount == 1 && (player1 == PlayerType.Bluetooth || player2 == PlayerType.Bluetooth)
            GameType.Network -> locPlayersCount == 1 && (player1 == PlayerType.Network || player2 == PlayerType.Network)
        }
    }

    override fun toBundle(): StateBundle = StateBundle().apply {
        putByte("rows", rows.toByte())
        putByte("cols", cols.toByte())
        putByte("win", win.toByte())
        putByte("playerX", player1.ordinal.toByte())
        putByte("playerO", player2.ordinal.toByte())
    }

    override fun fromBundle(bundle: StateBundle?) {
        bundle?.run {
            rows = getByte("rows").toInt()
            cols = getByte("cols").toInt()
            win = getByte("win").toInt()
            player1 = PlayerType.values()[getByte("playerX").toInt()]
            player2 = PlayerType.values()[getByte("playerO").toInt()]
        }
    }

    override fun onServiceInactive() = Unit

    override fun onServiceActive() = setupPlayers()
}

// Change CameConfig to fit for RemotePlayer settings
fun GameConfig.fromSettings(settings: GameSettings) {
    rows = settings.rows
    cols = settings.cols
    win = settings.win
    val remotePlayer = when (gameType) {
        GameType.BluetoothClassic, GameType.BluetoothLE -> PlayerType.Bluetooth
        GameType.Network -> PlayerType.Network
        else -> throw IllegalStateException()
    }
    val (pl1, pl2) = if (settings.creatorMark == Mark.Cross) {
        Pair(remotePlayer, PlayerType.Human)
    } else {
        Pair(PlayerType.Human, remotePlayer)
    }
    player1 = pl1
    player2 = pl2
}

fun GameConfig.toSettings(): GameSettings {
    return GameSettings(rows, cols, win, when (player1) {
        PlayerType.Human, PlayerType.Bot -> Mark.Cross
        else -> Mark.Nought
    })
}