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
import com.example.transport.*
import com.example.transport.test.TestActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import org.junit.*
import org.junit.runners.MethodSorters
import java.text.SimpleDateFormat
import java.util.*


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CreateGameTest {
    private val firstTestTimeout = 60_000L
    private val testTimeout = 15_000L

    private val scope = CoroutineScope(Dispatchers.Default)
    private val discoverableTimeout: Int = 60

    private fun setupNetworkGameCreator(example: NetworkTestExample) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                require(intent.action == BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
                val state = when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0)) {
                    BluetoothAdapter.SCAN_MODE_NONE -> "non connectable"
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> "connectable"
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> "discoverable;connectable"
                    else -> throw IllegalStateException("WTF")
                }
                val prevState = when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0)) {
                    BluetoothAdapter.SCAN_MODE_NONE -> "non connectable"
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> "connectable"
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> "discoverable;connectable"
                    else -> throw IllegalStateException("WTF")
                }
                println("$state -- prev: $prevState")
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        InstrumentationRegistry.getInstrumentation().context.registerReceiver(receiver, filter)

        val settings =
            GameSettings(example.rows, example.cols, example.win, example.playerMark)

        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("created test launched: ${formatter.format(date)}")


        val job = scope.async() {
            println("created test launched job: ${formatter.format(Date())}")
            val created = BluetoothInteractorImpl.createGame(settings).onEach { status ->
                when (status) {
                    is Awaiting -> {
                        println("awaiting")
                        if (firstTest) {
                            println("first test")
                            val intent = Intent(
                                ApplicationProvider.getApplicationContext(),
                                TestActivity::class.java
                            ).setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                                .putExtra(
                                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
                                    discoverableTimeout
                                )
                            launchActivityForResult<TestActivity>(intent).use { scenario ->
                                runBlocking {
                                    val result = withTimeoutOrNull(10000) {
                                        scenario.result
                                    } ?: throw IllegalStateException("timeout exceeds")
                                    if (result.resultCode == Activity.RESULT_CANCELED) {
                                        throw IllegalStateException("can not make device discoverable")
                                    }
                                    println("android makes device discoverable for: ${result.resultCode} seconds")
                                }
                            }
                        }
                    }
                    is CreatingFailure -> println("fallure")
                    is Connected -> println("connected: ")
                }
            }.last() as Connected

            val btServer = created.server

            val modelScope = CoroutineScope(Dispatchers.Default)
            val model = BluetoothServerGameModel(
                example.rows, example.cols, example.win, scope, btServer,
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
            val timeout: Long = if (firstTest) {
                firstTest = false
                firstTestTimeout
            } else testTimeout
            withTimeoutOrNull(timeout) {
                job.await()
            } ?: Assert.fail("long test")
        }
        InstrumentationRegistry.getInstrumentation().context.unregisterReceiver(receiver)
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