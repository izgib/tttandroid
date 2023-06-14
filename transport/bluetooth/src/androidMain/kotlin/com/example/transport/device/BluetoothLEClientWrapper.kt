package com.example.transport.device

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.controllers.NetworkClient
import com.example.controllers.PlayerAction
import com.example.controllers.models.Response
import com.example.game.Coord
import com.example.transport.ClientAction
import com.example.transport.clientMessage
import com.example.transport.move
import kotlinx.coroutines.channels.Channel
import java.io.Closeable

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BluetoothLEClientWrapper(
    private val gatt: BluetoothGatt,
    private val clientMessages: BluetoothGattCharacteristic,
    private val operationEnd: Channel<Unit>,
    private val responseChannel: Channel<Response>,
) : NetworkClient, Closeable {
    override suspend fun getResponse(): Response {
        return responseChannel.receive()
    }

    override suspend fun sendMove(move: Coord) {
        clientMessages.value = clientMessage {
            this.move = move {
                row = move.row
                col = move.col
            }
        }.toByteArray()
        gatt.writeCharacteristic(clientMessages)
        operationEnd.receive()
    }

    override suspend fun sendAction(action: PlayerAction) {
        clientMessages.value = clientMessage {
            this.action = when (action) {
                PlayerAction.GiveUp -> ClientAction.CLIENT_ACTION_GIVE_UP
                PlayerAction.Leave -> ClientAction.CLIENT_ACTION_LEAVE
            }
        }.toByteArray()
        gatt.writeCharacteristic(clientMessages)
        operationEnd.receive()
    }

    override fun close() {
        gatt.disconnect()
    }
}