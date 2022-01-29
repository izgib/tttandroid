package com.example.controllers

import com.example.controllers.models.*
import com.example.game.Coord
import com.example.game.Mark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun connectClickListener(
    scope: CoroutineScope,
    model: GameModel,
    moves: Array<Coord>,
    playerX: PlayerType,
    playerO: PlayerType
) {
    val players = arrayOf(playerX, playerO)
    val moveIterator = moves.iterator().withIndex()
    scope.launch {
        model.clickRegister.listenerState.collect { accepting ->
            if (accepting) {
                var i: Int
                var move: Coord
                do {
                    moveIterator.next().run {
                        i = index
                        move = value
                    }
                } while (players[i and 1] != PlayerType.Human)
                model.clickRegister.moveChannel.send(move)
            }
        }
    }
}

fun consumeGameFlow(
    scope: CoroutineScope,
    model: GameModel,
    moves: Array<Coord>,
    endSignal: GameSignal
) {
    val gameField = GameField(model.rows, model.cols, model.win)
    val moveIterator = moves.iterator().withIndex()
    scope.launch {
        model.gameFlow.collect { signal ->
            when (signal) {
                is Cross, is Nought -> {
                    val (i, expected) = moveIterator.next()
                    val real = when (i and 1) {
                        0 -> {
                            check(signal is Cross)
                            signal.move //.also { gameField.putX(it) }
                        }
                        1 -> {
                            check(signal is Nought)
                            signal.move //.also { gameField.putO(it) }
                        }
                        else -> throw IllegalStateException()
                    }
                    assert(expected == real) { "Player move expected to be $expected, but got $real" }
/*                    val line = CharArray(gameField.cols){'-'}
                    println(line)
                    print(gameField)
                    println(line)*/
                }
                is EndState -> {
                    val real = signal.state
                    val expected = (endSignal as EndState).state
                    assert(real == expected) { "End State: real: $real: expected: $expected" }
                    assert(!moveIterator.hasNext()) { "not all move have been consumed" }
                    return@collect
                }
                is GameInterruption -> {
                    val real = signal.cause
                    val expected = (endSignal as GameInterruption).cause
                    assert(real == expected) { "Interruption: real: $real: expected: $expected" }
                    return@collect
                }
            }
        }
    }
    model.start()
}

private class GameField(val rows: Int, val cols: Int, var winLine: Int) {
    private val gameField: Array<Array<Mark>> = Array(rows) { Array(cols) { Mark.Empty } }

    fun putX(move: Coord) {
        gameField[move.row][move.col] = Mark.Cross
    }

    fun putO(move: Coord) {
        gameField[move.row][move.col] = Mark.Nought
    }

    override fun toString(): String = buildString(rows * (cols + 1)) {
        gameField.forEach { row ->
            appendLine(CharArray(cols) { i ->
                when (row[i]) {
                    Mark.Cross -> 'X'
                    Mark.Nought -> 'O'
                    Mark.Empty -> ' '
                }
            })
        }
    }
}