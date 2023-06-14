package com.example.transport

import com.example.controllers.NetworkClient
import com.example.controllers.PlayerAction
import com.example.controllers.models.InterruptionException
import com.example.controllers.models.Response
import com.example.game.Coord
import com.github.hypfvieh.bluetooth.wrapper.BluetoothGattCharacteristic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Closeable

class BluetoothLEClientWrapper(
    private val clientMessages: BluetoothGattCharacteristic,
    private val responses: ReceiveChannel<Response>,
) : NetworkClient, Closeable {

    override suspend fun getResponse(): Response {
        println("response received client")
        val resp = responses.receive()
        println("move: ${resp.move} state: ${resp.state}")
        return resp
    }

    override suspend fun sendMove(move: Coord) {
        val data = clientMessage {
            this.move = move {
                row = move.row
                col = move.col
            }
        }.toByteArray()
        clientMessages.writeValue(data, mapOf())
    }

    override suspend fun sendAction(action: PlayerAction) {
        println("sending action")
        val data = clientMessage {
            this.action = when (action) {
                PlayerAction.Leave -> ClientAction.CLIENT_ACTION_LEAVE
                PlayerAction.GiveUp -> ClientAction.CLIENT_ACTION_GIVE_UP
            }
        }.toByteArray()
        clientMessages.writeValue(data, mapOf())
    }

    override fun close() {
        println("closing wrapper")
        responses.cancel()
    }
}