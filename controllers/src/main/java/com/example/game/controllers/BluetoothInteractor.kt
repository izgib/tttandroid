package com.example.game.controllers

import android.bluetooth.BluetoothDevice
import com.example.game.domain.game.Mark
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.*

interface BluetoothInteractor {
    fun createGame(settings: GameSettings): ReceiveChannel<GameInitStatus>

    fun joinGame(device: BluetoothDevice): ReceiveChannel<GameInitStatus>

    fun serverWrapper(): NetworkServer

    fun clientWrapper(): NetworkClient

    companion object {
        val MY_UUID = UUID.fromString("e67682c9-268c-4de8-ad82-44822952c5ee")!!
    }
}

sealed class GameInitStatus {
    object Awaiting : GameInitStatus()
    object Failure : GameInitStatus()
    data class Connected(val server: NetworkServer) : GameInitStatus()
    data class OppConnected(val gameSettings: GameSettings, val client: NetworkClient) : GameInitStatus()
}

data class GameSettings(val rows: Int, val cols: Int, val win: Int, val creatorMark: Mark)