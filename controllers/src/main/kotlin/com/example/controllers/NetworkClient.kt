package com.example.controllers

import com.example.controllers.models.Response
import com.example.game.Coord

//all suspend must throw InterruptionException on error
interface NetworkClient {
    suspend fun getResponse(): Response
    suspend fun sendMove(move: Coord)
    suspend fun sendAction(action: PlayerAction)
}

enum class PlayerAction {
    Leave, GiveUp
}

/*
sealed class PlayerAction()
object Leave: PlayerAction()
object GiveUp: PlayerAction()*/
