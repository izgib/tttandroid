package com.example.game.controllers

import android.bluetooth.BluetoothDevice
import com.example.game.domain.game.Mark
import kotlinx.coroutines.channels.ReceiveChannel

interface BluetoothInteractor {
    fun CreateGame(rows: Short, cols: Short, win: Short, mark: Byte): ReceiveChannel<GameInitStatus>

    fun JoinGame(device: BluetoothDevice): ReceiveChannel<GameInitStatus>

    fun serverWrapper(): NetworkServer

    fun clientWrapper(): NetworkClient
}

sealed class GameInitStatus {
    object Awaiting : GameInitStatus()
    object Failure : GameInitStatus()
    object Connected : GameInitStatus()
    data class OppConnected(val gameSettings: GameSettings) : GameInitStatus()
}

data class GameSettings(val rows: Int, val cols: Int, val win: Int, val mark: Mark)