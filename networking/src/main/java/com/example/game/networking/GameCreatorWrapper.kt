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

class GameCreatorWrapper(
        private val reqChan: Channel<CreatorRequest>,
        private val respChan: ReceiveChannel<CrResponse>
) : GameInitializer, NetworkClient {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val fbb = FlatBufferBuilder(1024)
    private lateinit var chan: ReceiveChannel<ServerResponse>

    companion object {
        const val TAG = "GameCreatorWrapper"
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.creatorChan2GameChan(chan: ReceiveChannel<CrResponse>): ReceiveChannel<ServerResponse> = produce {
        try {
            for (resp in chan) {
                send(when (resp.respType()) {
                    CreatorRespMsg.Move -> {
                        val move = resp.resp(Move()) as Move
                        GameMove(move.row().toInt(), move.col().toInt())
                    }
                    CreatorRespMsg.GameEvent -> {
                        val event = resp.resp(GameEvent()) as GameEvent
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
                    else -> throw IllegalArgumentException("got unexpected type: ${CreatorRespMsg.name(resp.respType().toInt())}")
                })
            }
        } catch (e: io.grpc.StatusException) {
            send(Interruption(InterruptCause.Disconnected))
            close()
        }
    }

    @ExperimentalCoroutinesApi
    override fun sendCreationRequest(rows: Int, cols: Int, win: Int, mark: Mark): ReceiveChannel<GameCreationStatus> {
        fbb.finish(CrRequest.createCrRequest(fbb, CreatorReqMsg.GameParams,
                GameParams.createGameParams(fbb, rows.toShort(), cols.toShort(), win.toShort(), mark.mark)))
        val req = CrRequest.getRootAsCrRequest(fbb.dataBuffer())

        return scope.produce(capacity = Channel.CONFLATED) {
            try {
                reqChan.send(CrReq(req))

                var resp = respChan.receive()
                if (resp.respType() == CreatorRespMsg.GameId) {
                    val gameID = resp.resp(GameId()) as GameId
                    send(GameID(gameID.ID()))

                    resp = respChan.receive()
                    Log.d(TAG, "response received")
                    if (resp.respType() == CreatorRespMsg.GameEvent) {
                        val event = resp.resp(GameEvent()) as GameEvent
                        if (event.type() == GameEventType.GameStarted) {
                            send(Created)
                            chan = creatorChan2GameChan(respChan)
                            close()
                            return@produce
                        } else {
                            throw IllegalStateException("expected gameStarted, got ${GameEventType.names[event.type().toInt()]}")
                        }
                    } else {
                        throw IllegalStateException("expected gameEvent, got ${CreatorRespMsg.name(resp.respType().toInt())}")
                    }

                } else {
                    throw IllegalStateException("expected gameID, got ${CreatorRespMsg.name(resp.respType().toInt())}")
                }
            } catch (e: ClosedReceiveChannelException) {

            } catch (e: io.grpc.StatusRuntimeException) {
                Log.e(TAG, "got error: $e")
                send(CreationFailure)
            }
            close()
        }
    }

    override fun CancelGame() {
        scope.launch {
            reqChan.send(CrDisconnect)
            Log.d(TAG, "game canceled")
        }
    }

    override suspend fun getMove(): Result<Coord, Interruption> {
        chan.receive().let {
            return when (it) {
                is GameMove -> Success(Coord(it.i, it.j))
                is Interruption -> Failure(it)
                else -> throw IllegalArgumentException("unexpected response")
            }
        }
    }

    override suspend fun sendMove(move: Coord): Interruption? {
        val m = Move.createMove(fbb, move.i.toShort(), move.j.toShort())
        fbb.finish(CrRequest.createCrRequest(fbb, CreatorReqMsg.Move, m))
        reqChan.send(CrReq(CrRequest.getRootAsCrRequest(fbb.dataBuffer())))

        return null
    }

    override suspend fun getState(): Result<GameState, Interruption> {
        chan.receive().let {
            return when (it) {
                is State -> Success(it.state)
                is Interruption -> Failure(it)
                else -> throw IllegalArgumentException("unexpected response")
            }
        }
    }
}