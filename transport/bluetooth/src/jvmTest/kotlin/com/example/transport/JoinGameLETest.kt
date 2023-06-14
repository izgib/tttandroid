package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.GameInterruption
import com.example.controllers.models.InterruptCause
import com.example.controllers.models.NetworkGameModel
import com.example.game.Mark
import com.example.game.not
import com.example.transport.BluetoothDevice
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.DiscoveryFilter
import com.github.hypfvieh.bluetooth.DiscoveryTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import org.junit.*
import org.junit.rules.ExternalResource
import org.junit.runners.MethodSorters
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class JoinGameLETest {
    private var firstTest = true
    private val tries = 2
    private val retryTimeout = 1000L
    private val testTimeout = 60_000L

    private val scope = CoroutineScope(Dispatchers.Default)
    private val deviceManager = DeviceManager.createInstance(false)
    private val leInteractor = BluetoothLEInteractorImpl(deviceManager)

    @get:Rule
    val gameRule = GameItemRule(deviceManager)

    private fun setupNetworkGameJoiner(example: NetworkTestExample) {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("joined test launched: ${formatter.format(date)}")


        val job = scope.async() {
            val game = gameRule.gameItem?.let { item ->
                return@let leInteractor.connectGame(item.device, item.settings).onEach { state ->
                    when (state) {
                        is ConnectedGame -> println("game connected")
                        is ConnectingFailure -> println("connect failure")
                        is Connecting -> println("connecting")
                    }
                }.last() as ConnectedGame
            } ?: throw IllegalStateException("game item must be initialized")

            val params = game.params

            assert(params.rows == example.rows) {
                "rows: expected ${example.rows} but got ${params.rows}"
            }
            assert(params.cols == example.cols) {
                "cols: expected ${example.cols} but got ${params.cols}"
            }
            assert(params.win == example.win) {
                "win: expected ${example.win} but got ${params.win}"
            }
            assert(!params.creatorMark == example.playerMark) {
                "mark: expected ${!params.creatorMark} but got ${example.playerMark}"
            }

            val modelScope = CoroutineScope(Dispatchers.Default)
            val model =
                NetworkGameModel(params.rows, params.cols, params.win, modelScope, game.client)
            val register = MoveRegister(model)
            val localAction = with(example) {
                if (action != null && playerMark.mark.toInt() == moves.size and 1) {
                    action
                } else null
            }

            val breachPlayer: LocalPlayer? = example.run {
                if ((endSignal as? GameInterruption)?.cause == InterruptCause.InvalidMove) {
                    val ind = (moves.size - 1) and 1
                    if (playerMark.mark.toInt() == ind) {
                        return@run DummyBot(moves.filterIndexed { i, _ -> (i and 1) == ind })
                    }
                }
                return@run null
            }

            val player = breachPlayer ?: HumanPlayer(register)
            val ind: Int = when (example.playerMark) {
                Mark.Cross -> {
                    model.setupPlayerX(player); 0
                }
                Mark.Nought -> {
                    model.setupPlayerO(player); 1
                }
                else -> throw IllegalArgumentException()
            }

            if (breachPlayer == null) {
                val moves = example.moves.filterIndexed { i, _ -> (i and 1) == ind }
                modelScope.launch {
                    register.consumeMoves(scope, moves, localAction).join()
                    model.cancel()
                }
            }
            val gameCycle = model.consumeFlow(scope, example.moves, example.endSignal)
            gameCycle.join()
            if (gameCycle.isCompleted) (game.client as Closeable).close()
            gameRule.gameItem!!.device.disconnect()
        }
        runBlocking {
            withTimeoutOrNull(testTimeout) {
                job.await()
            } ?: Assert.fail("long test")
        }

    }

    @Test
    fun `1 Player X Win - Local Player O`() = setupNetworkGameJoiner(
        winX.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `2 Player O Win - Local Player O`() = setupNetworkGameJoiner(
        winO.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `3 Tie - Local Player O`() = setupNetworkGameJoiner(tie.toNetworkTest(Mark.Nought))

    @Test
    fun `4 Player X Gave-Up Player - Local Player O`() = setupNetworkGameJoiner(
        gaveUpX.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `5 Player O Gave-Up Player - Local Player O`() = setupNetworkGameJoiner(
        gaveUpO.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `6 Interrupted - Player X Cheated - Local Player X`() = setupNetworkGameJoiner(
        cheatPlayerXTemplate.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `7 Interrupted - Player O Cheated - Local Player O`() = setupNetworkGameJoiner(
        cheatPlayerOTemplate.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `8 Interrupted - Player X Left game - Local Player O`() = setupNetworkGameJoiner(
        gameCancelXTemplate.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `9 Interrupted - Player O Left game - Local Player X`() = setupNetworkGameJoiner(
        gameCancelOTemplate.toNetworkTest(Mark.Nought)
    )

    companion object {
        lateinit var wantedDevice: BluetoothDevice

        private val wantedDevName = System.getProperty("deviceName")!!
        //private val wantedDevName = "OnePlus 6T"

        private val wantedDevAddress = System.getProperty("macAddress")!!
        //private val wantedDevAddress = "64:A2:F9:B0:7B:F9"
    }
}

class GameItemRule(private val manager: DeviceManager): ExternalResource() {
    var gameItem: BluetoothGameItem? = null
        private set

    override fun before() {
        gameItem = runBlocking {
            manager.searchAdvertisingData().first()
        }
    }

    override fun after() {
        gameItem = null
    }

}