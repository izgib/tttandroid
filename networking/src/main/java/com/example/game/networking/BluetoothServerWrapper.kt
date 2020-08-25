package com.example.game.networking

import android.bluetooth.BluetoothSocket
import com.example.game.controllers.NetworkServer
import com.example.game.controllers.models.InterruptCause
import com.example.game.controllers.models.Interruption
import com.example.game.controllers.models.InterruptionException
import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer

class BluetoothServerWrapper(
        private val gameSocket: BluetoothSocket
) : NetworkServer {
    private val fbb = FlatBufferBuilder(1024)
    private val buffer = ByteArray(1024)
    private val bb = ByteBuffer.wrap(buffer)

    override suspend fun getMove(): Coord {
        withContext(Dispatchers.IO) {
            gameSocket.inputStream.read(bb.array())
        }
        val resp = OppResponse.getRootAsOppResponse(bb)
        bb.clear()
        when (resp.respType()) {
            OpponentRespMsg.Move -> {
                val m = resp.resp(Move()) as Move
                return Coord(m.row().toInt(), m.col().toInt())
            }
            OpponentRespMsg.GameEvent -> {
                val event = resp.resp(GameEvent()) as GameEvent
                throw InterruptionException(event2Cause(event.type()))
            }
            else -> throw IllegalArgumentException("expected game move, got ${OpponentRespMsg.name(resp.respType().toInt())}")
        }
    }

    override suspend fun sendMove(move: Coord) {
        val m = Move.createMove(fbb, move.row.toShort(), move.col.toShort())
        fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.Move, m))

        try {
            withContext(Dispatchers.IO) {
                gameSocket.outputStream.write(fbb.sizedByteArray())
            }
        } catch (e: IOException) {
            throw InterruptionException(InterruptCause.Disconnected)
        }
    }

    override suspend fun sendState(state: GameState) {
        val event = when (state) {
            is Continues, Tie -> {
                GameEvent.startGameEvent(fbb)
                val type = if (state == Continues) {
                    GameEventType.OK
                } else {
                    GameEventType.Tie
                }
                GameEvent.addType(fbb, type)
                GameEvent.endGameEvent(fbb)
            }
            is Win -> {
                val winLine = WinLine.createWinLine(
                        fbb,
                        state.line.mark.mark,
                        Move.createMove(fbb, state.line.start.row.toShort(), state.line.start.col.toShort()),
                        Move.createMove(fbb, state.line.end.row.toShort(), state.line.end.col.toShort())
                )
                GameEvent.createGameEvent(fbb, GameEventType.Win, winLine)
            }
        }

        fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.GameEvent, event))

        try {
            gameSocket.outputStream.run {
                withContext(Dispatchers.IO) {
                    write(fbb.sizedByteArray())
                    flush()
                }
            }
        } catch (e: IOException) {
            throw InterruptionException(InterruptCause.OppLeave)
        }
    }

    override suspend fun sendInterruption(interruption: Interruption) {
        GameEvent.startGameEvent(fbb)
        GameEvent.addType(fbb, cause2Event(interruption.cause))
        fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.GameEvent, GameEvent.endGameEvent(fbb)))
        try {
            withContext(Dispatchers.IO) {
                gameSocket.outputStream.run {
                    write(fbb.sizedByteArray())
                    flush()
                }
                gameSocket.close()
            }
        } catch (e: IOException) {
            throw InterruptionException(InterruptCause.OppLeave)
        }
    }

    private fun interruptionEvent(type: Byte): Byte {
        when (type) {
            GameEventType.Win, GameEventType.Tie, GameEventType.OK, GameEventType.GameStarted -> throw IllegalArgumentException("expected interruption event, got ${GameEventType.name(type.toInt())}")
            else -> return type
        }
    }
}