package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.BluetoothServerGameModel
import com.example.controllers.models.NetworkGameModel
import com.example.game.Mark
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


class InMemoryTest {
    private val timeout: Duration = 10.seconds

    private fun setupBluetoothTest(example: NetworkTestExample) {
        val creatorIn = PipedInputStream()
        val creatorOut = PipedOutputStream()

        val joinerIn = PipedInputStream(creatorOut)
        val joinerOut = PipedOutputStream(creatorIn)

        val creatorScope = CoroutineScope(Dispatchers.Default)
        val server = BluetoothServerWrapper(creatorScope, creatorConnection(creatorIn, creatorOut))
        val creatorModel =
            BluetoothServerGameModel(example.rows, example.cols, example.win, creatorScope, server)
        val creatorRegister = MoveRegister(creatorModel)

        val joinerScope = CoroutineScope(Dispatchers.Default)
        val client = BluetoothClientWrapper(joinerScope, joinerConnection(joinerIn, joinerOut))
        val joinerModel =
            NetworkGameModel(example.rows, example.cols, example.win, creatorScope, client)
        val joinerRegister = MoveRegister(joinerModel)

        val params = example.run {
            GameSettings(rows, cols, win, playerMark)
        }

        runBlocking {
            server.sendParams(params)
            assert(client.getParams() == params)
        }


        val xScope = CoroutineScope(CoroutineName("Player X"))
        val oScope = CoroutineScope(CoroutineName("Player O"))
        val xMoves = example.moves.filterIndexed { index, _ -> (index and 1) == 0 }
        val oMoves = example.moves.filterIndexed { index, _ -> (index and 1) == 1 }

        var xPlayerAction: PlayerAction? = null
        var oPlayerAction: PlayerAction? = null
        var xPlayer: LocalPlayer? = null
        var oPlayer: LocalPlayer? = null
        example.run {
            if (action == null) {
                xPlayer = DummyBot(xMoves)
                oPlayer = DummyBot(oMoves)
                return@run
            }

            when (moves.size and 1) {
                Mark.Cross.mark.toInt() -> {
                    xPlayerAction = action
                    oPlayer = DummyBot(oMoves)
                }
                Mark.Nought.mark.toInt() -> {
                    oPlayerAction = action
                    xPlayer = DummyBot(xMoves)
                }
            }
        }

        val creatorPlayer: Job
        val joinerPlayer: Job
        when (example.playerMark) {
            Mark.Cross -> {
                creatorModel.setupPlayerX(xPlayer ?: HumanPlayer(creatorRegister))
                creatorPlayer = creatorScope.launch {
                    creatorRegister.consumeMoves(xScope, xMoves, xPlayerAction).join()
                    creatorModel.cancel()
                }

                joinerModel.setupPlayerO(oPlayer ?: HumanPlayer(joinerRegister))
                joinerPlayer = joinerScope.launch {
                    joinerRegister.consumeMoves(oScope, oMoves, oPlayerAction).join()
                    joinerModel.cancel()
                }
            }
            Mark.Nought -> {
                creatorModel.setupPlayerO(oPlayer ?: HumanPlayer(creatorRegister))
                creatorPlayer = creatorScope.launch {
                    creatorRegister.consumeMoves(oScope, oMoves, oPlayerAction).join()
                    creatorModel.cancel()
                }

                joinerModel.setupPlayerX(xPlayer ?: HumanPlayer(joinerRegister))
                joinerPlayer = joinerScope.launch {
                    joinerRegister.consumeMoves(xScope, xMoves, xPlayerAction).join()
                    joinerModel.cancel()
                }
            }
            else -> throw IllegalArgumentException()
        }

        val creatorJob = creatorScope.async {
            select<Unit> {
                creatorPlayer.onJoin
                creatorModel.consumeFlow(creatorScope, example.moves, example.endSignal).onJoin {
                    println("creator consumed")
                }
                onTimeout(timeout) { throw IllegalStateException("creator does not finished on time") }
            }
        }
        val joinerJob = joinerScope.async {
            select<Unit> {
                joinerPlayer.onJoin
                joinerModel.consumeFlow(joinerScope, example.moves, example.endSignal).onJoin {
                    println("joiner consumed")
                }
                onTimeout(timeout) { throw IllegalStateException("joiner does not finished on time") }
            }
        }


        runBlocking {
            joinAll(creatorJob, joinerJob)
        }
    }

    @Test
    fun `Player X Win - Server X`() {
        setupBluetoothTest(winX.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Player X Win - Server O`() {
        setupBluetoothTest(winX.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Player O Win - Server X`() {
        setupBluetoothTest(winO.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Player O Win - Server O`() {
        setupBluetoothTest(winX.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Tie - Server X`() {
        setupBluetoothTest(tie.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Tie - Server O`() {
        setupBluetoothTest(tie.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Player X Gave-Up - Server X`() {
        setupBluetoothTest(gaveUpX.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Player X Gave-Up - Server O`() {
        setupBluetoothTest(gaveUpX.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Player O Gave-Up - Server X`() {
        setupBluetoothTest(gaveUpO.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Player O Gave-Up - Server O`() {
        setupBluetoothTest(gaveUpO.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Interrupted - Player X Cheated - Server O`() {
        setupBluetoothTest(cheatPlayerXTemplate.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Interrupted - Player O Cheated - Server X`() {
        setupBluetoothTest(cheatPlayerOTemplate.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Interrupted - Player X left game - Server X`() {
        setupBluetoothTest(gameCancelXTemplate.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Interrupted - Player X left game - Server O`() {
        setupBluetoothTest(gameCancelXTemplate.toNetworkTest(Mark.Nought))
    }

    @Test
    fun `Interrupted - Player O left game - Server X`() {
        setupBluetoothTest(gameCancelOTemplate.toNetworkTest(Mark.Cross))
    }

    @Test
    fun `Interrupted - Player O left game - Server O`() {
        setupBluetoothTest(gameCancelOTemplate.toNetworkTest(Mark.Nought))
    }


    private fun creatorConnection(creatorIn: InputStream, creatorOut: OutputStream) =
        object : ConnectionWrapper {
            override val inputStream = creatorIn
            override val outputStream = creatorOut

            override fun close() {
                inputStream.close()
                outputStream.close()
            }

        }


    private fun joinerConnection(joinerIn: InputStream, joinerOut: OutputStream) =
        object : ConnectionWrapper {
            override val inputStream = joinerIn
            override val outputStream = joinerOut

            override fun close() {
                inputStream.close()
                outputStream.close()
            }

        }
}