package com.example.transport

import com.example.controllers.PlayerAction
import com.example.game.*
import com.example.transport.service.toMark
import com.example.transport.service.toMarkType
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver

class PlainGameConfiguratorCreator(
    moves: List<Coord>,
    mark: Mark,
    private val gameID: Int,
    val action: PlayerAction? = null,
) : GameConfiguratorGrpc.GameConfiguratorImplBase() {
    private lateinit var game: Game
    private var initialization = true
    private val moveIterator = moves.iterator()
    private val playerMark = mark

    init {
        if (mark == Mark.Empty) throw IllegalArgumentException("expect cross or nought")
    }

    override fun createGame(responseObserver: StreamObserver<CreateResponse>): StreamObserver<CreateRequest> {
        return object : StreamObserver<CreateRequest> {
            override fun onNext(value: CreateRequest) {
                var reqConsumed = false
                if (initialization) {
                    assert(value.hasParams())
                    reqConsumed = true
                    with(value.params) {
                        assert(mark.toMark() != playerMark) { "wrong player mark" }
                        game = Game(rows, cols, win)
                    }
                    responseObserver.onNext(createResponse {
                        gameId = gameID
                    })
                    responseObserver.onNext(createResponse {
                        status = GameStatus.GAME_STATUS_GAME_STARTED
                    })
                    initialization = false
                }

                val curPlayer = playerMark.mark.toInt()
                while (true) {
                    val move = if (game.otherPlayer() == curPlayer) {
                        if (reqConsumed) return
                        when (value.payloadCase) {
                            CreateRequest.PayloadCase.MOVE -> {
                                assert(moveIterator.hasNext())
                                val expMove = moveIterator.next()
                                val actMove = with(value.move) { Coord(row, col) }
                                assert(actMove == expMove) { "expected $expMove, got $actMove" }
                                reqConsumed = true
                                expMove
                            }
                            CreateRequest.PayloadCase.ACTION -> {
                                when (value.action) {
                                    ClientAction.CLIENT_ACTION_GIVE_UP -> {
                                        assert(action == PlayerAction.GiveUp)
                                        responseObserver.onNext(createResponse {
                                            winLine = winLine {
                                                mark = playerMark.toMarkType()
                                            }
                                        })
                                    }
                                    ClientAction.CLIENT_ACTION_LEAVE ->
                                        assert(action == PlayerAction.Leave)
                                    else -> throw IllegalStateException()
                                }
                                responseObserver.onCompleted()
                                return
                            }
                            else -> throw IllegalStateException()
                        }
                    } else {
                        when {
                            moveIterator.hasNext() -> moveIterator.next()
                            action != null -> {
                                when (action) {
                                    PlayerAction.GiveUp -> {
                                        responseObserver.onNext(createResponse {
                                            winLine = winLine {
                                                mark = (!playerMark).toMarkType()
                                            }
                                        })
                                        responseObserver.onCompleted()
                                    }
                                    PlayerAction.Leave -> {
                                        responseObserver.onError(
                                            interruptionCauseException(
                                                StopCause.STOP_CAUSE_LEAVE
                                            )
                                        )
                                    }
                                }
                                return
                            }
                            else -> throw IllegalStateException()
                        }
                    }
                    if (!game.isValidMove(move)) {
                        responseObserver.onError(
                            interruptionCauseException(
                                StopCause.STOP_CAUSE_INVALID_MOVE
                            )
                        )
                        return
                    }

                    val toSend = game.curPlayer() == curPlayer
                    val state = game.run {
                        moveTo(move)
                        gameState(move)
                    }
                    responseObserver.onNext(createResponse {
                        if (toSend) {
                            this.move = move.toMove()
                        }
                        when (state) {
                            is Continues -> {
                                status = GameStatus.GAME_STATUS_OK
                            }
                            is Tie -> {
                                status = GameStatus.GAME_STATUS_TIE
                            }
                            is Win -> {
                                winLine = winLine {
                                    mark = state.line.mark.toMarkType()
                                    state.line.start?.let { start = it.toMove() }
                                    state.line.end?.let { end = it.toMove() }
                                }
                            }
                        }
                    })
                    if (state !is Continues) {
                        responseObserver.onCompleted()
                        return
                    }
                }
            }

            override fun onError(t: Throwable) {
                println("WTF: $t")
            }

            override fun onCompleted() {
                println("all is good")
            }
        }
    }

    private fun interruptionCauseException(cause: StopCause): StatusRuntimeException {
        return interruption {
            this.cause = cause
        }.toStatusRuntimeException()


        /*val status = Status.newBuilder()
            .setCode(Code.INTERNAL.number)
            .addDetails(interruptInfo.toAnyMessage())
            //.addDetails(interruptInfo.toAnyMessage())
            .addDetails(
                com.google.protobuf.Any.newBuilder()
                    .setValue(interruptInfo.toByteString())
            )
            .build()*/
        //return StatusProto.toStatusRuntimeException(status)
    }
}

fun ICoord.toMove(): Move {
    val r = row
    val c = col
    return move {
        row = r
        col = c
    }
}