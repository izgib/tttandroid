package com.example.transport

import com.example.controllers.*
import com.example.controllers.models.InterruptCause
import com.example.controllers.models.InterruptionException
import com.example.game.*
import com.example.transport.extensions.*
import kotlinx.coroutines.*
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import com.example.controllers.ClientAction as CAction

//This class represent
class BluetoothServerWrapper(
    private val scope: CoroutineScope,
    private val connection: ConnectionWrapper,
) : NetworkServer, Closeable {
    private val inputStream: InputStream = connection.inputStream
    private val outputStream: OutputStream = connection.outputStream

    //private val bb = ByteBuffer.allocate(256) //.order(ByteOrder.LITTLE_ENDIAN)

    suspend fun sendParams(settings: GameSettings) {
        println("sending game params")
        val params = bluetoothCreatorMsg {
            params = gameParams {
                rows = settings.rows
                cols = settings.cols
                win = settings.win
                mark = settings.creatorMark.toMarkType()
            }
        }
        try {
            params.writeDelimitedTo(outputStream)
        } catch (e: IOException) {
            println("sending params: ${e.message}")
            close()
            throw InterruptionException(InterruptCause.Disconnected)
        }
    }

    override suspend fun sendTurn(move: Coord?, state: GameState) {
        val turn = turnToCreatorMsg(move, state)

        try {
            turn.writeDelimitedTo(outputStream)
        } catch (e: IOException) {
            println("sending turn: ${e.message}")
            throw InterruptionException(InterruptCause.Disconnected)
        }
    }


    override suspend fun getResponse(): ClientResponse {
        return runInterruptible(Dispatchers.IO + scope.coroutineContext) {
            ClientMessage.parseDelimitedFrom(inputStream)
        }.toClientResponse()
    }

    override suspend fun sendInterruption(cause: InterruptCause) {
        println("sending interruption")
        try {
            bluetoothCreatorMsg {
                this.cause = cause.toStopCause()
            }.writeDelimitedTo(outputStream)
        } catch (e: IOException) {
            throw InterruptionException(InterruptCause.Leave)
        } finally {
            close()
        }
    }


    override fun close() {
        println("closing")
        connection.close()
    }
}

fun turnToCreatorMsg(move: Coord?, state: GameState): BluetoothCreatorMsg {
    return bluetoothCreatorMsg {
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
}