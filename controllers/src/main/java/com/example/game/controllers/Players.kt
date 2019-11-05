package com.example.game.controllers

import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameController
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

interface LocalPlayer {
    suspend fun getMove(): Coord
}

class BotPlayer(private val controller: GameController) : LocalPlayer {
    override suspend fun getMove(): Coord {
        val seq = controller.getEmptyCells()
        val len = seq.count()
        delay(200)
        return seq.elementAt(Random.nextInt(len))
    }
}

class ClickRegister(private val validationFunc: (Coord) -> Boolean) : LocalPlayer {
    //private val _requestObserver = ConsumeLiveEvent<Unit>()
    val requestObserver = Channel<Unit>()
    //override val requestObserver: LiveData<Unit> = _requestObserver
    private val moveChannel = Channel<Coord>()

    fun moveTo(i: Int, j: Int): Boolean {
        val move = Coord(i, j)
        if (validationFunc(move)) {
            GlobalScope.launch {
                moveChannel.send(move)
            }
            return true
        }
        return false
    }

    override suspend fun getMove(): Coord {
        requestObserver.send(Unit)
        return moveChannel.receive()
    }
}

