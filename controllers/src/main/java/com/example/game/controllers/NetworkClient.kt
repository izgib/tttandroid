package com.example.game.controllers

import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameState

interface NetworkClient {
    suspend fun getMove(): Result<Coord, Interruption>
    suspend fun sendMove(move: Coord): Interruption?
    suspend fun getState(): Result<GameState, Interruption>
    fun CancelGame()
}