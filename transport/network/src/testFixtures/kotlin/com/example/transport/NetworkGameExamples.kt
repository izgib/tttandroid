package com.example.transport

import com.example.controllers.LocalTestExample
import com.example.controllers.PlayerAction
import com.example.controllers.models.GameInterruption
import com.example.controllers.models.GameSignal
import com.example.controllers.models.InterruptCause
import com.example.game.Coord
import com.example.game.Mark

data class NetworkTestExample(
    val rows: Int,
    val cols: Int,
    val win: Int,
    val moves: List<Coord>,
    val endSignal: GameSignal,
    val playerMark: Mark,
    val action: PlayerAction? = null
)

fun LocalTestExample.toNetworkTest(playerMark: Mark): NetworkTestExample {
    if (playerMark == Mark.Empty) throw IllegalArgumentException("must be cross or nought")
    return NetworkTestExample(rows, cols, win, moves, endSignal, playerMark, action)
}

val cheatPlayerXTemplate = LocalTestExample(
    3,
    3,
    3,
    listOf(Coord(1, 1), Coord(0, 0), Coord(1, 1)),
    GameInterruption(InterruptCause.InvalidMove),
)

val cheatPlayerOTemplate = LocalTestExample(
    3,
    3,
    3,
    listOf(Coord(1, 1), Coord(1, 1)),
    GameInterruption(InterruptCause.InvalidMove),
)

val gameCancelXTemplate = LocalTestExample(
    3,
    3,
    3,
    listOf(Coord(1, 1), Coord(0, 0)),
    GameInterruption(InterruptCause.Leave),
    PlayerAction.Leave
)

val gameCancelOTemplate = LocalTestExample(
    3,
    3,
    3,
    listOf(Coord(1, 1)),
    GameInterruption(InterruptCause.Leave),
    PlayerAction.Leave
)

