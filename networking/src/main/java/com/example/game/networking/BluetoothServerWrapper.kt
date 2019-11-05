package com.example.game.networking

import android.bluetooth.BluetoothSocket
import com.example.game.controllers.*
import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import java.io.IOException
import java.nio.ByteBuffer

class BluetoothServerWrapper(
        private val gameSocket: BluetoothSocket
) : NetworkServer {
    private val fbb = FlatBufferBuilder(1024)
    private val buffer = ByteArray(1024)
    private val bb = ByteBuffer.wrap(buffer)

    override suspend fun getMove(): Result<Coord, Interruption> {
        gameSocket.inputStream.read(bb.array())
        val resp = OppResponse.getRootAsOppResponse(bb)
        bb.clear()
        return when (resp.respType()) {
            OpponentRespMsg.Move -> {
                val m = resp.resp(Move()) as Move
                Success(Coord(m.row().toInt(), m.col().toInt()))
            }
            OpponentRespMsg.GameEvent -> {
                val event = resp.resp(GameEvent()) as GameEvent
                Failure(Interruption(event2Cause(event.type())))
            }
            else -> throw IllegalArgumentException("expected game move, got ${OpponentRespMsg.name(resp.respType().toInt())}")
        }
    }

    override suspend fun sendMove(move: Coord): Interruption? {
        val m = Move.createMove(fbb, move.i.toShort(), move.j.toShort())
        fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.Move, m))

        return try {
            gameSocket.outputStream.write(fbb.sizedByteArray())
            null
        } catch (e: IOException) {
            Interruption(event2Cause(GameEventType.Disonnected))
        }
    }

    override suspend fun sendState(state: GameState): Interruption? {
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
                        Move.createMove(fbb, state.line.start.i.toShort(), state.line.start.j.toShort()),
                        Move.createMove(fbb, state.line.end.i.toShort(), state.line.end.j.toShort())
                )
                GameEvent.createGameEvent(fbb, GameEventType.Win, winLine)
            }
        }

        fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.GameEvent, event))

        return try {
            gameSocket.outputStream.run {
                write(fbb.sizedByteArray())
                flush()
            }
            null
        } catch (e: IOException) {
            Interruption(event2Cause(GameEventType.OppDisconnected))
        }
    }

    override suspend fun sendInterruption(interruption: Interruption): Interruption? {
        GameEvent.startGameEvent(fbb)
        GameEvent.addType(fbb, cause2Event(interruption.cause))
        fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.GameEvent, GameEvent.endGameEvent(fbb)))
        return try {
            gameSocket.outputStream.run {
                write(fbb.sizedByteArray())
                flush()
            }
            gameSocket.close()
            null
        } catch (e: IOException) {
            Interruption(event2Cause(GameEventType.OppDisconnected))
        }
    }

    private fun interruptionEvent(type: Byte): Byte {
        when (type) {
            GameEventType.Win, GameEventType.Tie, GameEventType.OK, GameEventType.GameStarted -> throw IllegalArgumentException("expected interruption event, got ${GameEventType.name(type.toInt())}")
            else -> return type
        }
    }
}