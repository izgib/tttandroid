package com.example.transport

import com.example.controllers.GameSettings
import com.example.controllers.PlayerAction
import com.example.game.*
import com.example.transport.service.toMarkType
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver

class PlainGameConfiguratorJoiner(
    settings: GameSettings, moves: List<Coord>,
    private val gameID: Int,
    val action: PlayerAction? = null,
) : GameConfiguratorGrpc.GameConfiguratorImplBase() {
    val game = Game(settings.rows, settings.cols, settings.win)
    private var initialization = true
    private val moveIterator = moves.iterator()
    private val playerMark: Mark

    init {
        if (settings.creatorMark == Mark.Empty) throw IllegalArgumentException("expect cross or nought")
        playerMark = settings.creatorMark
    }

    override fun joinGame(responseObserver: StreamObserver<JoinResponse>): StreamObserver<JoinRequest> {
        return object : StreamObserver<JoinRequest> {
            override fun onNext(value: JoinRequest) {
                var reqConsumed = false
                if (initialization) {
                    assert(value.hasGameId())
                    assert(value.gameId == gameID) { "wrong game id" }
                    reqConsumed = true

                    responseObserver.onNext(joinResponse {
                        status = GameStatus.GAME_STATUS_GAME_STARTED
                    })
                    initialization = false
                }

                val curPlayer = playerMark.mark.toInt()
                while (true) {
                    val move = if (game.otherPlayer() == curPlayer) {
                        if (reqConsumed) return

                        when (value.payloadCase) {
                            JoinRequest.PayloadCase.MOVE -> {
                                assert(moveIterator.hasNext())
                                val expMove = moveIterator.next()
                                val actMove = with(value.move) { Coord(row, col) }
                                assert(actMove == expMove) { "expected $expMove, got $actMove" }
                                reqConsumed = true
                                expMove
                            }
                            JoinRequest.PayloadCase.ACTION -> {
                                assert(action != null) { "not expected player action" }
                                when (value.action) {
                                    ClientAction.CLIENT_ACTION_GIVE_UP -> {
                                        assert(action == PlayerAction.GiveUp)
                                        responseObserver.onNext(joinResponse {
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
                                        responseObserver.onNext(joinResponse {
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
                    }

                    val toSend = game.curPlayer() == curPlayer
                    val state = game.run {
                        moveTo(move)
                        gameState(move)
                    }
                    responseObserver.onNext(joinResponse {
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

            override fun onError(t: Throwable?) {
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
            .setCode(Code.UNKNOWN.number)
            .addDetails(interruptInfo.toAnyMessage())
            .build()
        return StatusProto.toStatusRuntimeException(status)*/
    }
}