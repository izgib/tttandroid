package com.example.game


open class GameController(internal val rows: Int, internal val cols: Int, internal val win: Int) {
    internal val marks: Array<Mark> = arrayOf(Mark.Cross, Mark.Nought)
    internal val gameField: Array<Array<Mark>> = Array(rows) { Array(cols) { Mark.Empty } }
    var turn = 0

    fun moveTo(move: Coord) {
        gameField[move.row][move.col] = marks[curPlayer()]
    }

    fun curPlayer(): Int = turn and 1
    fun otherPlayer(): Int = turn + 1 and 1

    fun isValidMove(move: Coord): Boolean = gameField[move.row][move.col] == Mark.Empty

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

    fun clearField() {
        gameField.forEachIndexed { i, row ->
            row.forEachIndexed { j, _ -> gameField[i][j] = Mark.Empty }
        }
        turn = 0
    }
}