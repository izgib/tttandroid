package com.example.game.networking

import com.example.game.controllers.GameSettings
import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver

class PlainGameConfiguratorJoiner(settings: GameSettings, private val moves: Array<Coord>, private val gameID: Short) : GameConfiguratorGrpc.GameConfiguratorImplBase() {
    private val fbb = FlatBufferBuilder(1024)
    val game = Game(settings.rows, settings.cols, settings.win)
    private var initialization = true
    private val moveIterator = moves.iterator()
    private val playerMark: Int

    init {
        if (settings.creatorMark == Mark.Empty) throw IllegalArgumentException("expect cross or nought")
        playerMark = settings.creatorMark.mark.toInt()
    }

    override fun joinGame(responseObserver: StreamObserver<OppResponse>): StreamObserver<OppRequest> {
        return object : StreamObserver<OppRequest> {
            override fun onNext(value: OppRequest) {
                var consumed = false
                if (initialization) {
                    assert(value.reqType() == OpponentReqMsg.GameId) { "wrong event" }
                    val gameId = (value.req(GameId()) as GameId)
                    assert(gameId.ID() == gameID) { "wrong game id" }
                    consumed = true

                    GameEvent.startGameEvent(fbb)
                    GameEvent.addType(fbb, GameEventType.GameStarted)
                    fbb.finish(
                            OppResponse.createOppResponse(fbb, OpponentRespMsg.GameEvent,
                                    GameEvent.endGameEvent(fbb))
                    )
                    responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
                    initialization = false
                }

                while (true) {
                    val move: Coord
                    var curPlayerCheated: Boolean? = null
                    if (game.curPlayer() == playerMark) {
                        /*if (!moveIterator.hasNext()) {
                            responseObserver.onError(interruptionCauseException(
                                    ErrorDetails.InterruptionCause.LEAVE)
                            )
                            return
                        }*/
                        move = moveIterator.next()
                        if (!game.isValidMove(move)) {
                            curPlayerCheated = true
                        } else {
                            fbb.finish(OppResponse.createOppResponse(fbb, OpponentRespMsg.Move, Move.createMove(fbb, move.row.toShort(), move.col.toShort())))
                            responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
                        }
                    } else {
                        if (consumed) return
                        move = moveIterator.next()
                        assert(value.reqType() == OpponentReqMsg.Move) { "wrong package" }
                        val received = (value.req(Move()) as Move)
                        consumed = true
                        val gameCoord = Coord(received.row().toInt(), received.col().toInt())
                        assert(gameCoord == move) { "wrong move" }
                        if (!game.isValidMove(move)) {
                            curPlayerCheated = false
                        }
                    }
                    curPlayerCheated?.let { curPlayer ->
                        ErrorDetails.InterruptionInfo.newBuilder().setCause(ErrorDetails.InterruptionCause.OPP_INVALID_MOVE).build()
                        val cause = ErrorDetails.InterruptionInfo.newBuilder().setCause(if (curPlayer) {
                            ErrorDetails.InterruptionCause.OPP_INVALID_MOVE
                        } else {
                            ErrorDetails.InterruptionCause.INVALID_MOVE
                        }).build()
                        val status = Status.newBuilder()
                                .setCode(Code.UNKNOWN.number)
                                .addDetails(Any.pack(cause))
                                .build()
                        responseObserver.onError(StatusProto.toStatusRuntimeException(status))
                        return
                    }
                    val state = game.run {
                        moveTo(move)
                        gameState(move)
                    }

                    val eventOffset = when (state) {
                        is Continues -> {
                            GameEvent.startGameEvent(fbb)
                            GameEvent.addType(fbb, GameEventType.OK)
                            GameEvent.endGameEvent(fbb)
                        }
                        is Tie -> {
                            GameEvent.startGameEvent(fbb)
                            GameEvent.addType(fbb, GameEventType.Tie)
                            GameEvent.endGameEvent(fbb)
                        }
                        is Win -> {
                            val start = with(state.line.start) {
                                Move.createMove(fbb, row.toShort(), col.toShort())
                            }
                            val end = with(state.line.end) {
                                Move.createMove(fbb, row.toShort(), col.toShort())
                            }
                            val winLine = WinLine.createWinLine(fbb, state.line.mark.mark, start, end)
                            GameEvent.createGameEvent(fbb, GameEventType.Win, winLine)
                        }
                    }
                    fbb.finish(OppResponse.createOppResponse(fbb, OpponentRespMsg.GameEvent, eventOffset))
                    responseObserver.onNext(OppResponse.getRootAsOppResponse(fbb.dataBuffer()))
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

    private fun interruptionCauseException(cause: ErrorDetails.InterruptionCause): StatusRuntimeException {
        val interruptInfo = ErrorDetails.InterruptionInfo.newBuilder()
                .setCause(cause)
                .build()
        val status = Status.newBuilder()
                .setCode(Code.UNKNOWN.number)
                .addDetails(Any.pack(interruptInfo))
                .build()
        return StatusProto.toStatusRuntimeException(status)
    }
}