@file:Suppress("IllegalIdentifier")

package com.example.transport


import com.example.controllers.*
import com.example.controllers.models.GameInterruption
import com.example.controllers.models.InterruptCause
import com.example.controllers.models.NetworkGameModel
import com.example.game.Mark
import com.example.game.not
import com.example.transport.bluez.interfaceAddListener
import com.example.transport.bluez.interfaceRemovedListener
import com.example.transport.bluez.propertyListener
import com.github.hypfvieh.bluetooth.DeviceManager
import com.github.hypfvieh.bluetooth.DiscoveryFilter
import com.github.hypfvieh.bluetooth.DiscoveryTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import org.bluez.Device1
import org.bluez.Profile1
import org.bluez.ProfileManager1
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.io.Closeable
import java.text.SimpleDateFormat
import java.util.*


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class JoinGameTest {
    private val tries = 2
    private val retryTimeout = 1000L
    private val testTimeout = 60_000L

    private val scope = CoroutineScope(Dispatchers.Default)

    private fun setupNetworkGameJoiner(example: NetworkTestExample) {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date()
        println("joined test launched: ${formatter.format(date)}")

        val job = scope.async() {
            val joined: Joined = run {
                val t = if (firstTest) {
                    firstTest = false; 1
                } else tries
                for (i in 1..t) {
                    val joined = BluetoothInteractorImpl.joinGame(wantedDevice).onEach { status ->
                        when (status) {
                            is Loading -> println("loading")
                            is NeedsPairing -> throw IllegalStateException("needs pairing")
                            is JoinFailure -> println("falure")
                            is Joined -> println("joined: $wantedDevice")
                        }
                    }.last()
                    if (joined is Joined) return@run joined
                    delay(retryTimeout)
                }
                throw IllegalStateException("can not join game")
            }
            val params = joined.params

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
                NetworkGameModel(params.rows, params.cols, params.win, modelScope, joined.client)
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
        private var firstTest = true

        lateinit var wantedDevice: BluetoothDevice

        private val wantedDevName = System.getProperty("deviceName")!!

        private val wantedDevAddress = System.getProperty("macAddress")!!

        @BeforeClass
        @JvmStatic
        fun setupWantedDevice() {
            println("setup Devices")
            println("wanted $wantedDevName: $wantedDevAddress")
            val deviceManager = DeviceManager.createInstance(false)

            deviceManager.setScanFilter(
                mapOf(
                    DiscoveryFilter.Transport to DiscoveryTransport.BREDR,
                    DiscoveryFilter.DuplicateData to false,
                    //DiscoveryFilter.RSSI to (-100).toShort(),
                )
            )


            wantedDevice = runBlocking {
                return@runBlocking deviceManager.deviceSearchFlow(30000).onEach { dev ->
                    println("${dev.name}: ${dev.address}")
                }.firstOrNull { device ->
                    device.name == wantedDevName && device.address.contentEquals(
                        wantedDevAddress,
                        true
                    )
                } as BluetoothDevice?
            }
                ?: throw IllegalStateException("can not find device ${wantedDevName}:${wantedDevAddress}")
            if ((wantedDevice as com.github.hypfvieh.bluetooth.wrapper.BluetoothDevice).uuids.none { it == BluetoothInteractor.MY_UUID.toString() })
                println("can not find service with UUID:${BluetoothInteractor.MY_UUID}") else {
                println("${wantedDevice.name}:$wantedDevice.address} have TTT service")
            }
            /*val deviceList = runBlocking {
                deviceManager.deviceSearchFlow(30000).onEach { dev ->
                    println("${dev.name}: ${dev.address}")
*//*                    dev.uuids.forEachIndexed { i, service ->
                        println("${i + 1}: $service")
                    }*//*
                }.toList()
            }*/
/*            val haveNoDevice = deviceList.none { device ->
                device.uuids.firstOrNull { UUID.fromString(it) == BluetoothInteractor.MY_UUID }
                    ?.let {
                        if (!(device.name == wantedDevName && device.address == wantedDevAddress))
                            throw IllegalStateException("wrong device[${device.name}:${device.address}], but expected [$wantedDevName: $wantedDevAddress]")
                        wantedDevice = device
                        return@none true
                    }
                return@none false
            }
            if (haveNoDevice) throw IllegalStateException("searching for bluetooth profile with UUID:${BluetoothInteractor.MY_UUID}, but could not do it")*/
        }
    }
}