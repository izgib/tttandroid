package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.game.Coord
import com.example.game.ICoord
import com.example.game.Mark
import com.example.game.MarkLists
import com.example.game.tic_tac_toe.databinding.GameLayoutBinding
import com.example.game.tic_tac_toe.game.BoardView
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GameComponent(private val container: ViewGroup, state: GameState) : UIComponent<Coord> {
    private val binding = inflateView()
    private var moves: Flow<Coord>
    private var catchMove = false

    override fun getUserInteractionEvents(): Flow<Coord> = moves

    private fun inflateView() = GameLayoutBinding.inflate(LayoutInflater.from(container.context), container, true)

    init {
        val board = binding.gameBoard
        board.initGame(state.rows, state.cols)
        board.initMoves(state.marks)

        moves = callbackFlow {
            board.setOnPlayerMoveListener(object : BoardView.OnPlayerMoveListener {
                override fun onPlayerMove(board: BoardView, move: Coord) {
                    println("component: got move $move, catching: $catchMove")
                    if (catchMove) trySend(move)
                }
            })
            awaitClose()
        }
    }

    fun acceptMoves() {
        catchMove = true
    }

    fun rejectMoves() {
        catchMove = false
    }

    fun putX(coord: Coord) {
        binding.gameBoard.putX(coord.row, coord.col)
        binding.gameBoard.updateCanvas()
    }

    fun putO(coord: Coord) {
        binding.gameBoard.putO(coord.row, coord.col)
        binding.gameBoard.updateCanvas()
    }

    fun putWinLine(start: ICoord, end: ICoord, mark: Mark) {
        binding.gameBoard.putWLine(start.row, start.col, end.row, end.col, mark)
    }

    fun clear() {
        binding.gameBoard.clearField()
    }
}

data class GameState(val rows: Int, val cols: Int, val marks: MarkLists)