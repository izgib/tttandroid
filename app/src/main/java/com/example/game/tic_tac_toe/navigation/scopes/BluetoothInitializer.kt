package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.models.GameModel
import com.example.controllers.models.NetworkGameModel
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.screens.GameScreen
import com.example.transport.BluetoothDevice
import com.example.transport.BluetoothInteractor
import com.example.transport.GameJoinStatus
import com.example.transport.JoinFailure
import com.example.transport.Joined
import com.example.transport.Loading
import com.example.transport.NeedsPairing
import com.example.transport.device.BluetoothSensor
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

class BluetoothInitializer(
    private val backstack: Backstack,
    private val handle: BluetoothInteractor,
) : ScopedServices.Registered {
    private val btSensor: BluetoothSensor = backstack.lookup()

    private var scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var scopeBounded = true
    private val _started = MutableStateFlow<Boolean>(false)
    val started: StateFlow<Boolean> = _started

    private val dSet = linkedSetOf<BluetoothDevice>()
    val deviceFound: LinkedHashSet<BluetoothDevice> = dSet
    private fun createScope() = CoroutineScope(Job() + Dispatchers.Main.immediate)

    override fun onServiceUnregistered() {
        if (scopeBounded) scope.cancel()
    }

    override fun onServiceRegistered() = Unit

    private fun cancelAndRecreate(): Unit = scope.let { oldScope ->
        scope = createScope()
        _started.value = false
        oldScope.cancel()
    }

    private var devices: Channel<Unit>? = null
    // Return flow that emits Unit when found device append into deviceFoundList, flow gets canceled after device
    fun findDevices(): Flow<Unit> {
        if (devices == null) {
            devices = Channel(Channel.CONFLATED)
            dSet.clear()
            _started.value = true
            scope.launch {
                btSensor.findDevices().onEach {
                    dSet.add(it)
                    devices!!.send(Unit)
                }.collect()
                devices?.let { oldDevices ->
                    devices = null
                    oldDevices.close()
                    _started.value = false
                }
            }
        }
        return devices!!.receiveAsFlow()
    }

    private val jState = MutableStateFlow<GameJoinStatus?>(null)
    fun joinDevice(device: BluetoothDevice): Flow<GameJoinStatus> {
        if (jState.value == null) {
            scope.launch {
                val final = handle.joinGame(device).last()

                when (final) {
                    is Joined -> {
                        with(final.params) {
                            backstack.lookup<GameConfig>().fromSettings(this)
                            goToGame(NetworkGameModel(rows, cols, win, scope, final.client))
                        }
                    }

                    JoinFailure -> println("falied")
                    NeedsPairing -> println("needs pairing")
                    else -> throw IllegalStateException()
                }
                jState.value = null
            }
        }
        return jState.takeWhile { it != null } as Flow<GameJoinStatus>
    }

    private fun goToGame(model: GameModel) {
        scopeBounded = false
        backstack.goTo(GameScreen(model, scope))
    }

    fun cancel() {
        cancelAndRecreate()
    }
}