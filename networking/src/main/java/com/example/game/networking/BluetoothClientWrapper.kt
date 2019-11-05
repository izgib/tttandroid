package com.example.game.networking

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.game.controllers.*
import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

class BluetoothClientWrapper(private val gameSocket: BluetoothSocket) : NetworkClient, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private val fbb = FlatBufferBuilder(1024)
    private val buffer = ByteArray(1024)
    private val bb = ByteBuffer.wrap(buffer)

    override suspend fun getMove(): Result<Coord, Interruption> {
        gameSocket.inputStream.read(bb.array())
        val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
        bb.clear()
        return when (resp.msgType()) {
            BluetoothCreatorMsg.Move -> {
                val m = resp.msg(Move()) as Move
                Success(Coord(m.row().toInt(), m.col().toInt()))
            }
            BluetoothCreatorMsg.GameEvent -> {
                val event = resp.msg(GameEvent()) as GameEvent
                scope.cancel()
                Failure(
                        Interruption(
                                event2Cause(
                                        interruptionEvent(
                                                event.type()
                                        )
                                )
                        )
                )
            }
            else -> {
                scope.cancel()
                throw IllegalArgumentException("expected game move, got ${BluetoothCreatorMsg.name(resp.msgType().toInt())}")
            }
        }
    }

    override suspend fun sendMove(move: Coord): Interruption? {
        val m = Move.createMove(fbb, move.i.toShort(), move.j.toShort())
        fbb.finish(OppResponse.createOppResponse(fbb, OpponentRespMsg.Move, m))

        return try {
            gameSocket.outputStream.run {
                write(fbb.sizedByteArray())
                flush()
            }
            null
        } catch (e: IOException) {
            scope.cancel()
            Interruption(event2Cause(GameEventType.Disonnected))
        }
    }

    override suspend fun getState(): Result<GameState, Interruption> {
        try {
            gameSocket.inputStream.read(bb.array())
            val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
            bb.clear()

            if (resp.msgType() == BluetoothCreatorMsg.GameEvent) {
                val e = resp.msg(GameEvent()) as GameEvent
                return when (e.type()) {
                    GameEventType.OK -> Success(Continues)
                    GameEventType.Tie -> Success(Tie)
                    GameEventType.Win -> {
                        val winLine = e.followUp()!!
                        val start = winLine.start()!!
                        val end = winLine.end()!!
                        Success(Win(
                                EndWinLine(
                                        Mark.values()[winLine.mark().toInt()],
                                        Coord(start.row().toInt(), start.col().toInt()),
                                        Coord(end.row().toInt(), end.col().toInt())
                                )))
                    }
                    else -> Failure(
                            Interruption(
                                    event2Cause(
                                            interruptionEvent(
                                                    e.type()
                                            )
                                    )
                            )
                    )
                }
            } else {
                scope.cancel()
                throw IllegalArgumentException("expected game event, got ${BluetoothCreatorMsg.name(resp.msgType().toInt())}")
            }
        } catch (e: IOException) {
            scope.cancel()
            return Failure(Interruption(event2Cause(GameEventType.Disonnected)))
        }
    }

    override fun CancelGame() {
        try {
            gameSocket.close()
            scope.cancel()
        } catch (e: IOException) {
            Log.e("BI", "can not close bluetooth socket")
        }
    }

    private fun interruptionEvent(type: Byte): Byte {
        when (type) {
            GameEventType.Win, GameEventType.Tie, GameEventType.OK, GameEventType.GameStarted -> throw IllegalArgumentException("expected interruption event, got ${GameEventType.name(type.toInt())}")
            else -> return type
        }
    }
}