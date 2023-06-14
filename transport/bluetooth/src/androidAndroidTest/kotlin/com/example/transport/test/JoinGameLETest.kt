package com.example.transport.test

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.core.app.launchActivityForResult
import androidx.test.platform.app.InstrumentationRegistry
import com.example.controllers.*
import com.example.controllers.models.GameInterruption
import com.example.controllers.models.InterruptCause
import com.example.controllers.models.NetworkGameModel
import com.example.game.Mark
import com.example.game.not
import com.example.transport.*
import com.example.transport.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
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
    private val timeoutMs: Long = 120_000
    private val scope = CoroutineScope(Dispatchers.Default)
    private val leInteractor =
        BluetoothLEInteractorImpl(InstrumentationRegistry.getInstrumentation().context)

    @get:Rule
    val gameRule = GameItemRule(leInteractor)

    private fun setupNetworkGameJoiner(example: NetworkTestExample) {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("joined test launched: ${formatter.format(date)}")

        val job = scope.async {
            val game = gameRule.gameItem?.let { item ->
                return@let leInteractor.connectGame(item.device, item.settings).onEach { state ->
                    when (state) {
                        is ConnectedGame -> println("game connected")
                        is ConnectingFailure -> println("connect failure")
                        is Connecting -> println("connecting")
                    }
                }.last() as ConnectedGame
            } ?: throw IllegalStateException("game item must be initialized")

            with(game.params) {
                assert(rows == example.rows) {
                    "rows: expected ${example.rows} but got $rows"
                }
                assert(cols == example.cols) {
                    "cols: expected ${example.cols} but got $cols"
                }
                assert(win == example.win) {
                    "win: expected ${example.win} but got $win"
                }
                assert(creatorMark == !example.playerMark) {
                    "mark: expected ${!example.playerMark} but got $creatorMark"
                }
            }

            val modelScope = CoroutineScope(Dispatchers.Default)
            val model =
                game.params.run { NetworkGameModel(rows, cols, win, modelScope, game.client) }
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
        }

        runBlocking {
            withTimeoutOrNull(timeoutMs) {
                job.await()
            } ?: Assert.fail("long test")
        }
    }

    @Test
    fun `1_PlayerX_Win_-_Local_PlayerO`() = setupNetworkGameJoiner(
        winX.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `2_PlayerO_Win_-_Local_PlayerO`() = setupNetworkGameJoiner(
        winO.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `3_Tie_-_Local_PlayerO`() = setupNetworkGameJoiner(tie.toNetworkTest(Mark.Nought))

    @Test
    fun `4_PlayerX_Gave-Up_-_Local_PlayerO`() = setupNetworkGameJoiner(
        gaveUpX.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `5_PlayerO_Gave-Up_-_Local_PlayerO`() = setupNetworkGameJoiner(
        gaveUpO.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `6_Interrupted_-_PlayerX_Cheated_-_Local_Player_X`() = setupNetworkGameJoiner(
        cheatPlayerXTemplate.toNetworkTest(Mark.Cross)
    )

    @Test
    fun `7_Interrupted_-_PlayerO_Cheated_-_Local_PlayerO`() = setupNetworkGameJoiner(
        cheatPlayerOTemplate.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `8_Interrupted_-_PlayerX_Left_game_-_Local_PlayerO`() = setupNetworkGameJoiner(
        gameCancelXTemplate.toNetworkTest(Mark.Nought)
    )

    @Test
    fun `9_Interrupted_-_PlayerO_Left_game_-_Local_PlayerX`() = setupNetworkGameJoiner(
        gameCancelOTemplate.toNetworkTest(Mark.Nought)
    )

    companion object {
        lateinit var wantedDevice: BluetoothDevice
        private var devScenario: ActivityScenario<TestActivity>? = null

        @BeforeClass
        @JvmStatic
        fun setupWantedDevice() {

            val intent =
                Intent(ApplicationProvider.getApplicationContext(), TestActivity::class.java)
            intent.putExtra("name", BuildConfig.deviceName)
            intent.putExtra("address", BuildConfig.macAddress)

            val scenario = launchActivityForResult<TestActivity>(intent)
            scenario.moveToState(Lifecycle.State.STARTED)

            println("setting devices")
            val device = runBlocking {
                val result = withTimeoutOrNull(10000) {
                    scenario.result
                } ?: throw IllegalStateException("timeout exceeds")
                return@runBlocking when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val device: BluetoothDevice =
                            result.resultData.getParcelableExtra("device") as BluetoothDevice?
                                ?: throw IllegalStateException("expecting device")
                        println(device.name)
                        println(device.address)
                        device.uuids.forEach { parcelUuid ->
                            println(parcelUuid)
                        }
                        device
                    }
                    Activity.RESULT_CANCELED -> throw IllegalStateException("does not found device")
                    else -> throw IllegalStateException("unexpected result code: ${result.resultCode}")
                }
            }
            scenario.close()

            runBlocking {
                when (device.bondState) {
                    BluetoothDevice.BOND_NONE, BluetoothDevice.BOND_BONDING -> {
                        //device.automaticPairDevice(InstrumentationRegistry.getInstrumentation().targetContext)
                        val devIntent =
                            Intent(
                                ApplicationProvider.getApplicationContext(),
                                TestActivity::class.java
                            )
                                .putExtra("device", device)
                        devScenario = launchActivity<TestActivity>(devIntent)
                        devScenario?.moveToState(Lifecycle.State.STARTED)
                        println("pairing")
                        device.pairDevice(InstrumentationRegistry.getInstrumentation().targetContext)
                    }
                    BluetoothDevice.BOND_BONDED -> Unit
                    else -> throw IllegalStateException()
                }
            }
            wantedDevice = device
        }

        @AfterClass
        @JvmStatic
        fun deinitialize() {
            devScenario?.close()
            devScenario = null
        }
    }
}

class GameItemRule(private val leInteractor: BluetoothLEInteractor): ExternalResource() {
    var gameItem: BluetoothGameItem? = null
        private set

    override fun before() {
        gameItem = runBlocking {
            leInteractor.getDeviceList().first()
        }
    }

    override fun after() {
        gameItem = null
    }

}