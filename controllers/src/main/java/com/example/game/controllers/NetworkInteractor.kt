package com.example.game.controllers

import com.example.game.controllers.models.Range
import com.example.game.domain.game.Mark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface NetworkInteractor {
    fun CreateGame(scope: CoroutineScope): GameInitializer

    fun GameList(rowRange: Range, colRange: Range, winRange: Range, mark: Mark): Flow<GameItem>

    fun JoinGame(scope: CoroutineScope, gameID: Short): Flow<GameCreationStatus>

    fun getGameClientWrapper(): NetworkClient
}

sealed class GameCreationStatus
data class GameID(val ID: Short) : GameCreationStatus()
object CreationFailure : GameCreationStatus()
data class Created(val client: NetworkClient) : GameCreationStatus()

data class GameItem(val ID: Short, val settings: GameSettings)