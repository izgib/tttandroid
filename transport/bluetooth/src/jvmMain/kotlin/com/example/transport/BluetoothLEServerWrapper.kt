package com.example.transport

import com.example.controllers.ClientResponse
import com.example.controllers.NetworkServer
import com.example.controllers.models.InterruptCause
import com.example.game.Coord
import com.example.game.GameState
import com.example.transport.service.ServerMessageCharacteristic
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Closeable

class BluetoothLEServerWrapper(
    private val clientMessages: ReceiveChannel<ClientResponse>,
    private val serverMessages: ServerMessageCharacteristic,
) : NetworkServer, Closeable {
    override suspend fun sendInterruption(cause: InterruptCause) {
        serverMessages.sendInterruption(cause)
    }

    override suspend fun sendTurn(move: Coord?, state: GameState) {
        serverMessages.sendTurn(move, state)
    }

    override suspend fun getResponse(): ClientResponse {
        return clientMessages.receive()
    }

    override fun close() {

    }
}