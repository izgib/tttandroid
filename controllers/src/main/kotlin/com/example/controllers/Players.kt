package com.example.controllers

import com.example.controllers.models.GameModel
import com.example.game.AIPlayer
import com.example.game.Coord
import com.example.game.GameController
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.selects.select

interface LocalPlayer {
    suspend fun getMove(): Coord?
}

interface CanGiveUp {
    fun giveUp()
}

interface PlayerInitializer {
    fun setup(model: GameModel): LocalPlayer
}

class BotPlayer(model: GameModel) : LocalPlayer {
    val player = AIPlayer(model.controller)
    override suspend fun getMove(): Coord {
        return player.getMove()
    }
}


class MoveRegister(model: GameModel) {
    private val controller: GameController
    init {
        controller = model.controller
    }
    internal val _listenerState = MutableStateFlow(false)
    internal val scope: CoroutineScope = model.scope
    val listenerState: StateFlow<Boolean> = _listenerState

    internal val moveChannel = Channel<Coord?>()

    fun sendMove(move: Coord) {
        if (controller.isValidMove(move)) {
            _listenerState.value = false
            moveChannel.trySendBlocking(move)
        }
    }

    fun giveUp() {
        check(listenerState.value)
        _listenerState.value = false
        moveChannel.trySendBlocking(null)
    }
}

class HumanPlayer(private val register: MoveRegister): LocalPlayer {
    override suspend fun getMove(): Coord? {
        register._listenerState.value = true
        //register.scope
        println("getting move")
        return register.moveChannel.receive()
    }
}

/*fun human(register: MoveRegister): PlayerInitializer {
    return object : PlayerInitializer {
        override fun setup(model: GameModel): LocalPlayer {
            return HumanPlayer(register)
        }
    }
}*/

/*class ClickRegister private constructor(private val model: GameModel) : LocalPlayer {
    private val _listenerState = MutableStateFlow(false)
    val listenerState: Flow<Boolean> = _listenerState
    val moveChannel = Channel<Coord>()

    override suspend fun getMove(): Coord {
        println("getting move for clicker")
        _listenerState.value = true
        while (true) {
            val move = moveChannel.receive()
            if (model.controller.isValidMove(move)) {
                _listenerState.value = false
                return move
            }
        }
    }

    fun giveUp() = model.giveUp()

    companion object : PlayerInitializer {
        override fun setup(model: GameModel): ClickRegister {
            return ClickRegister(model)
        }
    }

}*/

