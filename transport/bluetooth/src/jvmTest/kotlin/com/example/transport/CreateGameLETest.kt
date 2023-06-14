package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.BluetoothServerGameModel
import com.example.game.Mark
import com.example.transport.service.Application
import com.example.transport.service.exportDbusObjects
import com.github.hypfvieh.bluetooth.DeviceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CreateGameLETest {
    private val timeoutMs: Long = 120_000
    private val scope = CoroutineScope(Dispatchers.Default)
    private val deviceManager = DeviceManager.createInstance(false)

    private fun setupNetworkGameCreator(example: NetworkTestExample) {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("created test launched: ${formatter.format(date)}")
        val settings = GameSettings(example.rows, example.cols, example.win, example.playerMark)


        val app = Application(deviceManager)
        app.exportDbusObjects(deviceManager.dbusConnection)
        app.registerApplication()

        val job = scope.async() {
            println("created test launched job: ${formatter.format(Date())}")
            val game = app.announceGame().onEach { state ->
                when (state) {
                    is ClientJoined -> println("client joined")
                    is Failed -> {
                        println("announcing failed")
                        app.unregisterApplication()
                    }
                    is Started -> println("started announcing")
                }
            }.last() as ClientJoined
            println("client connected: game initialized")

            val modelScope = CoroutineScope(Dispatchers.Default)
            val model = BluetoothServerGameModel(
                example.rows, example.cols, example.win, modelScope, game.server)
            val register = MoveRegister(model)
            val localAction = with(example) {
                if (action != null && playerMark.mark.toInt() == moves.size and 1) {
                    action
                } else null
            }

            val player = HumanPlayer(register)
            val ind: Int = when (example.playerMark) {
                Mark.Cross -> {
                    model.setupPlayerX(player); 0
                }
                Mark.Nought -> {
                    model.setupPlayerO(player); 1
                }
                else -> throw IllegalArgumentException()
            }

            val moves = example.moves.filterIndexed { i, _ -> (i and 1) == ind }
            modelScope.launch {
                register.consumeMoves(scope, moves, localAction).join()
                model.cancel()
            }
            val gameCycle = model.consumeFlow(scope, example.moves, example.endSignal)
            gameCycle.join()
            if (gameCycle.isCompleted && !gameCycle.isCancelled) (game.server as Closeable).close()
        }

        runBlocking {
            withTimeoutOrNull(timeoutMs) {
                job.await()
            } ?: Assert.fail("long test")
        }
    }


    @Test
    fun `1 Player X Win - Local Player X`() =
        setupNetworkGameCreator(winX.toNetworkTest(Mark.Cross))

    @Test
    fun `2 Player O Win - Local Player X`() =
        setupNetworkGameCreator(winO.toNetworkTest(Mark.Cross))

    @Test
    fun `3 Tie - Local Player X`() =
        setupNetworkGameCreator(tie.toNetworkTest(Mark.Cross))

    @Test
    fun `4 Player X Gave-Up - Local Player X`() =
        setupNetworkGameCreator(gaveUpX.toNetworkTest(Mark.Cross))

    @Test
    fun `5 Player O Gave-Up - Local Player X`() =
        setupNetworkGameCreator(gaveUpO.toNetworkTest(Mark.Cross))

    @Test
    fun `6 Interrupted - Player X Cheated - Local Player O`() = setupNetworkGameCreator(
        cheatPlayerXTemplate.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `7 Interrupted - Player O Cheated - Local Player X`() = setupNetworkGameCreator(
        cheatPlayerOTemplate.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `8 Interrupted - Player X Left game - Local Player X`() = setupNetworkGameCreator(
        gameCancelXTemplate.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `9 Interrupted - Player O Left game - Local Player X`() = setupNetworkGameCreator(
        gameCancelOTemplate.toNetworkTest(Mark.Cross)
    )
}