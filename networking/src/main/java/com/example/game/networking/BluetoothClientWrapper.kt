package com.example.game.networking

import android.util.Log
import com.example.game.controllers.NetworkClient
import com.example.game.controllers.models.InterruptCause
import com.example.game.controllers.models.Interruption
import com.example.game.controllers.models.InterruptionException
import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.coroutines.CoroutineContext

class BluetoothClientWrapper(private val inputStream: InputStream, private val outputStream: OutputStream) : NetworkClient, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Unconfined
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private val fbb = FlatBufferBuilder(1024)
    private val bb = ByteBuffer.wrap(ByteArray(1024))

    override suspend fun getMove(): Coord {
        withContext(Dispatchers.IO) {
            inputStream.read(bb.array())
        }
        val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
        bb.clear()
        return when (resp.msgType()) {
            BluetoothCreatorMsg.Move -> {
                val m = resp.msg(Move()) as Move
                Coord(m.row().toInt(), m.col().toInt())
            }
            BluetoothCreatorMsg.GameEvent -> {
                val event = resp.msg(GameEvent()) as GameEvent
                scope.cancel()
                val interruption =
                        Interruption(
                                event2Cause(
                                        interruptionEvent(
                                                event.type()
                                        )
                                )
                        )
                throw InterruptionException(interruption.cause)
            }
            else -> {
                throw IllegalArgumentException("expected game move, got ${BluetoothCreatorMsg.name(resp.msgType().toInt())}")
            }
        }
    }

    override suspend fun sendMove(move: Coord) {
        val m = Move.createMove(fbb, move.row.toShort(), move.col.toShort())
        fbb.finish(OppResponse.createOppResponse(fbb, OpponentRespMsg.Move, m))

        try {
            outputStream.run {
                withContext(Dispatchers.IO) {
                    write(fbb.sizedByteArray())
                    flush()
                }
            }
        } catch (e: IOException) {
            scope.cancel()
            throw InterruptionException(InterruptCause.OppLeave)
        }
    }

    override suspend fun getState(): GameState {
        try {
            withContext(Dispatchers.IO) {
                inputStream.read(bb.array())
            }
            val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
            bb.clear()

            if (resp.msgType() == BluetoothCreatorMsg.GameEvent) {
                val e = resp.msg(GameEvent()) as GameEvent
                return when (e.type()) {
                    GameEventType.OK -> Continues
                    GameEventType.Tie -> Tie
                    GameEventType.Win -> {
                        val winLine = e.followUp()!!
                        val start = winLine.start()!!
                        val end = winLine.end()!!
                        Win(
                                EndWinLine(
                                        Mark.values()[winLine.mark().toInt()],
                                        Coord(start.row().toInt(), start.col().toInt()),
                                        Coord(end.row().toInt(), end.col().toInt())
                                ))
                    }
                    else -> {
                        val interruption = Interruption(
                                event2Cause(
                                        interruptionEvent(
                                                e.type()
                                        )
                                )
                        )
                        throw InterruptionException(interruption.cause)
                    }
                }
            } else {
                scope.cancel()
                throw IllegalArgumentException("expected game event, got ${BluetoothCreatorMsg.name(resp.msgType().toInt())}")
            }
        } catch (e: IOException) {
            scope.cancel()
            throw InterruptionException(InterruptCause.OppLeave)
        }
    }

    override fun cancelGame() {
        try {
            outputStream.close()
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