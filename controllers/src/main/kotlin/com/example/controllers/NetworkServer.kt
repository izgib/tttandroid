package com.example.controllers

import com.example.controllers.models.InterruptCause
import com.example.game.Coord
import com.example.game.GameState

interface NetworkServer {
    //suspend fun sendMove(move: Coord)
    //suspend fun sendState(state: GameState)
    //suspend fun getMove(): Coord
    suspend fun sendInterruption(cause: InterruptCause)
    suspend fun sendTurn(move: Coord? = null, state: GameState)
    suspend fun getResponse(): ClientResponse
}

sealed class ClientResponse()
class ClientMove(val move: Coord) : ClientResponse()
class ClientAction(val action: PlayerAction) : ClientResponse()
