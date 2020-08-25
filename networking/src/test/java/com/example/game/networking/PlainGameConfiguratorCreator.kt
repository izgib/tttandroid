package com.example.game.networking

import com.example.game.domain.game.*
import com.example.game.networking.i9e.*
import com.google.flatbuffers.FlatBufferBuilder
import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver

class PlainGameConfiguratorCreator(moves: Array<Coord>, mark: Mark, private val gameID: Short) : GameConfiguratorGrpc.GameConfiguratorImplBase() {
    private val fbb = FlatBufferBuilder(1024)
    private lateinit var game: Game
    private var initialization = true
    private val moveIterator = moves.iterator()
    private val playerMark: Int

    init {
        if (mark == Mark.Empty) throw IllegalArgumentException("expect cross or nought")
        playerMark = mark.mark.toInt()
    }

    override fun createGame(responseObserver: StreamObserver<CrResponse>): StreamObserver<CrRequest> {
        return object : StreamObserver<CrRequest> {
            override fun onNext(value: CrRequest) {
                var consumed = false
                if (initialization) {
                    assert(value.reqType() == CreatorReqMsg.GameParams) { "wrong event" }
                    val params = (value.req(GameParams()) as GameParams)
                    consumed = true
                    with(params) {
                        assert(mark().toInt() != playerMark) { "wrong player mark" }
                        game = Game(rows().toInt(), cols().toInt(), win().toInt())
                    }

                    fbb.finish(
                            CrResponse.createCrResponse(fbb, CreatorRespMsg.GameId,
                                    GameId.createGameId(fbb, gameID)
                            )
                    )
                    responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))

                    GameEvent.startGameEvent(fbb)
                    GameEvent.addType(fbb, GameEventType.GameStarted)
                    fbb.finish(CrResponse.createCrResponse(fbb, CreatorRespMsg.GameEvent,
                            GameEvent.endGameEvent(fbb))
                    )
                    responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
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
                        }
                        fbb.finish(CrResponse.createCrResponse(fbb, CreatorRespMsg.Move, Move.createMove(fbb, move.row.toShort(), move.col.toShort())))
                        responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
                    } else {
                        if (consumed) return
                        move = moveIterator.next()
                        assert(value.reqType() == CreatorReqMsg.Move) { "wrong package" }
                        val received = (value.req(Move()) as Move)
                        consumed = true
                        val gameCoord = Coord(received.row().toInt(), received.col().toInt())
                        assert(gameCoord == move) { "wrong move" }
                        if (!game.isValidMove(move)) {
                            curPlayerCheated = true
                        }
                    }
                    curPlayerCheated?.let { curPlayer ->
                        responseObserver.onError(interruptionCauseException(if (curPlayer) {
                            ErrorDetails.InterruptionCause.OPP_INVALID_MOVE
                        } else {
                            ErrorDetails.InterruptionCause.INVALID_MOVE
                        }))
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
                    fbb.finish(CrResponse.createCrResponse(fbb, CreatorRespMsg.GameEvent, eventOffset))
                    responseObserver.onNext(CrResponse.getRootAsCrResponse(fbb.dataBuffer()))
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