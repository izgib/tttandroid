package com.example.transport

import com.example.controllers.GameSettings
import com.example.controllers.NetworkClient
import com.example.controllers.NetworkServer
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import java.util.*
import kotlin.coroutines.cancellation.CancellationException


interface BluetoothInteractor {
    fun createGame(settings: GameSettings): Flow<GameCreateStatus>
    fun joinGame(device: BluetoothDevice): Flow<GameJoinStatus>

    companion object {
        const val NAME = "TTT"
        val MY_UUID = UUID.fromString("e67682c9-268c-4de8-ad82-44822952c5ee")!!
        const val timeoutMs = 120000
    }
}

/*
const val BluetoothInteractor.MY_UUID: UUID
    get() = UUID.fromString("e67682c9-268c-4de8-ad82-44822952c5ee")
*/


/*
interface BluetoothInteractor {
    fun createGame(settings: GameSettings): ReceiveChannel<GameInitStatus>

    fun joinGame(device: BluetoothDevice): ReceiveChannel<GameInitStatus>

    fun serverWrapper(): NetworkServer

    fun clientWrapper(): NetworkClient

    companion object {
        const val NAME = "TTT"
        val MY_UUID = UUID.fromString("e67682c9-268c-4de8-ad82-44822952c5ee")!!
    }
}*/

sealed class GameCreateStatus
object Awaiting : GameCreateStatus()
object CreatingFailure : GameCreateStatus()
class Connected(val server: NetworkServer) : GameCreateStatus()

sealed class GameJoinStatus
object Loading : GameJoinStatus()
object JoinFailure : GameJoinStatus()
object NeedsPairing : GameJoinStatus()
class Joined(val params: GameSettings, val client: NetworkClient) : GameJoinStatus()