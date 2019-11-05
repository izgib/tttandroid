package com.example.game.domain.game

import kotlin.math.min

class Game(rows: Int, cols: Int, win: Int) : GameController(rows, cols, win) {
    private val marks: Array<Mark> = arrayOf(Mark.Cross, Mark.Nought)

    fun moveTo(move: Coord) {
        gameField[move.i][move.j] = marks[curPlayer()]
    }

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

    private fun lineIterator(start: Coord, i_step: Int, j_step: Int, length: Int): Iterator<Coord> = iterator {
        var i = start.i
        var j = start.j
        repeat(length) {
            yield(Coord(i, j))
            i += i_step
            j += j_step
        }
    }

    fun getMarks(): MarkLists {
        val crosses = ArrayList<Coord>(turn / 2 + curPlayer())
        val noughts = ArrayList<Coord>(turn / 2)
        gameField.forEachIndexed { rowInd, row ->
            row.forEachIndexed { colInd, mark ->
                when (mark) {
                    Mark.Cross -> crosses.add(Coord(rowInd, colInd))
                    Mark.Nought -> noughts.add(Coord(rowInd, colInd))
                    Mark.Empty -> {
                    }
                }
            }
        }
        return MarkLists(crosses, noughts)
    }

    private fun gameWon(mark: Mark, move: Coord): EndWinLine? {
        val leftStep = min(move.j, win - 1)
        val rightStep = min(cols - 1 - move.j, win - 1)
        val topStep = min(move.i, win - 1)
        val bottomStep = min(rows - 1 - move.i, win - 1)

        val lines: Array<LineIteratorParams> = arrayOf(
                // horizontal
                LineIteratorParams(
                        Coord(move.i, move.j - leftStep), 0, 1, rightStep + 1 + leftStep),
                // vertical
                LineIteratorParams(Coord(move.i - topStep, move.j), 1, 0, topStep + 1 + bottomStep),
                // main diagonal
                LineIteratorParams(
                        Coord(move.i - min(leftStep, topStep), move.j - min(leftStep, topStep)),
                        1,
                        1,
                        min(leftStep, topStep) + 1 + min(rightStep, bottomStep)),
                // anti diagonal
                LineIteratorParams(Coord(move.i + min(leftStep, bottomStep), move.j - min(leftStep, bottomStep)),
                        -1,
                        1,
                        min(leftStep, bottomStep) + 1 + min(rightStep, topStep))
        )

        lines.forEach {
            if (it.length >= win) {
                var l = 0
                var start: Coord? = null
                for (coord in lineIterator(it.start, it.i_step, it.j_step, it.length)) {
                    if (gameField[coord.i][coord.j] == mark) {
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
    val i: Int
    val j: Int
}

data class Coord(override val i: Int, override val j: Int) : ICoord
data class MarkLists(val crosses: List<Coord>, val noughts: List<Coord>)
enum class Mark(val mark: Byte) {
    Cross(0),
    Nought(1),
    Empty(2)
}

sealed class GameState
object Continues : GameState()
object Tie : GameState()
data class Win(val line: EndWinLine) : GameState()


data class LineIteratorParams(val start: Coord, var i_step: Int, var j_step: Int, var length: Int)

class GameRules {
    companion object {
        const val ROWS_MIN = 3
        const val ROWS_MAX = 15

        const val COLS_MIN = 3
        const val COLS_MAX = 15

        const val WIN_MIN = 3
        const val WIN_MAX = 7
    }
}

data class EndWinLine(val mark: Mark, val start: ICoord, val end: ICoord)