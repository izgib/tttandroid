package com.example.transport

import com.example.controllers.GameInitStatus
import com.example.controllers.GameSettings
import com.example.controllers.NetworkClient
import com.example.controllers.NetworkServer
import com.example.game.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

/*class BluetoothInteractorIml: BluetoothInteractor {
    internal val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    internal val joinFlow: MutableStateFlow<GameJoinStatus?>
    internal val createFlow: MutableStateFlow<GameCreateStatus?>

    fun createGame(settings: GameSettings): ReceiveChannel<GameInitStatus> {
        isCreator = true
        return GlobalScope.produce(capacity = Channel.CONFLATED) {
            try {
                gameSocket = startServer()
            } catch (e: IOException) {
                Log.e("BI", "got error: ${e.message}")
                send(GameInitStatus.Failure)
                return@produce
            }

            fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.GameParams,
                    with(settings) { GameParams.createGameParams(fbb, rows.toShort(), cols.toShort(), win.toShort(), creatorMark.mark) }
            ))

            try {
                gameSocket!!.outputStream
                gameSocket!!.outputStream.run {
                    withContext(Dispatchers.IO) {
                        write(fbb.sizedByteArray())
                        flush()
                    }
                }
                val serv = BluetoothServerWrapper(gameSocket!!)
                servWrapper = serv
                send(GameInitStatus.Connected(serv))
            } catch (e: IOException) {
                send(GameInitStatus.Failure)
            }
            close()
        }
    }

    override fun joinGame(device: BluetoothDevice): ReceiveChannel<GameInitStatus> {

    }

    override fun serverWrapper(): NetworkServer

    override fun clientWrapper(): NetworkClient
}*/

/*
sealed class GameJoinStatus
object Loading: GameJoinStatus()
object JoinFailure: GameJoinStatus()
class Joined(val client: NetworkClient): GameJoinStatus()

sealed class GameCreateStatus
object Awaiting: GameCreateStatus()
object CreatingFailure: GameCreateStatus()
class Connected(val server: NetworkServer): GameCreateStatus()*/
