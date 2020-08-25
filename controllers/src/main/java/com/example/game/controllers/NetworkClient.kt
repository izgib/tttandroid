package com.example.game.controllers

import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameState

//all suspend must throw InterruptionException on error
interface NetworkClient {
    suspend fun getMove(): Coord
    suspend fun sendMove(move: Coord)
    suspend fun getState(): GameState
    fun cancelGame()
}