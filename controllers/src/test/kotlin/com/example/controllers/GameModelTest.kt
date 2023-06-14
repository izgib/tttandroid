package com.example.controllers

import com.example.controllers.models.LocalGameModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.time.Duration

class GameModelTest {
    private val scope = CoroutineScope(Dispatchers.Default)

    private fun setupLocalGame(example: LocalTestExample) {
        val model = LocalGameModel(example.rows, example.cols, example.win, scope)
        val moveRegister = MoveRegister(model)
        moveRegister.consumeMoves(scope, example.moves)
        model.setupPlayerX(HumanPlayer(moveRegister))
        model.setupPlayerO(HumanPlayer(moveRegister))

        val job = scope.async {
            model.consumeFlow(this, example.moves, example.endSignal)
        }
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
            runBlocking {
                job.await()
            }
        }
    }

    private fun restartedLocalGame(tries: Int, rows: Int, cols: Int, win: Int) {
        require(tries > 0)
        val model = LocalGameModel(rows, cols, win, scope)

        val job = scope.async {
            for (i: Int in 1..tries) {
                model.clearField()
                model.setupPlayerX(BotPlayer(model))
                model.setupPlayerO(BotPlayer(model))
                model.justConsumeFlow(this, rows, cols).join()
            }
        }
        runBlocking { job.await() }
        /*        assertTimeoutPreemptively(Duration.ofSeconds(2*tries.toLong())) {
                    runBlocking {
                        job.await()
                    }
                }*/
    }

    private fun setupLocalGameWithPlayerAction(
        example: LocalTestExample,
        playerX: Player,
        playerO: Player
    ) {
        require(example.action != null)
        val model = LocalGameModel(example.rows, example.cols, example.win, scope)
        val register: MoveRegister? = if (playerX == Player.Human || playerO == Player.Human) {
            MoveRegister(model).also { moveReg ->
                val moves = if (playerX == Player.Human && playerO == Player.Human) {
                    example.moves
                } else {
                    val plIndex = if (playerX == Player.Human) 0 else 1
                    example.moves.filterIndexed { index, _ -> (index and 1) == plIndex }
                }
                moveReg.consumeMoves(scope, moves, example.action)
            }
        } else {
            null
        }

        model.setupPlayerX(
            when (playerX) {
                Player.Human -> HumanPlayer(register!!)
                Player.Bot -> DummyBot(example.moves.filterIndexed { index, _ -> (index and 1) == 0 })
            }
        )

        model.setupPlayerO(
            when (playerO) {
                Player.Human -> HumanPlayer(register!!)
                Player.Bot -> DummyBot(example.moves.filterIndexed { index, _ -> (index and 1) == 1 })
            }
        )

        val job = scope.async {
            model.consumeFlow(this, example.moves, example.endSignal)
        }
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
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
            DynamicTest.dynamicTest("Tied") { setupLocalGame(tie) },
            DynamicTest.dynamicTest("GiveUpX") {
                setupLocalGameWithPlayerAction(gaveUpX, Player.Human, Player.Human)
            },
            DynamicTest.dynamicTest("GiveUpO") {
                setupLocalGameWithPlayerAction(gaveUpO, Player.Human, Player.Human)
            },
            DynamicTest.dynamicTest("GiveUpX with bot") {
                setupLocalGameWithPlayerAction(gaveUpX, Player.Human, Player.Bot)
            },
            DynamicTest.dynamicTest("GiveUpO with bot") {
                setupLocalGameWithPlayerAction(gaveUpO, Player.Bot, Player.Human)
            },
        )
    }

    @Test
    fun `restart model - game plays 3 times`() {
        restartedLocalGame(3, 3, 3, 3)
    }

    @Test
    fun lol() {
        val channel = Channel<Int>(3)
        channel.trySend(1)
        channel.trySend(2)
        channel.trySend(3)

        GlobalScope.launch {
            var i = 4
            repeat(2) {
                delay(50)
                channel.send(i)
                i++
            }
            channel.close()
        }

        val job = GlobalScope.launch {
            println("inside job")
            channel.receiveAsFlow().onEach {
                println(it)
            }.last()
        }
        runBlocking {
            job.join()
            println("finished")
        }
    }
}

private enum class Player {
    Human, Bot
}