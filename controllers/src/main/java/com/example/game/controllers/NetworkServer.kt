package com.example.game.controllers

import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameState

interface NetworkServer {
    suspend fun getMove(): Result<Coord, Interruption>
    suspend fun sendMove(move: Coord): Interruption?
    suspend fun sendState(state: GameState): Interruption?
    suspend fun sendInterruption(interruption: Interruption): Interruption?
}