package com.example.transport.device

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.controllers.ClientResponse
import com.example.controllers.NetworkServer
import com.example.controllers.models.InterruptCause
import com.example.game.*
import com.example.transport.GameStatus
import com.example.transport.bluetoothCreatorMsg
import com.example.transport.extensions.toMarkType
import com.example.transport.extensions.toMove
import com.example.transport.extensions.toStopCause
import com.example.transport.move
import com.example.transport.winLine
import kotlinx.coroutines.channels.Channel
import java.io.Closeable

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEServerWrapper(
    private val device: BluetoothDevice,
    private val server: BluetoothGattServer,
    private val serverMessages: BluetoothGattCharacteristic,
    private val responseChannel: Channel<ClientResponse>
) : NetworkServer, Closeable {
    override suspend fun sendInterruption(cause: InterruptCause) {
        println("sending interruption")
        serverMessages.value = bluetoothCreatorMsg {
            this.cause = cause.toStopCause()
        }.toByteArray()
        server.notifyCharacteristicChanged(device, serverMessages, false)
        close()
    }

    override suspend fun sendTurn(move: Coord?, state: GameState) {
        val turn = bluetoothCreatorMsg {
            if (move != null) {
                this.move = move {
                    row = move.row
                    col = move.col
                }
            }
            when (state) {
                is Continues -> this.status = GameStatus.GAME_STATUS_OK
                is Tie -> this.status = GameStatus.GAME_STATUS_TIE
                is Win -> {
                    this.winLine = winLine {
                        state.line.start?.let { this.start = it.toMove() }
                        state.line.end?.let { this.end = it.toMove() }
                        mark = state.line.mark.toMarkType()
                    }
                }
            }
        }
        serverMessages.value = turn.toByteArray()
        println("sending turn: $move, $state")
        server.notifyCharacteristicChanged(device, serverMessages, false)
    }

    override suspend fun getResponse(): ClientResponse {
        return responseChannel.receive()
    }

    override fun close() {
        server.close()
        println("closing")
    }
}