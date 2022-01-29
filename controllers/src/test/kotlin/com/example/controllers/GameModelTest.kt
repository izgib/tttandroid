package com.example.controllers

import com.example.controllers.models.LocalGameModel
import com.example.controllers.models.PlayerType
import kotlinx.coroutines.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.Duration

class GameModelTest {
    private val scope = CoroutineScope(Dispatchers.Default)

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