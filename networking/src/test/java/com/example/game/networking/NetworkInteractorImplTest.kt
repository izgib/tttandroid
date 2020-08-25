package com.example.game.networking

import com.example.game.controllers.*
import com.example.game.controllers.models.GameInterruption
import com.example.game.controllers.models.InterruptCause
import com.example.game.controllers.models.NetworkGameModel
import com.example.game.controllers.models.PlayerType
import com.example.game.domain.game.Coord
import com.example.game.domain.game.Mark
import com.example.game.domain.game.not
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import io.grpc.util.MutableHandlerRegistry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.jupiter.api.*
import java.time.Duration
import java.util.concurrent.TimeUnit


internal class NetworkInteractorImplTest {
    private val gameId: Short = 1
    private val serviceRegistry = MutableHandlerRegistry()

    @Rule
    val grpcCleanup = GrpcCleanupRule()
    private lateinit var client: NetworkInteractorImpl
    private val scope = GlobalScope

    @BeforeEach
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


        /*@Test
        fun `standard situation`() {
            val serviceImpl = object : GameConfiguratorGrpc.GameConfiguratorImplBase() {
                override fun createGame(responseObserver: StreamObserver<CrResponse>): StreamObserver<CrRequest> {
                    var first = true
                    return object : StreamObserver<CrRequest> {
                        override fun onNext(value: CrRequest) {
                            println("got request")
                            if (first) {
                                val job = scope.launch {
                                    assert(value.reqType() == CreatorReqMsg.GameParams) { "wrong event" }
                                    fbb.finish(
                                            CrResponse.createCrResponse(fbb, CreatorRespMsg.GameId,
                                                    GameId.createGameId(fbb, gameId)
                                            )
                                    )
                                    responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
                                    println("sent game id")

                                    GameEvent.startGameEvent(fbb)
                                    GameEvent.addType(fbb, GameEventType.GameStarted)
                                    fbb.finish(CrResponse.createCrResponse(fbb, CreatorRespMsg.GameEvent,
                                            GameEvent.endGameEvent(fbb))
                                    )
                                    responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
                                    //delay(Random.nextLong(100))
                                    println("sent Start Token")
                                    first = false
                                }
                                runBlocking(scope.coroutineContext) { job.join() }
                            } else {
                                assert(value.reqType() == CreatorReqMsg.Move) { "wrong package" }
                                val received = (value.req(Move()) as Move)
                                val gameCoord = Coord(received.row().toInt(), received.col().toInt())
                                assert(gameCoord == move) { "wrong move" }
                            }
                            println("request processed")
                            //}
                            //responseObserver.onCompleted()
                        }

                        override fun onError(t: Throwable) {
                            println("WTF: $t")
                        }

                        override fun onCompleted() {
                            println("all is good")
                        }
                    }
                }
            }
            serviceRegistry.addService(serviceImpl)
            //val states = arrayOf<GameCreationStatus>(GameID(gameId), Created)
            val initializer = client.CreateGame(scope).sendCreationRequest(settings)
            val job = scope.launch {
                var i = 0
                println("start collect")
                initializer.collect { state ->
                    println(state)
                    //assert(state == states[i]) { "expected: ${states[i]}, but got: ${state}" }
                    i++
                }
            }
            runBlocking {
                job.join()
                val wrapper = client.getGameClientWrapper()
                wrapper.sendMove(move)
                wrapper.cancelGame()
            }
        }

        @Test
        fun `server is unavaliable`() {
            val states = arrayOf<GameCreationStatus>(CreationFailure)
            val initializer = client.CreateGame(scope).sendCreationRequest(settings)
            val job = scope.launch {
                var i = 0
                initializer.collect { state ->
                    println("collect state")
                    assert(state == states[i]) { "expected: ${states[i]}, but got: ${state}" }
                    i++
                }
            }
            runBlocking {
                job.join()
            }
        }

        @Test
        fun `connection error on init phase`() {
            val fakeError = StatusRuntimeException(Status.DEADLINE_EXCEEDED)
            val serverImpl = object : GameConfiguratorGrpc.GameConfiguratorImplBase() {
                override fun createGame(responseObserver: StreamObserver<CrResponse>): StreamObserver<CrRequest> {
                    return object : StreamObserver<CrRequest> {
                        override fun onNext(value: CrRequest) {
                            assert(value.reqType() == CreatorReqMsg.GameParams) { "wrong event" }
                            fbb.finish(
                                    CrResponse.createCrResponse(fbb, CreatorRespMsg.GameId,
                                            GameId.createGameId(fbb, gameId)
                                    )
                            )
                            responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
                            println("sent game id")

                            responseObserver.onError(fakeError)
                        }

                        override fun onError(t: Throwable?) {
                            println("WTF: $t")
                        }

                        override fun onCompleted() {
                            println("all is good")
                        }

                    }
                }
            }
            serviceRegistry.addService(serverImpl)

            val states = arrayOf<GameCreationStatus>(GameID(gameId), CreationFailure)
            val initializer = client.CreateGame(scope).sendCreationRequest(settings)
            val job = scope.launch {
                var i = 0
                println("start collect")
                initializer.collect { state ->
                    println(state)
                    assert(state == states[i]) { "expected: ${states[i]}, but got: ${state}" }
                    i++
                }
            }
            runBlocking {
                job.join()
            }
        }
        @Test()
        fun `connection error on game phase`() {
            val fakeError = Status.DEADLINE_EXCEEDED.asRuntimeException()
            val serviceImpl = object : GameConfiguratorGrpc.GameConfiguratorImplBase() {
                override fun createGame(responseObserver: StreamObserver<CrResponse>): StreamObserver<CrRequest> {
                    var first = true
                    return object : StreamObserver<CrRequest> {
                        override fun onNext(value: CrRequest) {
                            //scope.launch {
                            //runBlocking(Dispatchers.IO) {
                            if (first) {
                                assert(value.reqType() == CreatorReqMsg.GameParams) { "wrong event" }
                                fbb.finish(
                                        CrResponse.createCrResponse(fbb, CreatorRespMsg.GameId,
                                                GameId.createGameId(fbb, gameId)
                                        )
                                )
                                responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
                                println("sent game id")
                                //delay(Random.nextLong(100))

                                //delay(100)

                                GameEvent.startGameEvent(fbb)
                                GameEvent.addType(fbb, GameEventType.GameStarted)
                                fbb.finish(CrResponse.createCrResponse(fbb, CreatorRespMsg.GameEvent,
                                        GameEvent.endGameEvent(fbb))
                                )
                                responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
                                //delay(Random.nextLong(100))
                                println("sent game start token")
                                first = false
                            } else {
                                assert(value.reqType() == CreatorReqMsg.Move) { "wrong package" }
                                val received = (value.req(Move()) as Move)
                                val gameCoord = Coord(received.row().toInt(), received.col().toInt())
                                assert(gameCoord == move) { "wrong move" }

                                println("throw connection error")
                                responseObserver.onError(fakeError)
                            }
                            //}
                            //responseObserver.onCompleted()
                        }

                        override fun onError(t: Throwable) {
                            println("WTF: $t")
                        }

                        override fun onCompleted() {
                            println("all is good")
                        }
                    }
                }
            }
            serviceRegistry.addService(serviceImpl)

            //val states = arrayOf<GameCreationStatus>(GameID(gameId), Created)
            val initializer = client.CreateGame(scope).sendCreationRequest(settings)
            val job = scope.launch {
                var i = 0
                println("start collect")
                initializer.collect { state ->
                    println(state)
                    //assert(state == states[i]) { "expected: ${states[i]}, but got: ${state}" }
                    i++
                }
            }
            runBlocking {
                job.join()
                val wrapper = client.getGameClientWrapper()
                wrapper.sendMove(move)
                try {
                    wrapper.getState()
                } catch (e: InterruptionException) {
                    return@runBlocking
                }
                fail("method must throw exception")
            }
        }*/
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

/*        @Test
        fun `standard situation`() {
            val serviceImpl = object : GameConfiguratorGrpc.GameConfiguratorImplBase() {
                override fun joinGame(responseObserver: StreamObserver<OppResponse>): StreamObserver<OppRequest> {
                    return object : StreamObserver<OppRequest> {
                        override fun onNext(value: OppRequest) {
                            assert(value.reqType() == OpponentReqMsg.GameId) { "wrong package" }
                            assert((value.req(GameId()) as GameId).ID() == gameId) { "wrong id" }

                            GameEvent.startGameEvent(fbb)
                            GameEvent.addType(fbb, GameEventType.GameStarted)
                            fbb.finish(
                                    OppResponse.createOppResponse(fbb, OpponentRespMsg.GameEvent, GameEvent.endGameEvent(fbb))
                            )
                            responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
                            println("start token sent")

                            fbb.finish(
                                    OppResponse.createOppResponse(fbb, OpponentRespMsg.Move,
                                            Move.createMove(fbb, move.row.toShort(), move.col.toShort())
                                    )
                            )
                            responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
                            println("move sent")

                        }

                        override fun onError(t: Throwable) {
                            println("WTF :$t")
                        }

                        override fun onCompleted() {
                            println("all is good")
                        }

                    }
                }
            }
            serviceRegistry.addService(serviceImpl)

            //val response = Created
            val gameState = client.JoinGame(scope, gameId)
            val job = scope.launch {
                println("start collect")
                gameState.collect { state ->
                    println(state)
                    //assert(state == response) { "expected: $response, but got: $state" }
                }
            }
            runBlocking {
                job.join()
                val wrapper = client.getGameClientWrapper()
                println("before move receiving")
                val received = wrapper.getMove()
                assert(received == move) { "got wrong move" }
                wrapper.cancelGame()
            }
        }

        @Test
        fun `server unavailable`() {
            val response = CreationFailure
            val gameState = client.JoinGame(scope, gameId)
            val job = scope.launch {
                gameState.collect { state ->
                    assert(state == response) { "expected: $response, but got: $state" }
                }
            }
            runBlocking {
                job.join()
            }
        }

        @Test
        fun `wrong Id`() {
            val fakeError = StatusRuntimeException(Status.NOT_FOUND)

            val serviceImpl = object : GameConfiguratorGrpc.GameConfiguratorImplBase() {
                override fun joinGame(responseObserver: StreamObserver<OppResponse>): StreamObserver<OppRequest> {
                    return object : StreamObserver<OppRequest> {
                        override fun onNext(value: OppRequest) {
                            responseObserver.onError(fakeError)
                        }

                        override fun onError(t: Throwable) {
                            println("Wtf: $t")
                        }

                        override fun onCompleted() {
                            println("all is good")
                        }

                    }
                }
            }
            serviceRegistry.addService(serviceImpl)

            val response = CreationFailure
            val gameState = client.JoinGame(scope, gameId)
            val job = scope.launch {
                println("start collect")
                gameState.collect { state ->
                    println(state)
                    assert(state == response) { "expected: $response, but got: $state" }
                }
            }
            runBlocking {
                job.join()
            }
        }

        @Test()
        fun `connection error on game phase`() {
            val fakeError = Status.DEADLINE_EXCEEDED.asRuntimeException()
            val serviceImpl = object : GameConfiguratorGrpc.GameConfiguratorImplBase() {
                override fun joinGame(responseObserver: StreamObserver<OppResponse>): StreamObserver<OppRequest> {
                    return object : StreamObserver<OppRequest> {
                        override fun onNext(value: OppRequest) {
                            assert(value.reqType() == OpponentReqMsg.GameId) { "wrong package" }
                            assert((value.req(GameId()) as GameId).ID() == gameId) { "wrong id" }

                            GameEvent.startGameEvent(fbb)
                            GameEvent.addType(fbb, GameEventType.GameStarted)
                            fbb.finish(
                                    OppResponse.createOppResponse(fbb, OpponentRespMsg.GameEvent, GameEvent.endGameEvent(fbb))
                            )
                            responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
                            println("start token sent")

                            fbb.finish(
                                    OppResponse.createOppResponse(fbb, OpponentRespMsg.Move,
                                            Move.createMove(fbb, move.row.toShort(), move.col.toShort())
                                    )
                            )
                            responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
                            println("move sent")

                            responseObserver.onError(fakeError)
                        }

                        override fun onError(t: Throwable) {
                            println("WTF :$t")
                        }

                        override fun onCompleted() {
                            println("all is good")
                        }

                    }
                }
            }
            serviceRegistry.addService(serviceImpl)

            //val response = Created
            val gameState = client.JoinGame(scope, gameId)
            val job = scope.launch {
                println("start collect")
                gameState.collect { state ->
                    println(state)
                    //assert(state == response) { "expected: $response, but got: $state" }
                }
            }
            runBlocking {
                job.join()
                val wrapper = client.getGameClientWrapper()
                println("before move receiving")
                val received = wrapper.getMove()
                assert(received == move) { "got wrong move" }

                try {
                    wrapper.getState()
                } catch (e: InterruptionException) {
                    return@runBlocking
                }
                fail("method must throw exception")
            }
        }*/
    }
}