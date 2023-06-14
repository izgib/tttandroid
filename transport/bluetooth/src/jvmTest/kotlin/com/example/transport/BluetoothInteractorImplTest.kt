package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.GameInterruption
import com.example.controllers.models.InterruptCause
import com.example.controllers.models.NetworkGameModel
import com.example.controllers.models.PlayerType
import com.example.game.Coord
import com.example.game.Mark
import com.example.game.not
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import java.time.Duration
import java.util.concurrent.TimeUnit

/*
class BluetoothInteractorImplTest {
    private val gameId: Short = 1

    private lateinit var client: BluetoothInteractorImpl
    private val scope = GlobalScope

    @Before
    @Timeout(1, unit = TimeUnit.SECONDS)
    fun setUp() {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                .fallbackHandlerRegistry(serviceRegistry).build().start())
        client = NetworkInteractorImpl.testInstance(grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build())
        )
    }

    @Nested
    @Timeout(1, unit = TimeUnit.SECONDS)
    internal inner class CreateGame {
        private fun setupNetworkGameCreator(example: NetworkTestExample) {
            val settings = GameSettings(3, 3, 3, example.localPlayerMark)
            serviceRegistry.addService(PlainGameConfiguratorCreator(example.moves, !example.localPlayerMark, gameId))
            val initializer = client.CreateGame(scope).sendCreationRequest(settings)
            val job = scope.async {
                val created = initializer.onEach { state -> if (state is GameID) assert(state.ID == gameId) }.first { state -> state is Created }
                val ntClient = (created as Created).client
                val (player1, player2) = if (example.localPlayerMark == Mark.Cross) {
                    Pair(PlayerType.Human, PlayerType.Network)
                } else {
                    Pair(PlayerType.Network, PlayerType.Human)
                }

                val model = NetworkGameModel(example.rows, example.cols, example.win, player1, player2, scope, ntClient)
                connectClickListener(scope, model, example.moves, player1, player2)
                consumeGameFlow(this, model, example.moves, example.endSignal)
            }
            assertTimeoutPreemptively(Duration.ofSeconds(1)) {
                runBlocking {
                    job.await()
                }
            }
        }

        @TestFactory
        fun `Network Game Creator`(): List<DynamicTest> {
            val cheatPlayerX = NetworkTestExample(3, 3, 3, arrayOf(Coord(1, 1), Coord(0, 0), Coord(1, 1)), GameInterruption(InterruptCause.OppCheating), Mark.Nought)
            val cheatPlayerO = NetworkTestExample(3, 3, 3, arrayOf(Coord(1, 1), Coord(1, 1)), GameInterruption(InterruptCause.OppCheating), Mark.Cross)
            val gameCancel = NetworkTestExample(3, 3, 3, arrayOf(Coord(1, 1)), GameInterruption(InterruptCause.OppLeave), Mark.Cross)

            return arrayListOf(
                    DynamicTest.dynamicTest("Player X Win: Local Player X") { setupNetworkGameCreator(winX.toNetworkTest(Mark.Cross)) },
                    DynamicTest.dynamicTest("Player X Win: Local Player O") { setupNetworkGameCreator(winX.toNetworkTest(Mark.Nought)) },
                    DynamicTest.dynamicTest("Player O Win: Local Player X") { setupNetworkGameCreator(winO.toNetworkTest(Mark.Cross)) },
                    DynamicTest.dynamicTest("Player O Win: Local Player O") { setupNetworkGameCreator(winO.toNetworkTest(Mark.Nought)) },
                    DynamicTest.dynamicTest("Tie: Local Player X") { setupNetworkGameCreator(tie.toNetworkTest(Mark.Cross)) },
                    DynamicTest.dynamicTest("Tie: Local Player O") { setupNetworkGameCreator(tie.toNetworkTest(Mark.Nought)) },
                    DynamicTest.dynamicTest("Interrupted - Player X Cheated : Local Player O") { setupNetworkGameCreator(cheatPlayerX) },
                    DynamicTest.dynamicTest("Interrupted - Player O Cheated : Local Player X") { setupNetworkGameCreator(cheatPlayerO) }
                    //        DynamicTest.dynamicTest("Game Cancelation") { setupNetworkGameCreator(gameCancel) }
            )
        }

    }


    @Nested
    @Timeout(1, unit = TimeUnit.SECONDS)
    internal inner class JoinGame {
        private fun setupNetworkGameJoiner(example: NetworkTestExample) {
            val settings = GameSettings(3, 3, 3, !example.localPlayerMark)
            serviceRegistry.addService(PlainGameConfiguratorJoiner(settings, example.moves, gameId))
            val initializer = client.JoinGame(scope, gameId)
            val job = scope.async {
                val created = initializer.first { state -> state is Created }
                val ntClient = (created as Created).client
                val (player1, player2) = if (example.localPlayerMark == Mark.Cross) {
                    Pair(PlayerType.Human, PlayerType.Network)
                } else {
                    Pair(PlayerType.Network, PlayerType.Human)
                }

                val model = NetworkGameModel(example.rows, example.cols, example.win, player1, player2, scope, ntClient)
                connectClickListener(scope, model, example.moves, player1, player2)
                consumeGameFlow(this, model, example.moves, example.endSignal)
            }
            assertTimeoutPreemptively(Duration.ofSeconds(1)) {
                runBlocking {
                    job.await()
                }
            }
        }

        @TestFactory
        fun `Network Game Joiner`(): List<DynamicTest> {
            val cheatPlayerX = NetworkTestExample(3, 3, 3, arrayOf(Coord(1, 1), Coord(0, 0), Coord(1, 1)), GameInterruption(InterruptCause.OppCheating), Mark.Nought)
            val cheatPlayerO = NetworkTestExample(3, 3, 3, arrayOf(Coord(1, 1), Coord(1, 1)), GameInterruption(InterruptCause.OppCheating), Mark.Cross)

            return arrayListOf(
                    DynamicTest.dynamicTest("Player X Win: Local Player X") { setupNetworkGameJoiner(winX.toNetworkTest(Mark.Cross)) },
                    DynamicTest.dynamicTest("Player X Win: Local Player O") { setupNetworkGameJoiner(winX.toNetworkTest(Mark.Nought)) },
                    DynamicTest.dynamicTest("Player O Win: Local Player X") { setupNetworkGameJoiner(winO.toNetworkTest(Mark.Cross)) },
                    DynamicTest.dynamicTest("Player O Win: Local Player O") { setupNetworkGameJoiner(winO.toNetworkTest(Mark.Nought)) },
                    DynamicTest.dynamicTest("Tie: Local Player X") { setupNetworkGameJoiner(tie.toNetworkTest(Mark.Cross)) },
                    DynamicTest.dynamicTest("Tie: Local Player O") { setupNetworkGameJoiner(tie.toNetworkTest(Mark.Nought)) },
                    DynamicTest.dynamicTest("Interrupted - Player O Cheated : Local Player X") { setupNetworkGameJoiner(cheatPlayerO) },
                    DynamicTest.dynamicTest("Interrupted - Player X Cheated : Local Player O") { setupNetworkGameJoiner(cheatPlayerX) }
                    //DynamicTest.dynamicTest("Game Cancelation") { setupNetworkServerGame(gameCancel) }
            )
        }

    }
}*/
