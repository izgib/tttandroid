package com.example.game.controllers

import com.example.game.controllers.models.*
import com.example.game.domain.game.Coord
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration


internal class GameModelTest {
    private val scope = GlobalScope

    private fun setupLocalGame(example: LocalTestExample) {
        val model = LocalGameModel(example.rows, example.cols, example.win, PlayerType.Human, PlayerType.Human, scope)
        val job = scope.async {
            connectClickListener(scope, model, example.moves, PlayerType.Human, PlayerType.Human)
            consumeGameFlow(scope, model, example.moves, example.endSignal)
        }
        assertTimeoutPreemptively(Duration.ofSeconds(1)) {
            runBlocking {
                job.await()
            }
        }
    }

    @TestFactory
    fun `Local Game`(): List<DynamicTest> {
        return arrayListOf(
                DynamicTest.dynamicTest("Player X Win") { setupLocalGame(winX) },
                DynamicTest.dynamicTest("Player O Win") { setupLocalGame(winO) },
                DynamicTest.dynamicTest("Tied") { setupLocalGame(tie) }
        )
    }
}

fun connectClickListener(scope: CoroutineScope, model: GameModel, moves: Array<Coord>, playerX: PlayerType, playerO: PlayerType) {
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

fun consumeGameFlow(scope: CoroutineScope, model: GameModel, moves: Array<Coord>, endSignal: GameSignal) {
    val moveIterator = moves.iterator().withIndex()
    model.start()
    scope.launch {
        model.gameFlow.collect { signal ->
            when (signal) {
                is Cross, is Nought -> {
                    val (i, expected) = moveIterator.next()
                    val real = when (i and 1) {
                        0 -> {
                            check(signal is Cross)
                            signal.move
                        }
                        1 -> {
                            check(signal is Nought)
                            signal.move
                        }
                        else -> throw IllegalStateException()
                    }
                    assert(expected == real) { "Player move expected to be $expected, but got $real" }
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
}
