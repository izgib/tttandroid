package com.example.transport

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivityForResult
import androidx.test.platform.app.InstrumentationRegistry
import com.example.controllers.*
import com.example.controllers.models.BluetoothServerGameModel
import com.example.game.Mark
import com.example.transport.test.TestActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.text.SimpleDateFormat
import java.util.*

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CreateGameLETest {
    private val firstTestTimeout = 60_000L
    private val testTimeout = 30_000L

    private val timeoutMs: Long = 120_000L
    private val scope = CoroutineScope(Dispatchers.Default)

    private val discoverableTimeout: Int = 60

    private val leInteractor = BluetoothLEInteractorImpl(InstrumentationRegistry.getInstrumentation().context)

    private fun setupNetworkGameCreator(example: NetworkTestExample) {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("created test launched: ${formatter.format(date)}")

        val app = leInteractor.createApplication(GameSettings(3, 3, 3, Mark.Cross))
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
                example.rows, example.cols, example.win, scope, game.server,
            )

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
            model.consumeFlow(scope, example.moves, example.endSignal).join()
        }
        runBlocking {
            val timeout: Long = firstTestTimeout
            withTimeoutOrNull(timeout) {
                job.await()
            } ?: {
                app.unregisterApplication()
                Assert.fail("long test")
            }
        }
        app.unregisterApplication()
    }

    @Test
    fun `1_PlayerX_Win_-_Local_PlayerX`() =
        setupNetworkGameCreator(winX.toNetworkTest(Mark.Cross))

    @Test
    fun `2_PlayerO_Win_-_Local_PlayerX`() =
        setupNetworkGameCreator(winO.toNetworkTest(Mark.Cross))

    @Test
    fun `3_Tie_-_Local_PlayerX`() = setupNetworkGameCreator(tie.toNetworkTest(Mark.Cross))

    @Test
    fun `4_PlayerX_Gave-Up_-_Local_PlayerX`() = setupNetworkGameCreator(
        gaveUpX.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `5_PlayerO_Gave-Up_-_Local_PlayerX`() = setupNetworkGameCreator(
        gaveUpO.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `6_Interrupted_-_PlayerX_Cheated_-_Local_Player_O`() = setupNetworkGameCreator(
        cheatPlayerXTemplate.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `7_Interrupted_-_PlayerO_Cheated_-_Local_PlayerX`() = setupNetworkGameCreator(
        cheatPlayerOTemplate.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `8_Interrupted_-_PlayerX_Left_game_-_Local_PlayerX`() = setupNetworkGameCreator(
        gameCancelXTemplate.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `9_Interrupted_-_PlayerO_Left_game_-_Local_PlayerX`() = setupNetworkGameCreator(
        gameCancelOTemplate.toNetworkTest(Mark.Cross)
    )

    companion object {
        private var firstTest = true
    }
}