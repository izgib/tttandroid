package com.example.controllers

import com.example.game.Coord
import com.example.game.GameState

//all suspend must throw InterruptionException on error
interface NetworkClient {
    suspend fun getMove(): Coord
    suspend fun sendMove(move: Coord)
    suspend fun getState(): GameState
    fun cancelGame()
}