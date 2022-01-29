package com.example.controllers

import com.example.game.AIPlayer
import com.example.game.Coord
import com.example.game.GameController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface LocalPlayer {
    suspend fun getMove(): Coord
}

class BotPlayer(controller: GameController) : LocalPlayer {
    val player = AIPlayer(controller)
    override suspend fun getMove(): Coord {
        delay(200)
        return player.getMove()
    }
}

class ClickRegister(private val validationFunc: (Coord) -> Boolean) : LocalPlayer {
    private val _listenerState = MutableStateFlow(false)
    val listenerState: Flow<Boolean> = _listenerState
    val moveChannel = Channel<Coord>()


    override suspend fun getMove(): Coord {
        println("getting move for clicker")
        _listenerState.value = true
        while (true) {
            val move = moveChannel.receive()
            if (validationFunc(move)) {
                _listenerState.value = false
                return move
            }
        }
    }
}

