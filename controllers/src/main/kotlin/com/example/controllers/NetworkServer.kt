package com.example.controllers

import com.example.controllers.models.Interruption
import com.example.game.Coord
import com.example.game.GameState

interface NetworkServer {
    suspend fun getMove(): Coord
    suspend fun sendMove(move: Coord)
    suspend fun sendState(state: GameState)
    suspend fun sendInterruption(interruption: Interruption)
    fun cancelGame()
}