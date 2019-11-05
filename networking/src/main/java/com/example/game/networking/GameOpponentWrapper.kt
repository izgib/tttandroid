package com.example.game.networking

import android.util.Log
import com.example.game.controllers.*
import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch


class GameOpponentWrapper(
        private val reqChan: Channel<OpponentRequest>,
        private val respChan: ReceiveChannel<OppResponse>
) : NetworkClient {
    private val scope = CoroutineScope(Dispatchers.Default)
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
                            else -> Interruption(event2Cause(event.type()))
                        }
                    }
                    else -> throw IllegalStateException("got unexpected type: ${OpponentRespMsg.name(resp.respType().toInt())}")
                })
            }
        } catch (e: io.grpc.StatusException) {
            send(Interruption(InterruptCause.Disconnected))
            close()
        }
    }

    @ExperimentalCoroutinesApi
    fun joinGame(gameID: Short): ReceiveChannel<GameCreationStatus> {
        fbb.finish(OppRequest.createOppRequest(fbb, OpponentReqMsg.GameId,
                GameId.createGameId(fbb, gameID)))
        val gID = OppRequest.getRootAsOppRequest(fbb.dataBuffer())

        return scope.produce(capacity = Channel.CONFLATED) {
            reqChan.send(OppReq(gID))

            try {
                val resp = respChan.receive()
                if (resp.respType() == OpponentRespMsg.GameEvent) {
                    val event = (resp.resp(GameEvent()) as GameEvent)
                    if (event.type() == GameEventType.GameStarted) {
                        Log.d(TAG, "Got GameStart Event")
                        send(Created)
                        chan = joinChan2GameChan(respChan)
                        close()
                        return@produce
                    } else {
                        throw IllegalStateException("expected gameStarted, got ${GameEventType.names[event.type().toInt()]}")
                    }
                } else {
                    throw IllegalStateException("expected gameEvent, got ${OpponentRespMsg.name(resp.respType().toInt())}")
                }
            } catch (e: ClosedReceiveChannelException) {
                Log.e(TAG, "got error: $e")
                send(CreationFailure)
            } catch (e: io.grpc.StatusException) {
                Log.e(TAG, "got error: $e")
                send(CreationFailure)
            }
            close()
        }
    }

    override suspend fun getMove(): Result<Coord, Interruption> {
        chan.receive().let {
            return when (it) {
                is GameMove -> Success(Coord(it.i, it.j))
                is Interruption -> Failure(it)
                else -> throw IllegalStateException("unexpected response")
            }
        }
    }

    override suspend fun sendMove(move: Coord): Interruption? {
        val m = Move.createMove(fbb, move.i.toShort(), move.j.toShort())
        fbb.finish(OppRequest.createOppRequest(fbb, OpponentReqMsg.Move, m))
        reqChan.send(OppReq(OppRequest.getRootAsOppRequest(fbb.dataBuffer())))
        return null
    }

    override suspend fun getState(): Result<GameState, Interruption> {
        chan.receive().let {
            return when (it) {
                is State -> Success(it.state)
                is Interruption -> Failure(it)
                else -> throw java.lang.IllegalArgumentException("unexpected response")
            }
        }
    }

    override fun CancelGame() {
        scope.launch {
            reqChan.send(OppDisconnect)
            Log.d(TAG, "game canceled")
        }
    }


}