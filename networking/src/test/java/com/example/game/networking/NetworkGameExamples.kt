package com.example.game.networking

import com.example.game.controllers.LocalTestExample
import com.example.game.controllers.models.GameSignal
import com.example.game.domain.game.Coord
import com.example.game.domain.game.Mark

data class NetworkTestExample(val rows: Int, val cols: Int, val win: Int, val moves: Array<Coord>, val endSignal: GameSignal, val localPlayerMark: Mark)

fun LocalTestExample.toNetworkTest(localPlayerMark: Mark): NetworkTestExample {
    if (localPlayerMark == Mark.Empty) throw IllegalArgumentException("must be cross or nought")
    return NetworkTestExample(rows, cols, win, moves, endSignal, localPlayerMark)
}