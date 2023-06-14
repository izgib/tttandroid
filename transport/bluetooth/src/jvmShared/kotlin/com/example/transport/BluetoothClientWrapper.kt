package com.example.transport

import com.example.controllers.GameSettings
import com.example.controllers.NetworkClient
import com.example.controllers.PlayerAction
import com.example.controllers.models.InterruptCause
import com.example.controllers.models.InterruptionException
import com.example.controllers.models.Response
import com.example.game.*
import com.example.transport.extensions.toCoord
import com.example.transport.extensions.toMark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import com.example.transport.ClientAction as CAction

class BluetoothClientWrapper(
    private val scope: CoroutineScope,
    private val connection: ConnectionWrapper,
) : NetworkClient, Closeable {
    private val inputStream: InputStream = connection.inputStream
    private val outputStream: OutputStream = connection.outputStream

    //private val bb = ByteBuffer.allocate(256) //.order(ByteOrder.LITTLE_ENDIAN)
    //private var needData = true

/*    override suspend fun getMove(): Coord = onBuffer(onResult = { bb ->
        println("move received")
        val msg = ClientMessage.parseFrom(bb)
        when (msg.payloadCase) {
            ClientMessage.PayloadCase.MOVE -> msg.move.run { Coord(row, col) }
            ClientMessage.PayloadCase.ACTION ->
        }
        val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
        return@onBuffer when (resp.msgType) {
            BluetoothCreatorMsg.Move -> {
                val m = resp.msg(Move()) as Move
                Coord(m.row().toInt(), m.col().toInt())
            }
            BluetoothCreatorMsg.GameEvent -> {
                val event = resp.msg(GameEvent()) as GameEvent
                val interruption = Interruption(event2Cause(interruptionEvent(event.type())))
                throw InterruptionException(interruption.cause)
            }
            else -> {
                val mName = BluetoothCreatorMsg.name(BluetoothCreatorMsg.Move.toInt())
                val eName = BluetoothCreatorMsg.name(BluetoothCreatorMsg.GameEvent.toInt())
                val got = BluetoothCreatorMsg.name(resp.msgType.toInt())
                throw IllegalArgumentException("expected $mName or $eName, but got $got")
            }
        }
    }, onError = { e ->
        println("getting move: ${e.message}")
        throw InterruptionException(InterruptCause.Disconnected)
    })*/

/*    override suspend fun getResponse(): Response = onBuffer(onResult = { bb ->
        println("move received")

        val resp = BluetoothCreatorMsg.parseFrom(bb)
        if (resp.hasCause()) throw InterruptionException(resp.cause.toInterruptCause())
        val state = when {
            resp.hasWinLine() -> {
                with(resp.winLine) {
                    Win(
                        EndWinLine(
                            mark.toMark(),
                            startOrNull?.toCoord(),
                            endOrNull?.toCoord()
                        )
                    )
                }
            }
            resp.hasStatus() -> {
                when (resp.status) {
                    GameStatus.GAME_STATUS_OK -> Continues
                    GameStatus.GAME_STATUS_TIE -> Tie
                    else -> throw IllegalStateException(resp.status.toString())
                }
            }
            else -> throw IllegalStateException("unexpected response: $resp")
        }
        return@onBuffer Response(resp.moveOrNull?.toCoord(), state)
    }, onError = { e ->
        println("getting move: ${e.message}")
        throw InterruptionException(InterruptCause.Disconnected)
    })*/

    override suspend fun getResponse(): Response {
        println("response received client")

        val resp = runInterruptible(Dispatchers.IO + scope.coroutineContext) {
            try {
                BluetoothCreatorMsg.parseDelimitedFrom(inputStream)
            } catch (e: IOException) {
                println("getting response: $e")
                e.printStackTrace()
                throw InterruptionException(InterruptCause.Disconnected)
            }
        }

/*        val resp = withContext(Dispatchers.IO + scope.coroutineContext) {
            try {
                BluetoothCreatorMsg.parseDelimitedFrom(inputStream)
            } catch (e: IOException) {
                println("getting response: ${e.message}")
                throw InterruptionException(InterruptCause.Disconnected)
            }
        }*/

        if (resp.hasCause()) {
            println("interruption received")
            throw InterruptionException(resp.cause.toInterruptCause())
        }
        println("received status${if (resp.hasMove()) " with move" else ""}")
        val state = when {
            resp.hasWinLine() -> {
                with(resp.winLine) {
                    Win(
                        EndWinLine(
                            mark.toMark(),
                            startOrNull?.toCoord(),
                            endOrNull?.toCoord()
                        )
                    )
                }
            }
            resp.hasStatus() -> {
                when (resp.status) {
                    GameStatus.GAME_STATUS_OK -> Continues
                    GameStatus.GAME_STATUS_TIE -> Tie
                    else -> throw IllegalStateException(resp.status.toString())
                }
            }
            else -> throw IllegalStateException("unexpected response: $resp")
        }
        return Response(resp.moveOrNull?.toCoord(), state)
    }


    override suspend fun sendMove(move: Coord) {
        println("sending move")
        val move = clientMessage {
            this.move = move {
                row = move.row
                col = move.col
            }
        }
        try {
            move.writeDelimitedTo(outputStream)
        } catch (e: IOException) {
            println("sending move: ${e.message}")
            throw InterruptionException(InterruptCause.Disconnected)
        }
    }

    override suspend fun sendAction(action: PlayerAction) {
        println("sending action")
        val actionMsg = clientMessage {
            this.action = when (action) {
                PlayerAction.Leave -> CAction.CLIENT_ACTION_LEAVE
                PlayerAction.GiveUp -> CAction.CLIENT_ACTION_GIVE_UP
            }
        }
        try {
            actionMsg.writeDelimitedTo(outputStream)
        } catch (e: IOException) {
            println("sending action: ${e.message}")
            throw InterruptionException(InterruptCause.Disconnected)
        }
    }

/*    suspend fun getParams(): GameSettings = onBuffer(onResult = { bb ->
        println("params received")
        val resp = BluetoothCreatorMsg.parseFrom(bb)
        //val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
        return@onBuffer if (resp.hasParams()) {
            resp.params.run {
                GameSettings(rows, cols, win, mark.toMark())
            }
        } else {
            val respName = GameParams.getDescriptor().name
            val got = resp.payloadCase.name
            throw IllegalArgumentException("expected $respName, but got $got")
        }
    }, onError = { e ->
        println("getting params: ${e.message}")
        throw InterruptionException(InterruptCause.Disconnected)
    })*/

    suspend fun getParams(): GameSettings {
        println("params received")
        val resp = try {
            BluetoothCreatorMsg.parseDelimitedFrom(inputStream)
        } catch (e: IOException) {
            println("getting params: ${e.message}")
            throw InterruptionException(InterruptCause.Disconnected)
        }

        return if (resp.hasParams()) {
            resp.params.run {
                GameSettings(rows, cols, win, mark.toMark())
            }
        } else {
            throw IllegalArgumentException("unexpected params")
            /*val respName = GameParams.getDescriptor().name
            val got = resp.payloadCase.name
            throw IllegalArgumentException("expected $respName, but got $got")*/
        }
    }

    /*private suspend inline fun <reified T> onBuffer(
        crossinline onResult: (bb: ByteBuffer) -> T,
        crossinline onError: (t: IOException) -> Nothing
    ): T {
        withContext(Dispatchers.IO) {
            try {
                inputStream.read(bb.array()).let { length ->
                    val arr = bb.array()
                    println("readcount: $length array: ${arr.size} position: ${bb.position()}")
                    println(arr.take(length).joinToString("") { "%02x".format(it) })
                    bb.limit(length)
                }
            } catch (e: IOException) {
                onError(e)
            }
        }

        println("position: ${bb.position()}")
        val result = onResult(bb)
        bb.position(bb.position())
        if (!bb.hasRemaining()) {
            bb.clear()
        }
        return result
    }*/

    override fun close() {
        println("closing client")
        connection.close()
    }
}

fun StopCause.toInterruptCause(): InterruptCause {
    return when (this) {
        StopCause.STOP_CAUSE_LEAVE -> InterruptCause.Leave
        StopCause.STOP_CAUSE_DISCONNECT -> InterruptCause.Disconnected
        StopCause.STOP_CAUSE_INVALID_MOVE -> InterruptCause.InvalidMove
        StopCause.STOP_CAUSE_INTERNAL -> InterruptCause.Internal
        else -> throw IllegalStateException()
    }
}

fun BluetoothCreatorMsg.toResponse(): Response {
    if (hasCause()) throw InterruptionException(cause.toInterruptCause())

    val state = when {
        hasWinLine() -> {
            with(winLine) {
                Win(
                    EndWinLine(
                        mark.toMark(),
                        startOrNull?.toCoord(),
                        endOrNull?.toCoord()
                    )
                )
            }
        }
        hasStatus() -> {
            when (status) {
                GameStatus.GAME_STATUS_OK -> Continues
                GameStatus.GAME_STATUS_TIE -> Tie
                else -> throw IllegalStateException(status.toString())
            }
        }
        else -> throw IllegalStateException("unexpected response: $this")
    }
    return Response(moveOrNull?.toCoord(), state)
}