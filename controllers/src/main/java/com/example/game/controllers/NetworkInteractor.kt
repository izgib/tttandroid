package com.example.game.controllers

import com.example.game.domain.game.Mark
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface NetworkInteractor {
    fun CreateGame(): GameInitializer

    fun GameList(rowRange: ParamRange, colRange: ParamRange, winRange: ParamRange, markF: Byte): Flow<GameItem>

    fun JoinGame(gameID: Short): ReceiveChannel<GameCreationStatus>

    fun getGameClientWrapper(): NetworkClient
}

sealed class GameCreationStatus
data class GameID(val ID: Short) : GameCreationStatus()
object CreationFailure : GameCreationStatus()
object Created : GameCreationStatus()


class GameItem(val ID: Short, val rows: Int, val cols: Int, val win: Int, val mark: Mark)