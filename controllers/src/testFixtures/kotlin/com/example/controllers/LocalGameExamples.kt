package com.example.controllers

import com.example.game.*
import com.example.controllers.models.EndState
import com.example.controllers.models.GameSignal


class LocalTestExample(val rows: Int, val cols: Int, val win: Int, val moves: List<Coord>, val endSignal: GameSignal, val action: PlayerAction? = null)

val winX = LocalTestExample(3, 3, 3, listOf(
        Coord(1, 1), Coord(1, 2), Coord(0, 0), Coord(2, 2),
        Coord(0, 2), Coord(2, 1), Coord(0, 1)),
        EndState(Win(EndWinLine(Mark.Cross, Coord(0, 0), Coord(0, 2))))
)

val tie = LocalTestExample(3, 3, 3, listOf(
        Coord(1, 1), Coord(0, 0), Coord(2, 0), Coord(0, 2),
        Coord(0, 1), Coord(2, 1), Coord(1, 2), Coord(1, 0),
        Coord(2, 2)), EndState(Tie)
)

val winO = LocalTestExample(3, 3, 3, listOf(
        Coord(1, 2), Coord(1, 1), Coord(1, 0), Coord(0, 0),
        Coord(2, 2), Coord(0, 2), Coord(2, 0), Coord(0, 1)),
        EndState(Win(EndWinLine(Mark.Nought, Coord(0, 0), Coord(0, 2))))
)

val gaveUpX = LocalTestExample(3, 3, 3, listOf(
        Coord(1, 0), Coord(1, 1), Coord(1, 2), Coord(0, 2),
        Coord(2, 0), Coord(0, 0)),
        EndState(Win(EndWinLine(Mark.Nought, null, null))),
        PlayerAction.GiveUp,
)

val gaveUpO = LocalTestExample(3, 3, 3, listOf(
        Coord(1, 1), Coord(1, 2), Coord(0, 2), Coord(2, 0),
        Coord(0, 0)),
        EndState(Win(EndWinLine(Mark.Cross, null, null))),
        PlayerAction.GiveUp,
)