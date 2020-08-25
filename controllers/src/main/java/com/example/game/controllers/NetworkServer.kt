package com.example.game.controllers

import com.example.game.controllers.models.Interruption
import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameState

interface NetworkServer {
    suspend fun getMove(): Coord
    suspend fun sendMove(move: Coord)
    suspend fun sendState(state: GameState)
    suspend fun sendInterruption(interruption: Interruption)
}