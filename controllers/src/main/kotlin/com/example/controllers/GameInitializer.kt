package com.example.controllers

import kotlinx.coroutines.flow.Flow

interface GameInitializer {
    fun sendCreationRequest(settings: GameSettings): Flow<GameCreationStatus>
    fun cancelGame()
}