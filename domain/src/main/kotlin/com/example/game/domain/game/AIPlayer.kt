package com.example.game.domain.game

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

typealias PotentialMap = Array<IntArray>

infix fun Int.pow(power: Int): Int {
    if (this < 0) throw IndexOutOfBoundsException("$this is less than zero.")
    if (power == 0) return 1
    var res = this
    var i = power - 1
    while (i > 0) {
        res *= this
        i--
    }
    return res
}


enum class LineStatus {
    Empty, OwnControl, OpponentControl, Contested
}


class Line {
    var st = LineStatus.Empty
        private set
    var power: Int = 0
        private set

    fun occupy() {
        if (st == LineStatus.Contested) {
            return
        }
        if (st == LineStatus.OpponentControl) {
            st = LineStatus.Contested
            power = 0
            return
        }
        st = LineStatus.OwnControl
        power++
    }

    fun forfeit() {
        if (st == LineStatus.Contested) {
            return
        }
        if (st == LineStatus.OwnControl) {
            st = LineStatus.Contested
            power = 0
            return
        }
        st = LineStatus.OpponentControl
        power++
    }
}

data class Cell(val horizon: Array<Line>, val vertical: Array<Line>, val mainDiagonal: Array<Line>, val antiDiagonal: Array<Line>) {
    fun forfeit() {
        antiDiagonal.forEach { it.forfeit() }
        horizon.forEach { it.forfeit() }
        mainDiagonal.forEach { it.forfeit() }
        vertical.forEach { it.forfeit() }
    }

    fun occupy() {
        antiDiagonal.forEach { it.occupy() }
        horizon.forEach { it.occupy() }
        mainDiagonal.forEach { it.occupy() }
        vertical.forEach { it.occupy() }
    }
}

data class Potential(val own: Int, val opponent: Int)

class AIPlayer(private val controller: GameController) {
    internal val usedCap = controller.win * (9 pow 8)
    private var savedGameField: Array<Array<Mark>> = Array(controller.rows) { i -> controller.gameField[i].copyOf() }
    private val field = Array(controller.rows) { arrayOfNulls<Cell>(controller.cols) }
    internal val potentialMap: PotentialMap
    internal val oppPotentialMap: PotentialMap

    init {
        val empty: Array<Line> = emptyArray()

        val vertical = Array(min(controller.rows - (controller.win - 1), controller.win)) { Array(controller.cols) { Line() } }

        val rowDiagCount = controller.cols - (controller.win - 1)
        val mainDiagonal = Array(vertical.size) { Array(rowDiagCount) { Line() } }
        val antiDiagonal = Array(vertical.size) { Array(rowDiagCount) { Line() } }
        val mainRanges = Array(controller.win) { IntRange(it, controller.cols - (controller.win - it)) }
        val antiRanges = Array(controller.win) { IntRange((controller.win - 1) - it, (controller.cols - 1) - it) }

        var vertSize = 0
        var rangeStart = 0
        for (row in 0 until controller.rows) {
            val mainDiag = ArrayList<Line>(controller.win)
            val antiDiag = ArrayList<Line>(controller.win)

            val vertStart: Int
            when {
                row < vertical.size -> {
                    vertStart = 0
                    vertSize++
                }
                row > controller.rows - controller.win -> {
                    if (vertSize > controller.rows - row) {
                        vertSize--
                    }
                    rangeStart++
                    vertStart = vertical.size - vertSize
                }
                else -> {
                    vertStart = 0
                    for (j in 1 until vertical.size) {
                        vertical[j - 1] = vertical[j]
                        mainDiagonal[j - 1] = mainDiagonal[j]
                        antiDiagonal[j - 1] = antiDiagonal[j]
                    }
                    vertical[vertical.size - 1] = Array(controller.cols) { Line() }
                    mainDiagonal[mainDiagonal.size - 1] = Array(rowDiagCount) { Line() }
                    antiDiagonal[antiDiagonal.size - 1] = Array(rowDiagCount) { Line() }
                }
            }

            val horizon = Array(min(rowDiagCount, controller.win)) { Line() }
            var horSize = 0
            for (col in 0 until controller.cols) {
                val hor: Array<Line>
                val vert = Array(vertSize) { index -> vertical[vertStart + index][col] }
                when {
                    col < horizon.size -> {
                        horSize++
                        hor = horizon.copyOf(horSize).requireNoNulls()
                    }
                    col > controller.cols - controller.win -> {
                        if (horSize > controller.cols - col) {
                            horSize--
                        }
                        hor = horizon.copyOfRange(horizon.size - horSize, horizon.size)
                    }
                    else -> {
                        for (k in 1 until horizon.size) {
                            horizon[k - 1] = horizon[k]
                        }
                        horizon[horizon.size - 1] = Line()
                        hor = horizon.copyOf()
                    }
                }
                for (i in 0 until vertSize) {
                    val rangeIndex = (rangeStart + vertSize - 1) - i
                    val main = mainRanges[rangeIndex]
                    val anti = antiRanges[rangeIndex]
                    if (col in main) {
                        mainDiag.add(mainDiagonal[vertStart + i][col - main.first])
                    }
                    if (col in anti) {
                        antiDiag.add(antiDiagonal[vertStart + i][col - anti.first])
                    }
                }

                val md = if (mainDiag.count() > 0) {
                    val _md = mainDiag.toTypedArray()
                    mainDiag.clear()
                    _md
                } else {
                    empty
                }
                val ad = if (antiDiag.count() > 0) {
                    val _ad = antiDiag.toTypedArray()
                    antiDiag.clear()
                    _ad
                } else {
                    empty
                }

                field[row][col] = Cell(hor, vert, md, ad)
            }
        }

        potentialMap = Array(controller.rows) { row -> IntArray(controller.cols) { col -> potentialValue(row, col) } }
        oppPotentialMap = Array(controller.rows) { row -> IntArray(controller.cols) { col -> potentialMap[row][col] } }
    }

    fun getMove(): Coord {
        findMove()?.let {
            sendMove(it)
            savedGameField[it.row][it.col] = controller.marks[controller.otherPlayer()]
        }

        val ownPotential = getMaxPower(potentialMap)

        val maxValues: ArrayList<Coord>
        if (ownPotential >= controller.win - 1) {
            var max = -1
            maxValues = ArrayList(8)
            for ((rowInd, row) in potentialMap.withIndex()) {
                for ((colInd, cell) in row.withIndex()) {
                    if (cell == -1 || cell == usedCap) {
                        continue
                    }
                    if (cell >= max) {
                        if (cell > max) {
                            maxValues.clear()
                            max = cell
                        }
                        maxValues.add(Coord(rowInd, colInd))
                    }
                }
            }
        } else {
            var max = -1
            maxValues = ArrayList(8)
            for ((rowInd, row) in potentialMap.withIndex()) {
                for ((colInd, cell) in row.withIndex()) {
                    if (cell == -1 || cell == usedCap) {
                        continue
                    }
                    val value = max(cell, oppPotentialMap[rowInd][colInd])
                    if (value > max) {
                        maxValues.clear()
                        maxValues.add(Coord(rowInd, colInd))
                        max = value
                    }
                }
            }
        }

        val move = with(maxValues) {
            get(Random.nextInt(count()))
        }

        goTo(move)

        return move
    }

    internal fun goTo(move: Coord) {
        potentialMap[move.row][move.col] = usedCap
        oppPotentialMap[move.row][move.col] = -1
        savedGameField[move.row][move.col] = controller.marks[controller.curPlayer()]
        field[move.row][move.col]!!.occupy()
        recalculatePotential(move)
    }

    private fun getMaxPower(field: Array<IntArray>): Int {
        var max = 0
        for (row in field) {
            for (cell in row) {
                if (cell == -1 || cell == usedCap) {
                    continue
                }
                if (cell < controller.win * (9 pow max + 1)) {
                    continue
                }
                do {
                    max++
                } while (cell >= controller.win * (9 pow max + 1))
            }
        }
        return max
    }

    private fun findMove(): Coord? {
        for ((i, row) in controller.gameField.withIndex()) {
            for ((j, mark) in row.withIndex()) {
                if (mark != savedGameField[i][j]) {
                    return Coord(i, j)
                }
            }
        }
        return null
    }

    private fun potentialValues(row: Int, col: Int): Potential {
        var ownValue = 0
        var opponentValue = 0
        with(field[row][col]!!) {
            for (direction in arrayOf(antiDiagonal, horizon, mainDiagonal, vertical)) {
                var ownCount = 0
                var opponentCount = 0
                var ownFirstPower = 0
                var ownSecondPower = 0
                var opponentFirstPower = 0
                var opponentSecondPower = 0
                loop@
                for (l in direction) {
                    when (l.st) {
                        LineStatus.Contested -> continue@loop
                        LineStatus.OwnControl -> {
                            ownCount++
                            if (l.power >= ownFirstPower && l.power > ownSecondPower) {
                                ownSecondPower = ownFirstPower
                                ownFirstPower = l.power
                            }
                        }
                        LineStatus.OpponentControl -> {
                            opponentCount++
                            if (l.power >= opponentFirstPower && l.power > opponentSecondPower) {
                                opponentSecondPower = opponentFirstPower
                                opponentFirstPower = l.power
                            }
                        }
                        LineStatus.Empty -> {
                            ownCount++
                            opponentCount++
                        }
                    }
                }
                ownValue += when (ownCount) {
                    0 -> 0
                    controller.win -> controller.win * (9 pow ownFirstPower) + controller.win * (9 pow ownSecondPower)
                    else -> controller.win * (9 pow ownFirstPower) + ownCount - 1
                }
                opponentValue += when (opponentCount) {
                    0 -> 0
                    controller.win -> controller.win * (9 pow opponentFirstPower) + controller.win * (9 pow opponentSecondPower)
                    else -> controller.win * (9 pow opponentFirstPower) + opponentCount - 1
                }
            }
            return Potential(ownValue, opponentValue)
        }
    }

    internal fun potentialValue(row: Int, col: Int): Int {
        with(field[row][col]!!) {
            return directionPotential(antiDiagonal) + directionPotential(horizon) +
                    directionPotential(mainDiagonal) + directionPotential(vertical)
        }
    }

    private fun directionPotential(direction: Array<Line>): Int {
        return when (val count = direction.count()) {
            0 -> 0
            controller.win -> 2 * controller.win
            else -> controller.win + count - 1
        }
    }

    internal fun sendMove(move: Coord) {
        potentialMap[move.row][move.col] = -1
        oppPotentialMap[move.row][move.col] = usedCap
        savedGameField[move.row][move.col] = controller.marks[controller.curPlayer()]
        field[move.row][move.col]!!.forfeit()
        recalculatePotential(move)
    }

    private fun recalculatePotential(move: Coord) {
        val leftStep = min(move.col, controller.win - 1)
        val rightStep = min(controller.cols - 1 - move.col, controller.win - 1)
        val topStep = min(move.row, controller.win - 1)
        val bottomStep = min(controller.rows - 1 - move.row, controller.win - 1)

        fun recalculateSegmentPotential(segment: LineParams) {
            loop@
            for (coord in segment.iterator()) {
                when (potentialMap[coord.row][coord.col]) {
                    -1, usedCap -> continue@loop
                }
                val (own, opp) = potentialValues(coord.row, coord.col)

                potentialMap[coord.row][coord.col] = own
                oppPotentialMap[coord.row][coord.col] = opp
            }
        }

        //horizontal
        if (rightStep + 1 + leftStep >= controller.win) {
            val firstSegment = LineParams(Coord(move.row, move.col - 1), 0, -1, leftStep)
            val secondSegment = LineParams(Coord(move.row, move.col + 1), 0, 1, rightStep)

            recalculateSegmentPotential(firstSegment)
            recalculateSegmentPotential(secondSegment)
        }

        // vertical
        if (topStep + 1 + bottomStep >= controller.win) {
            val firstSegment = LineParams(
                    Coord(move.row - 1, move.col), -1, 0, topStep)
            val secondSegment = LineParams(
                    Coord(move.row + 1, move.col), 1, 0, bottomStep)

            recalculateSegmentPotential(firstSegment)
            recalculateSegmentPotential(secondSegment)
        }

        // main diagonal
        if (min(leftStep, topStep) + 1 + min(rightStep, bottomStep) >= controller.win) {
            val firstSegment = LineParams(
                    Coord(move.row - 1, move.col - 1),
                    -1,
                    -1,
                    min(leftStep, topStep))
            val secondSegment = LineParams(
                    Coord(move.row + 1, move.col + 1),
                    1,
                    1,
                    min(rightStep, bottomStep))

            recalculateSegmentPotential(firstSegment)
            recalculateSegmentPotential(secondSegment)
        }

        // anti diagonal
        if (min(leftStep, bottomStep) + 1 + min(rightStep, topStep) >= controller.win) {
            val firstSegment = LineParams(Coord(move.row + 1, move.col - 1),
                    1,
                    -1,
                    min(leftStep, bottomStep))
            val secondSegment = LineParams(Coord(move.row - 1, move.col + 1),
                    -1,
                    1,
                    min(rightStep, topStep))

            recalculateSegmentPotential(firstSegment)
            recalculateSegmentPotential(secondSegment)
        }
    }
}
