package com.example.game.controllers

import com.example.game.domain.game.Mark
import kotlinx.coroutines.channels.ReceiveChannel

interface GameInitializer {
    fun sendCreationRequest(rows: Int, cols: Int, win: Int, mark: Mark): ReceiveChannel<GameCreationStatus>
    fun CancelGame()
}