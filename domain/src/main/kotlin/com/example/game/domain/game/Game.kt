package com.example.game.domain.game

import kotlin.math.min

class Game(rows: Int, cols: Int, win: Int) : GameController(rows, cols, win) {
    fun gameState(move: Coord): GameState {
        val winLine = gameWon(marks[curPlayer()], move)
        if (winLine != null) {
            return Win(winLine)
        }
        if (cols * rows - 1 <= turn) {
            return Tie
        }
        turn++
        return Continues
    }

    internal fun gameWon(mark: Mark, move: Coord): EndWinLine? {
        val leftStep = min(move.col, win - 1)
        val rightStep = min(cols - 1 - move.col, win - 1)
        val topStep = min(move.row, win - 1)
        val bottomStep = min(rows - 1 - move.row, win - 1)

        val lines: Array<LineParams> = arrayOf(
                // horizontal
                LineParams(
                        Coord(move.row, move.col - leftStep), 0, 1, rightStep + 1 + leftStep),
                // vertical
                LineParams(Coord(move.row - topStep, move.col), 1, 0, topStep + 1 + bottomStep),
                // main diagonal
                LineParams(
                        Coord(move.row - min(leftStep, topStep), move.col - min(leftStep, topStep)),
                        1,
                        1,
                        min(leftStep, topStep) + 1 + min(rightStep, bottomStep)),
                // anti diagonal
                LineParams(Coord(move.row + min(leftStep, bottomStep), move.col - min(leftStep, bottomStep)),
                        -1,
                        1,
                        min(leftStep, bottomStep) + 1 + min(rightStep, topStep))
        )

        lines.forEach { line ->
            if (line.length >= win) {
                var l = 0
                var start: Coord? = null
                for (coord in line.iterator()) {
                    if (gameField[coord.row][coord.col] == mark) {
                        if (start == null) {
                            start = coord
                        }
                        l++
                        if (l >= win) {
                            return EndWinLine(mark, start, coord)
                        }
                    } else {
                        l = 0
                        start = null
                    }
                }
            }
        }
        return null
    }
}

interface ICoord {
    val row: Int
    val col: Int
}

data class Coord(override val row: Int, override val col: Int) : ICoord
data class MarkLists(val crosses: List<Coord>, val noughts: List<Coord>)
enum class Mark(val mark: Byte) {
    Cross(0),
    Nought(1),
    Empty(2)
}

operator fun Mark.not() = when (this) {
    Mark.Cross -> Mark.Nought
    Mark.Nought -> Mark.Cross
    Mark.Empty -> Mark.Empty
}

sealed class GameState
object Continues : GameState()
object Tie : GameState()
data class Win(val line: EndWinLine) : GameState()


data class LineParams(val start: Coord, val i_step: Int, var j_step: Int, var length: Int)

internal fun LineParams.iterator(): Iterator<Coord> = iterator {
    var i = start.row
    var j = start.col
    repeat(length) {
        yield(Coord(i, j))
        i += i_step
        j += j_step
    }
}

class GameRules {
    companion object {
        const val ROWS_MIN = 3
        const val ROWS_MAX = 15

        const val COLS_MIN = 3
        const val COLS_MAX = 15

        const val WIN_MIN = 3
        const val WIN_MAX = 8
    }
}

data class EndWinLine(val mark: Mark, val start: ICoord, val end: ICoord)