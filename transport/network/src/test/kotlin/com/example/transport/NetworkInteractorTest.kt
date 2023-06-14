package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.NetworkGameModel
import com.example.game.Mark
import com.example.game.not
import com.example.transport.service.NetworkInteractor
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import io.grpc.util.MutableHandlerRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import org.junit.Rule
import org.junit.jupiter.api.*
import java.time.Duration
import java.util.concurrent.TimeUnit


internal class NetworkInteractorTest {
    private val gameId: Int = 1
    private val serviceRegistry = MutableHandlerRegistry()

    @Rule
    val grpcCleanup = GrpcCleanupRule()
    private lateinit var client: NetworkInteractor
    private var scope = CoroutineScope(Dispatchers.Default)

    @BeforeEach
    @Timeout(1, unit = TimeUnit.SECONDS)
    fun setUp() {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName)
                .fallbackHandlerRegistry(serviceRegistry).build().start()
        )
        client = NetworkInteractor.testInstance(
            grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build()
            )
        )
    }

    @Nested
    @Timeout(1, unit = TimeUnit.SECONDS)
    internal inner class CreateGame {
        private fun setupNetworkGameCreator(example: NetworkTestExample) {
            val modelScope = CoroutineScope(Dispatchers.Default)
            val settings = GameSettings(3, 3, 3, example.playerMark)

            serviceRegistry.addService(
                PlainGameConfiguratorCreator(
                    example.moves,
                    !example.playerMark,
                    gameId,
                    example.action,
                )
            )

            val initializer = client.CreateGame(scope).sendCreationRequest(settings)
            val job = scope.async {
                val created =
                    initializer.onEach { state -> if (state is GameID) assert(state.ID == gameId) }
                        .last() as Created
                val ntClient = created.client
                val model =
                    NetworkGameModel(example.rows, example.cols, example.win, modelScope, ntClient)
                val register = MoveRegister(model)

                val localAction = with(example) {
                    if (action != null && playerMark.mark.toInt() == moves.size and 1) {
                        action
                    } else null
                }

                val ind: Int = when (example.playerMark) {
                    Mark.Cross -> {
                        model.setupPlayerX(HumanPlayer(register)); 0
                    }
                    Mark.Nought -> {
                        model.setupPlayerO(HumanPlayer(register)); 1
                    }
                    else -> throw IllegalArgumentException()
                }
                val moves = example.moves.filterIndexed { index, _ -> (index and 1) == ind }
                modelScope.launch {
                    register.consumeMoves(scope, moves, localAction).join()
                    model.cancel()
                }
                model.consumeFlow(scope, example.moves, example.endSignal)
            }
            assertTimeoutPreemptively(Duration.ofSeconds(120))
            {
                runBlocking {
                    job.join()
                }
            }
        }

        @TestFactory
        fun `Network Game Creator`(): List<DynamicTest> {

            return arrayListOf(
                DynamicTest.dynamicTest("Player X Win: Local Player X") {
                    setupNetworkGameCreator(winX.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Player X Win: Local Player O") {
                    setupNetworkGameCreator(winX.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Player O Win: Local Player X") {
                    setupNetworkGameCreator(winO.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Player O Win: Local Player O") {
                    setupNetworkGameCreator(winO.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Tie: Local Player X") {
                    setupNetworkGameCreator(tie.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Tie: Local Player O") {
                    setupNetworkGameCreator(tie.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Player X Gave-Up Player: Local Player X") {
                    setupNetworkGameCreator(gaveUpX.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Player X Gave-Up Player: Local Player O") {
                    setupNetworkGameCreator(gaveUpX.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Player O Gave-Up Player: Local Player X") {
                    setupNetworkGameCreator(gaveUpO.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Player O Gave-Up Player: Local Player O") {
                    setupNetworkGameCreator(gaveUpO.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Interrupted - Player X Cheated : Local Player X") {
                    setupNetworkGameCreator(cheatPlayerXTemplate.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Interrupted - Player X Cheated : Local Player O") {
                    setupNetworkGameCreator(cheatPlayerXTemplate.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Interrupted - Player O Cheated : Local Player X") {
                    setupNetworkGameCreator(cheatPlayerOTemplate.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Interrupted - Player O Cheated : Local Player O") {
                    setupNetworkGameCreator(cheatPlayerOTemplate.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Interrupted - Player X Left game : Local Player X") {
                    setupNetworkGameCreator(gameCancelXTemplate.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Interrupted - Player X Left game : Local Player O") {
                    setupNetworkGameCreator(gameCancelXTemplate.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Interrupted - Player O Left game : Local Player X") {
                    setupNetworkGameCreator(gameCancelOTemplate.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Interrupted - Player O Left game : Local Player O") {
                    setupNetworkGameCreator(gameCancelOTemplate.toNetworkTest(Mark.Nought))
                },
            )
        }


    }

    @Nested
    @Timeout(1, unit = TimeUnit.SECONDS)
    internal inner class JoinGame {
        private fun setupNetworkGameJoiner(example: NetworkTestExample) {
            val modelScope = CoroutineScope(Dispatchers.Default)
            val settings = GameSettings(3, 3, 3, !example.playerMark)

            serviceRegistry.addService(
                PlainGameConfiguratorJoiner(
                    settings,
                    example.moves,
                    gameId,
                    example.action
                )
            )
            val initializer = client.JoinGame(scope, gameId)
            val job = scope.async {
                val created = initializer.first { state -> state is Created }
                val ntClient = (created as Created).client

                val model =
                    NetworkGameModel(
                        example.rows,
                        example.cols,
                        example.win,
                        modelScope,
                        ntClient
                    )
                val register = MoveRegister(model)
                val localAction = with(example) {
                    if (action != null && playerMark.mark.toInt() == moves.size and 1) {
                        action
                    } else null
                }

                val ind = when (example.playerMark) {
                    Mark.Cross -> {
                        model.setupPlayerX(HumanPlayer(register)); 0
                    }
                    Mark.Nought -> {
                        model.setupPlayerO(HumanPlayer(register)); 1
                    }
                    else -> throw IllegalArgumentException()
                }
                val moves = example.moves.filterIndexed { index, _ -> (index and 1) == ind }
                modelScope.launch {
                    register.consumeMoves(scope, moves, localAction).join()
                    model.cancel()
                }
                model.consumeFlow(scope, example.moves, example.endSignal)
            }
            assertTimeoutPreemptively(Duration.ofSeconds(1)) {
                runBlocking {
                    job.join()
                }
            }
        }

        @TestFactory
        fun `Network Game Joiner`(): List<DynamicTest> {
            return arrayListOf(
                DynamicTest.dynamicTest("Player X Win: Local Player X") {
                    setupNetworkGameJoiner(
                        winX.toNetworkTest(Mark.Cross)
                    )
                },
                DynamicTest.dynamicTest("Player X Win: Local Player O") {
                    setupNetworkGameJoiner(
                        winX.toNetworkTest(Mark.Nought)
                    )
                },
                DynamicTest.dynamicTest("Player O Win: Local Player X") {
                    setupNetworkGameJoiner(
                        winO.toNetworkTest(Mark.Cross)
                    )
                },
                DynamicTest.dynamicTest("Player O Win: Local Player O") {
                    setupNetworkGameJoiner(
                        winO.toNetworkTest(Mark.Nought)
                    )
                },
                DynamicTest.dynamicTest("Tie: Local Player X") {
                    setupNetworkGameJoiner(
                        tie.toNetworkTest(
                            Mark.Cross
                        )
                    )
                },
                DynamicTest.dynamicTest("Tie: Local Player O") {
                    setupNetworkGameJoiner(
                        tie.toNetworkTest(
                            Mark.Nought
                        )
                    )
                },
                DynamicTest.dynamicTest("Player X Gave-Up Player: Local Player X") {
                    setupNetworkGameJoiner(
                        gaveUpX.toNetworkTest(Mark.Cross)
                    )
                },
                DynamicTest.dynamicTest("Player X Gave-Up Player: Local Player O") {
                    setupNetworkGameJoiner(
                        gaveUpX.toNetworkTest(Mark.Nought)
                    )
                },
                DynamicTest.dynamicTest("Player O Gave-Up Player: Local Player X") {
                    setupNetworkGameJoiner(
                        gaveUpO.toNetworkTest(Mark.Cross)
                    )
                },
                DynamicTest.dynamicTest("Player O Gave-Up Player: Local Player O") {
                    setupNetworkGameJoiner(
                        gaveUpO.toNetworkTest(Mark.Nought)
                    )
                },
                DynamicTest.dynamicTest("Interrupted - Player X Cheated : Local Player X") {
                    setupNetworkGameJoiner(
                        cheatPlayerXTemplate.toNetworkTest(Mark.Cross)
                    )
                },
                DynamicTest.dynamicTest("Interrupted - Player X Cheated : Local Player O") {
                    setupNetworkGameJoiner(
                        cheatPlayerXTemplate.toNetworkTest(Mark.Nought)
                    )
                },
                DynamicTest.dynamicTest("Interrupted - Player O Cheated : Local Player X") {
                    setupNetworkGameJoiner(
                        cheatPlayerOTemplate.toNetworkTest(Mark.Cross)
                    )
                },
                DynamicTest.dynamicTest("Interrupted - Player O Cheated : Local Player O") {
                    setupNetworkGameJoiner(
                        cheatPlayerOTemplate.toNetworkTest(Mark.Nought)
                    )
                },
                DynamicTest.dynamicTest("Interrupted - Player X Left game : Local Player X") {
                    setupNetworkGameJoiner(gameCancelXTemplate.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Interrupted - Player X Left game : Local Player O") {
                    setupNetworkGameJoiner(gameCancelXTemplate.toNetworkTest(Mark.Nought))
                },
                DynamicTest.dynamicTest("Interrupted - Player O Left game : Local Player X") {
                    setupNetworkGameJoiner(gameCancelOTemplate.toNetworkTest(Mark.Cross))
                },
                DynamicTest.dynamicTest("Interrupted - Player O Left game : Local Player O") {
                    setupNetworkGameJoiner(gameCancelOTemplate.toNetworkTest(Mark.Nought))
                },
            )
        }
    }
}