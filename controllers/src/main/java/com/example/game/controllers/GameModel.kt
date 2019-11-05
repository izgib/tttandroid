package com.example.game.controllers

import com.example.game.domain.game.Coord
import com.example.game.domain.game.GameState
import com.example.game.domain.game.MarkLists
import kotlinx.coroutines.channels.Channel
import java.io.Serializable

enum class PlayerType {
    Bot, Human, Bluetooth, Network
}


interface GameModel { //LifecycleOwner {
    val crossObserver: Channel<Coord>
    val noughtObserver: Channel<Coord>
    val endedObserver: Channel<GameState>
    val interruptObserver: Channel<Interruption>
    val clickRegister: ClickRegister

    fun initPlayers(player1: PlayerType, player2: PlayerType)

    fun start()
    fun reload(): MarkLists
    fun cancel()
}

enum class GameType {
    Local, Bluetooth, Network
}

data class GameParamsData(val rows: Int, val cols: Int, val win: Int, val player1: PlayerType, val player2: PlayerType)
data class ParamRange(val start: Short, val end: Short) : Serializable
