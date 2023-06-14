package com.example.transport

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
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
import com.example.transport.test.TestActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.*
import org.junit.runners.MethodSorters
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class JoinGameTest {
    private val timeoutMs: Long = 120_000
    private val scope = CoroutineScope(Dispatchers.Default)
    private val tries = 2
    private val retryTimeout = 1000L

    private val devName: String = BuildConfig.deviceName
    private val devAddress: String = BuildConfig.macAddress
    private var paired: Boolean = false

    init {
        println("${BuildConfig.deviceName}: ${BuildConfig.deviceName.length}")
        println("${BuildConfig.macAddress}: ${BuildConfig.macAddress.length}")
    }

/*    @get:Rule
    val activityRule = ActivityScenarioRule(TestActivity::class.java)*/


    /*private val devName = System.getProperty("deviceName")
    private val devAddress = System.getProperty("macAddress")*/
/*    @get:Rule
    val chain = RuleChain.outerRule(permissionRule).around(activityRule)*/

/*    @Test
    fun h() {
        activityRule.scenario.onActivity { activity ->
            activity.lifecycleScope.launchWhenStarted {
                val wantedDevice = activity.btSensor.findDevices().first { device ->
                    device.address == macAddress && device.name == deviceName
                }
            }
        }
    }*/

    private fun setupNetworkGameJoiner(example: NetworkTestExample) {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("joined test launched: ${formatter.format(date)}")

        val job = scope.async {
            val joined = BluetoothInteractorImpl.joinGame(wantedDevice).onEach { status ->
                when (status) {
                    is Loading -> println("loading")
                    is JoinFailure -> println("failure")
                    is NeedsPairing -> throw IllegalStateException("need pair devices")
                    is Joined -> println("joined: $wantedDevice")
                }
            }.last() as Joined

            with(joined.params) {
                assert(rows == example.rows) {
                    "rows: expected ${example.rows} but got $rows"
                }
                assert(cols == example.cols) {
                    "cols: expected ${example.cols} but got $cols"
                }
                assert(win == example.win) {
                    "win: expected ${example.win} but got $win"
                }
                assert(!creatorMark == example.playerMark) {
                    "mark: expected ${!creatorMark} but got ${example.playerMark}"
                }
            }

            val modelScope = CoroutineScope(Dispatchers.Default)
            val model =
                joined.params.run { NetworkGameModel(rows, cols, win, modelScope, joined.client) }
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
            if (gameCycle.isCompleted) (joined.client as Closeable).close()
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
                    RESULT_OK -> {
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
                    RESULT_CANCELED -> throw IllegalStateException("does not found device")
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

suspend fun BluetoothDevice.automaticPairDevice(context: Context) =
    suspendCancellableCoroutine<Unit> { continuation ->
        val pairingListener = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                            ?: throw IllegalStateException()
                    val variant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, -1)
                    if (variant < 0) throw IllegalStateException()
                    when (variant) {
                        BluetoothDevice.PAIRING_VARIANT_PIN -> {
                            device.setPin(byteArrayOf(0x00, 0x00, 0x12, 0x34))
                        }
                        BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION -> {
                            device.setPairingConfirmation(true)
                        }
                    }
                    context.unregisterReceiver(this)
                    continuation.resume(Unit)
                }
            }
        }
        val pairFilter = IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
        pairFilter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY - 1
        context.registerReceiver(pairingListener, pairFilter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            check(this.createBond())
        } //TODO сделать для старых версий https://www.programcreek.com/java-api-examples/?class=android.bluetooth.BluetoothDevice&method=createBond
        continuation.invokeOnCancellation { context.unregisterReceiver(pairingListener) }
    }