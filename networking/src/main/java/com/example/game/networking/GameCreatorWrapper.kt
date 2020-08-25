package com.example.game.networking

import com.example.game.controllers.*
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
import kotlin.time.ExperimentalTime

class GameCreatorWrapper(
        private val reqChan: Channel<CreatorRequest>,
        private val respChan: ReceiveChannel<CrResponse>,
        private val scope: CoroutineScope
) : GameInitializer, NetworkClient {

    private val fbb = FlatBufferBuilder(1024)
    private lateinit var chan: ReceiveChannel<ServerResponse>

    companion object {
        const val TAG = "GameCreatorWrapper"
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.creatorChan2GameChan(chan: ReceiveChannel<CrResponse>): ReceiveChannel<ServerResponse> = produce {
        try {
            for (resp in chan) {
                println("got response: ${CreatorRespMsg.name(resp.respType().toInt())}")
                send(when (resp.respType()) {
                    CreatorRespMsg.Move -> {
                        val move = resp.resp(Move()) as Move
                        println("here in move")
                        val it = GameMove(move.row().toInt(), move.col().toInt())
                        println(it)
                        it
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
                            else -> throw IllegalStateException("wrong event")
                        }
                    }
                    else -> throw IllegalArgumentException("got unexpected type: ${CreatorRespMsg.name(resp.respType().toInt())}")
                })
            }
            println("end of iterator")
            close()
        } catch (t: Throwable) {
            val status = io.grpc.protobuf.StatusProto.fromThrowable(t)
            println("Throwable: $t")
            val cause = if (status == null) {
                println("disc")
                InterruptCause.Disconnected
            } else {
                println("other")
                status.toInterruption().cause
            }
            close(InterruptionException(cause))
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    override fun sendCreationRequest(settings: GameSettings): Flow<GameCreationStatus> {
        fbb.finish(CrRequest.createCrRequest(fbb, CreatorReqMsg.GameParams,
                with(settings) { GameParams.createGameParams(fbb, rows.toShort(), cols.toShort(), win.toShort(), creatorMark.mark) }))
        val req = CrRequest.getRootAsCrRequest(fbb.dataBuffer())

        val state = Channel<GameCreationStatus>(Channel.CONFLATED)
        scope.launch {
            try {
                println("before sending request")
                reqChan.send(CrReq(req))

                println("before receiving response")
                var resp = respChan.receive()
                println("response received: GameID")

                if (resp.respType() != CreatorRespMsg.GameId) {
                    throw IllegalStateException("expected gameID, got ${CreatorRespMsg.name(resp.respType().toInt())}")
                }
                val gameID = resp.resp(GameId()) as GameId
                state.send(GameID(gameID.ID()))

                resp = respChan.receive()
                println("response received: Game Start token")

                if (resp.respType() != CreatorRespMsg.GameEvent) {
                    throw IllegalStateException("expected gameEvent, got ${CreatorRespMsg.name(resp.respType().toInt())}")
                }
                val event = resp.resp(GameEvent()) as GameEvent
                if (event.type() != GameEventType.GameStarted) {
                    throw IllegalStateException("expected gameStarted, got ${GameEventType.names[event.type().toInt()]}")
                }
            } catch (e: io.grpc.StatusRuntimeException) {
                println("grpc error")
                state.send(CreationFailure)
                state.close()
                return@launch
            }

            chan = creatorChan2GameChan(respChan)
            state.send(Created(this@GameCreatorWrapper))
            state.close()
        }

        return state.consumeAsFlow()
    }

    override suspend fun getMove(): Coord {
        return when (val resp = chan.receive()) {
            is GameMove -> Coord(resp.row, resp.col)
            is Interruption -> throw InterruptionException(resp.cause)
            else -> throw IllegalArgumentException("unexpected response")
        }
    }

    override suspend fun sendMove(move: Coord) {
        val m = Move.createMove(fbb, move.row.toShort(), move.col.toShort())
        fbb.finish(CrRequest.createCrRequest(fbb, CreatorReqMsg.Move, m))
        reqChan.send(CrReq(CrRequest.getRootAsCrRequest(fbb.dataBuffer())))
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
            reqChan.send(CrDisconnect)
            println("game canceled")
            //Log.d(TAG, "game canceled")
        }
    }
}