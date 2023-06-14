package com.example.transport.service

import com.example.controllers.*
import com.example.controllers.ClientAction
import com.example.transport.ClientAction as NetAction
import com.example.controllers.models.*
import com.example.controllers.models.Interruption
import com.example.game.*
import com.example.transport.*
import io.grpc.Status
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
    private val respChan: ReceiveChannel<JoinResponse>,
    private val scope: CoroutineScope
) : NetworkClient {
    private lateinit var chan: ReceiveChannel<ServerResponse>

    companion object {
        const val TAG = "GameOpponentWrapper"
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.joinChan2GameChan(chan: ReceiveChannel<JoinResponse>): ReceiveChannel<ServerResponse> =
        produce {
            try {
                for (resp in chan) {
                    val move = if (resp.hasMove()) resp.move.toCoord() else null
                    val state = when (resp.payloadCase) {
                        JoinResponse.PayloadCase.STATUS -> {
                            when (resp.status) {
                                GameStatus.GAME_STATUS_OK -> Continues
                                GameStatus.GAME_STATUS_TIE -> Tie
                                else -> throw IllegalArgumentException("got unexpected status: ${resp.status.name}")
                            }
                        }
                        JoinResponse.PayloadCase.WIN_LINE -> {
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
                        else -> throw IllegalArgumentException("got unexpected payload: ${resp.payloadCase.name}")
                    }
                    send(Response(move, state))
                }
                println("end of iterator")
                close()
            } catch (t: Throwable) {

                val cause = if (Status.fromThrowable(t).code == Status.INTERNAL.code) {
                    val md = Status.trailersFromThrowable(t)
                    requireNotNull(md) { "metadata must be initialized" }
                    md.toInterruption().cause
                } else {
                    InterruptCause.Disconnected
                }
                close(InterruptionException(cause))
                /*
                val status = io.grpc.protobuf.StatusProto.fromThrowable(t)
                val cause = if (status == null) {
                    println("Throwable: $t")
                    //throw IllegalArgumentException("unexpected behaviour")
                    InterruptCause.Disconnected
                } else {
                    status.toInterruption().cause
                }
                close(InterruptionException(cause))*/
            }
        }

    @ExperimentalCoroutinesApi
    fun joinGame(gameID: Int): Flow<GameCreationStatus> {
        val req = joinRequest {
            this.gameId = gameID
        }

        val state = Channel<GameCreationStatus>(Channel.CONFLATED)

        scope.launch {
            reqChan.send(OppReq(req))
            try {
                val resp = respChan.receive()
                if (resp.status != GameStatus.GAME_STATUS_GAME_STARTED) {
                    val startedName = GameStatus.GAME_STATUS_GAME_STARTED.name
                    val gotName = resp.status.name
                    throw IllegalStateException("expected $startedName: got $gotName")
                }

                chan = joinChan2GameChan(respChan)
                //Log.d(TAG, "Got GameStart Event")
                state.send(Created(this@GameOpponentWrapper))

            } catch (e: io.grpc.StatusRuntimeException) {
                println("grpc error: ${e.message}")
                state.send(CreationFailure)
                //Log.e(TAG, "got error: $e")
            }
            println("before closing state chan")
            state.close()
        }

        return state.consumeAsFlow()
    }

    override suspend fun getResponse(): Response {
        chan.receive().let {
            return when (it) {
                is Response -> it
                is Interruption -> throw InterruptionException(it.cause)
            }
        }
    }

    override suspend fun sendMove(move: Coord) {
        reqChan.send(OppReq(joinRequest {
            this.move = move {
                row = move.row
                col = move.col
            }
        }))
    }

    override suspend fun sendAction(action: PlayerAction) {
        reqChan.send(OppAction(action))
    }
}