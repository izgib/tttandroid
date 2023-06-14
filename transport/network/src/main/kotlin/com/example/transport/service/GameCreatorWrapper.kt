package com.example.transport.service

import com.example.controllers.*
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

class GameCreatorWrapper(
    private val reqChan: Channel<CreatorRequest>,
    private val respChan: ReceiveChannel<CreateResponse>,
    private val scope: CoroutineScope
) : GameInitializer, NetworkClient {

    private lateinit var chan: ReceiveChannel<ServerResponse>

    companion object {
        const val TAG = "GameCreatorWrapper"
    }

    @ExperimentalCoroutinesApi
    private fun CoroutineScope.creatorChan2GameChan(chan: ReceiveChannel<CreateResponse>): ReceiveChannel<ServerResponse> =
        produce {
            try {
                for (resp in chan) {
                    val move = if (resp.hasMove()) resp.move.toCoord() else null
                    val state = when (resp.payloadCase) {
                        CreateResponse.PayloadCase.STATUS -> {
                            when (resp.status) {
                                GameStatus.GAME_STATUS_OK -> Continues
                                GameStatus.GAME_STATUS_TIE -> Tie
                                else -> throw IllegalArgumentException("got unexpected status: ${resp.status.name}")
                            }
                        }
                        CreateResponse.PayloadCase.WIN_LINE -> {
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
                close()
            } catch (t: Throwable) {
                println("Throwable: $t")

                val cause = if (Status.fromThrowable(t).code == Status.INTERNAL.code) {
                    val md = Status.trailersFromThrowable(t)
                    requireNotNull(md) { "metadata must be initialized" }
                    md.toInterruption().cause
                } else {
                    InterruptCause.Disconnected
                }
                close(InterruptionException(cause))


/*                val status = io.grpc.protobuf.StatusProto.fromThrowable(t)
                val cause = if (status == null) {
                    println("disc")
                    InterruptCause.Disconnected
                } else {
                    println("other")
                    val md = Status.trailersFromThrowable(t)
                    status.toInterruption().cause
                }
                close(InterruptionException(cause))*/
            }
        }

    @ExperimentalCoroutinesApi
    override fun sendCreationRequest(settings: GameSettings): Flow<GameCreationStatus> {
        val req = createRequest {
            params = gameParams {
                rows = settings.rows
                cols = settings.cols
                win = settings.win
                mark = when (settings.creatorMark) {
                    Mark.Cross -> MarkType.MARK_TYPE_CROSS
                    Mark.Nought -> MarkType.MARK_TYPE_NOUGHT
                    else -> throw IllegalArgumentException("unexpected value: ${settings.creatorMark.name}")
                }
            }
        }
        val state = Channel<GameCreationStatus>(Channel.CONFLATED)
        scope.launch {
            try {
                reqChan.send(CrReq(req))

                var resp = respChan.receive()
                if (!resp.hasGameId()) {
                    throw IllegalStateException(
                        "expected ${CreateResponse.PayloadCase.GAME_ID.name}, got ${resp.payloadCase.name}"
                    )
                }
                state.send(GameID(resp.gameId))
                //Log.d(TAG, "game canceled")
                println("ID: ${resp.gameId}")

                resp = respChan.receive()
                if (resp.status != GameStatus.GAME_STATUS_GAME_STARTED) {
                    val startedName = GameStatus.GAME_STATUS_GAME_STARTED.name
                    val gotName = resp.status.name
                    throw IllegalStateException("expected $startedName: got $gotName")
                }
            } catch (e: io.grpc.StatusRuntimeException) {
                println("grpc error: ${e.message}")
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

    override suspend fun getResponse(): Response {
        return when (val resp = chan.receive()) {
            is Response -> resp
            is Interruption -> throw InterruptionException(resp.cause)
        }
    }

    override suspend fun sendMove(move: Coord) {
        reqChan.send(CrReq(createRequest {
            this.move = move {
                row = move.row
                col = move.col
            }
        }))
    }

    override suspend fun sendAction(action: PlayerAction) {
        reqChan.trySend(CrAction(action))
    }

    override fun cancelGame() {
        scope.launch {
            sendAction(PlayerAction.Leave)
            println("game canceled")
            //Log.d(TAG, "game canceled")
        }
    }
}

fun MarkType.toMark(): Mark = when (this) {
    MarkType.MARK_TYPE_CROSS -> Mark.Cross
    MarkType.MARK_TYPE_NOUGHT -> Mark.Nought
    MarkType.MARK_TYPE_UNSPECIFIED -> Mark.Empty
    MarkType.UNRECOGNIZED -> throw IllegalArgumentException("unexpected value")
}

fun Mark.toMarkType(): MarkType = when (this) {
    Mark.Cross -> MarkType.MARK_TYPE_CROSS
    Mark.Nought -> MarkType.MARK_TYPE_NOUGHT
    Mark.Empty -> MarkType.MARK_TYPE_UNSPECIFIED
}

fun Move.toCoord() = Coord(row, col)
