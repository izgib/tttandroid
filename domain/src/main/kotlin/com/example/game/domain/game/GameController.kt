package com.example.game.domain.game


abstract class GameController(protected val rows: Int, protected val cols: Int, protected val win: Int) {
    protected val gameField: Array<Array<Mark>> = Array(rows) { Array(cols) { Mark.Empty } }
    protected var turn = 0

    fun getEmptyCells(): Sequence<Coord> = sequence {
        for ((i, row) in gameField.withIndex()) {
            for ((j, item) in row.withIndex()) {
                if (item == Mark.Empty) {
                    yield(Coord(i, j))
                }
            }
        }
    }

    fun curPlayer(): Int = turn and 1
    fun isValidMove(move: Coord): Boolean = gameField[move.i][move.j] == Mark.Empty
}