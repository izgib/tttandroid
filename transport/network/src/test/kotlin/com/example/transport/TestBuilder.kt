package com.example.transport

import com.example.game.*
import com.example.transport.service.toMark
import com.example.transport.service.toMarkType
import com.google.protobuf.Any
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver


class CreatorBuilder : GameConfiguratorGrpc.GameConfiguratorImplBase() {
    var gameID = 1
    var rows: Int = GameRules.ROWS_MIN
        set(value) {
        if (value !in GameRules.ROWS_MIN..GameRules.ROWS_MAX) throw IllegalArgumentException()
        field = value
    }
    var cols: Int = GameRules.COLS_MIN
        set(value) {
            if (value !in GameRules.COLS_MIN..GameRules.COLS_MAX) throw IllegalArgumentException()
            field = value
        }
    var win: Int = GameRules.WIN_MIN
        set(value) {
            if (value !in GameRules.WIN_MIN..GameRules.WIN_MAX) throw IllegalArgumentException()
            field = value
        }
    var playerMark: Mark = Mark.Cross
    private lateinit var game: Game

    private fun onInit(value: CreateRequest, responseObserver: StreamObserver<CreateResponse>) {
        assert(value.hasParams())
        with(value) {
            assert(params.rows == rows && params.cols == cols && params.win == win)
            assert(params.mark.toMark() != playerMark) { "wrong player mark" }
            game = Game(rows, cols, win)
            responseObserver.onNext(createResponse {
                gameId = gameID
            })
            responseObserver.onNext(createResponse {
                status = GameStatus.GAME_STATUS_GAME_STARTED
            })
        }
    }

    var moves: List<Coord> = listOf()
        set(value) {
            moveIterator = value.iterator()
            field = value
        }
    private lateinit var moveIterator: Iterator<Coord>
    

    private fun gameConsumer(value: CreateRequest, responseObserver: StreamObserver<CreateResponse>) {
        if (value.hasMove()) {
            assert(game.otherPlayer() == playerMark.mark.toInt())
            val expMove = moveIterator.next()
            val actMove = with(value.move) { Coord(row, col) }
            assert(actMove == expMove) { "expected $expMove, got $actMove" }

            if (!game.isValidMove(expMove)) {
                responseObserver.onError(
                    interruptionCauseException(
                        StopCause.STOP_CAUSE_INVALID_MOVE
                    )
                )
                return
            }
            val gameState = getState(expMove)

            responseObserver.onNext(createResponse {
                fillState(gameState)
            })

            if (isEndState(gameState)) {
                assert(!moveIterator.hasNext())
                responseObserver.onCompleted()
            }
        }
        if (game.curPlayer() == playerMark.mark.toInt()) {
            val m = moveIterator.next()
            if (!game.isValidMove(m)) {
                responseObserver.onError(
                    interruptionCauseException(
                        StopCause.STOP_CAUSE_INVALID_MOVE
                    )
                )
                return
            }
            val gameState = getState(m)

            responseObserver.onNext(createResponse {
                move = m.toMove()
                fillState(gameState)
            })

            if (isEndState(gameState)) {
                assert(!moveIterator.hasNext())
                responseObserver.onCompleted()
            }
        }
    }

    private fun getState(move: Coord): GameState {
        game.moveTo(move)
        return game.gameState(move)
    }

    private fun isEndState(state: GameState) = when (state) {
        is Continues -> false
        is Tie -> true
        is Win -> true
    }

    var initialized: Boolean = false


    override fun createGame(responseObserver: StreamObserver<CreateResponse>): StreamObserver<CreateRequest> {
        return object : StreamObserver<CreateRequest> {
            override fun onNext(value: CreateRequest) {
                if (!initialized) {
                    onInit(value, responseObserver)
                    initialized = true
                }
                gameConsumer(value, responseObserver)
            }

            override fun onError(t: Throwable) {
                println("WTF: $t")
            }

            override fun onCompleted() {
                println("all is good")
            }
        }
    }

    private fun CreateResponseKt.Dsl.fillState(state: GameState) {
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
    }

}

fun creator(init: CreatorBuilder.() -> Unit): CreatorBuilder {
    return CreatorBuilder().apply(init)
}

fun lol() {

}

fun interruptionCauseException(cause: StopCause): StatusRuntimeException {
    return interruption {
        this.cause = cause
    }.toStatusRuntimeException()
    /*val status = Status.newBuilder()
        .setCode(Code.UNKNOWN.number)
        .addDetails(interruptInfo.toAnyMessage())
        .build()
    return StatusProto.toStatusRuntimeException(status)*/
}


