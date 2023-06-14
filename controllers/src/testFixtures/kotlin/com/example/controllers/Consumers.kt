package com.example.controllers

import com.example.controllers.models.*
import com.example.game.Coord
import com.example.game.Mark
import com.example.game.Tie
import com.example.game.Win
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun MoveRegister.consumeMoves(
    scope: CoroutineScope,
    moves: List<Coord>,
    action: PlayerAction? = null
): Job {
    val moveIterator = moves.iterator()
    return scope.launch {
        listenerState.collect { accepting ->
            if (accepting) {
                if (moveIterator.hasNext()) {
                    sendMove(moveIterator.next())
                    return@collect
                }
                if (action != null) {
                    when (action) {
                        PlayerAction.GiveUp -> giveUp()
                        PlayerAction.Leave -> cancel()
                    }
                    return@collect
                }
                throw IllegalStateException("consumed all moves")
            }
        }
    }
}

fun GameModel.consumeFlow(scope: CoroutineScope, moves: List<Coord>, endSignal: GameSignal): Job {
    val moveIterator = moves.iterator().withIndex()
    val job = scope.launch {
        gameFlow.collect { signal ->
            when (signal) {
                is Cross, is Nought -> {
                    val (i, expected) = moveIterator.next()
                    val actual = when (i and 1) {
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
                    assert(expected == actual) { "Player move $actual, expected: $expected" }
                }

                is EndState -> {
                    val actual = signal.state
                    val expected = (endSignal as EndState).state
                    assert(actual == expected) { "End State actual: $actual, expected: $expected" }
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
    start()
    return job
}


fun Mark.toChar(): Char {
    return when (this) {
        Mark.Cross -> 'X'
        Mark.Nought -> 'O'
        Mark.Empty -> '.'
    }
}

fun GameModel.justConsumeFlow(scope: CoroutineScope, rows: Int, cols: Int): Job {
    val job = scope.launch {
        val formatter = GameFieldFormatter(rows, cols)
        val final = gameFlow.onEach { signal ->
            when (signal) {
                is Cross -> formatter.putX(signal.move)
                is Nought -> formatter.putO(signal.move)
                else -> return@onEach
            }
        }.last()
        print(formatter)
        when (final) {
            is EndState -> {
                when (val state = final.state) {
                    is Win -> with(state.line) {
                        println(buildString {
                            append("player ${mark.toChar()} win game")
                            if (start != null && end != null) append(" with $start--$end")
                        })
                        mark
                    }
                    is Tie -> println("game was tied")
                    else -> throw IllegalArgumentException("expected to be WIN or TIE State")
                }
            }
            is GameInterruption -> {
                println("game was interrupted by ${final.cause.name} interruption")
                return@launch
            }
            else -> throw IllegalStateException("got unexpeted element: $final")
        }
    }
    start()
    return job
}

class GameFieldFormatter(val rows: Int, val cols: Int) {
    private var turn = 0
    val field = Array(rows) { Array(cols) { Mark.Empty } }

    fun putX(move: Coord) {
        require(field[move.row][move.col] == Mark.Empty)
        require(turn and 1 == 0)
        field[move.row][move.col] = Mark.Cross
        turn++
    }

    fun putO(move: Coord) {
        require(field[move.row][move.col] == Mark.Empty) {
            buildString { append(this)
                append("move: $move")
            }
        }
        field[move.row][move.col] = Mark.Nought
        require(turn and 1 == 1)
        turn++
    }

    override fun toString(): String {
        return buildString {
            field.forEach { row ->
                appendLine(CharArray(row.size) { i -> row[i].toChar()})
            }
        }
    }

}
