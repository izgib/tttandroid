package com.example.game.networking

import com.example.game.controllers.Created
import com.example.game.controllers.CreationFailure
import com.example.game.controllers.GameCreationStatus
import com.example.game.controllers.NetworkClient
import com.example.game.controllers.models.*
import com.example.game.domain.game.*
import com.example.game.networking.ext.toInterruption
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch


class GameOpponentWrapper(
        private val reqChan: Channel<OpponentRequest>,
        private val respChan: ReceiveChannel<OppResponse>,
        private val scope: CoroutineScope
) : NetworkClient {
    private val fbb = FlatBufferBuilder(1024)
    private lateinit var chan: ReceiveChannel<ServerResponse>

    companion object {
        const val TAG = "GameOpponentWrapper"
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.joinChan2GameChan(chan: ReceiveChannel<OppResponse>): ReceiveChannel<ServerResponse> = produce {
        try {
            for (resp in chan) {
                send(when (resp.respType()) {
                    OpponentRespMsg.Move -> {
                        val move = resp.resp(com.example.game.networking.i9e.Move()) as Move
                        GameMove(move.row().toInt(), move.col().toInt())
                    }
                    OpponentRespMsg.GameEvent -> {
                        val event = resp.resp(com.example.game.networking.i9e.GameEvent()) as GameEvent
                        when (event.type()) {
                            GameEventType.OK -> State(Continues)
                            GameEventType.Tie -> State(Tie)
                            GameEventType.Win -> {
                                val winLine = event.followUp()!!
                                val start = winLine.start()!!
                                val end = winLine.end()!!
                                State(Win(EndWinLine(
                                        Mark.values()[winLine.mark().toInt()],
                                        Coord(start.row().toInt(), start.col().toInt()),
                                        Coord(end.row().toInt(), end.col().toInt())
                                )))
                            }
                            else -> throw IllegalStateException("wrong event")
                        }
                    }
                    else -> throw IllegalStateException("got unexpected type: ${OpponentRespMsg.name(resp.respType().toInt())}")
                })
            }
            println("end of iterator")
            close()
        } catch (t: Throwable) {
            val status = io.grpc.protobuf.StatusProto.fromThrowable(t)
            val cause = if (status == null) {
                println("Throwable: $t")
                //throw IllegalArgumentException("unexpected behaviour")
                InterruptCause.Disconnected
            } else {
                status.toInterruption().cause
            }
            close(InterruptionException(cause))
        }
    }

    @ExperimentalCoroutinesApi
    fun joinGame(gameID: Short): Flow<GameCreationStatus> {
        fbb.finish(OppRequest.createOppRequest(fbb, OpponentReqMsg.GameId,
                GameId.createGameId(fbb, gameID)))
        val gameID = OppRequest.getRootAsOppRequest(fbb.dataBuffer())
        val state = Channel<GameCreationStatus>(Channel.CONFLATED)

        scope.launch {
            println("before sending request")
            reqChan.send(OppReq(gameID))
            println("before getting response")
            try {
                val resp = respChan.receive()
                println("response received")

                if (resp.respType() != OpponentRespMsg.GameEvent) {
                    throw IllegalStateException("expected gameEvent, got ${OpponentRespMsg.name(resp.respType().toInt())}")
                }

                val event = (resp.resp(GameEvent()) as GameEvent)
                if (event.type() != GameEventType.GameStarted) {
                    throw IllegalStateException("expected gameStarted, got ${GameEventType.names[event.type().toInt()]}")
                }

                chan = joinChan2GameChan(respChan)
                //Log.d(TAG, "Got GameStart Event")
                state.send(Created(this@GameOpponentWrapper))

            } catch (e: io.grpc.StatusRuntimeException) {
                println("grpc error")
                state.send(CreationFailure)
                //Log.e(TAG, "got error: $e")
            }
            println("before closing state chan")
            state.close()
        }

        return state.consumeAsFlow()
    }

    override suspend fun getMove(): Coord {
        chan.receive().let {
            return when (it) {
                is GameMove -> Coord(it.row, it.col)
                is Interruption -> throw InterruptionException(it.cause)
                else -> throw IllegalArgumentException("unexpected response")
            }
        }
    }

    override suspend fun sendMove(move: Coord) {
        val m = Move.createMove(fbb, move.row.toShort(), move.col.toShort())
        fbb.finish(OppRequest.createOppRequest(fbb, OpponentReqMsg.Move, m))
        reqChan.send(OppReq(OppRequest.getRootAsOppRequest(fbb.dataBuffer())))
    }

    override suspend fun getState(): GameState {
        chan.receive().let {
            return when (it) {
                is State -> it.state
                is Interruption -> throw InterruptionException(it.cause)
                else -> throw IllegalArgumentException("unexpected response")
            }
        }
    }

    override fun cancelGame() {
        scope.launch {
            reqChan.send(OppDisconnect)
            println("game canceled")
            //Log.d(TAG, "game canceled")
        }
    }


}