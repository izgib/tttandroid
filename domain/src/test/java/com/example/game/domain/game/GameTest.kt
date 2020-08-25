package com.example.game.domain.game

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


class GameTest {
    lateinit var game: Game

    @Nested
    class `gameWon test` {
        val game = Game(3, 3, 3)
        private val fieldProperty = game::class.memberProperties.first { it.name == "gameField" } as KMutableProperty1<*, *>


        private fun setGameField(field: Array<Array<Mark>>) {
            fieldProperty.setter.call(game, field)
        }

        init {
            fieldProperty.isAccessible = true
        }


        @Test
        fun `Vertical Line`() {
            val gameField = arrayOf<Array<Mark>>(
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Empty),
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Empty),
                    arrayOf(Mark.Cross, Mark.Empty, Mark.Empty)
            )
            val winLine = EndWinLine(Mark.Cross, Coord(0, 0), Coord(2, 0))
            setGameField(gameField)

            for (coord in arrayOf(Coord(0, 0), Coord(1, 0))) {
                assert(game.gameWon(Mark.Cross, coord) == winLine)
            }
        }

        @Test
        fun `Horizontal line`() {
            val gameField = arrayOf<Array<Mark>>(
                    arrayOf(Mark.Nought, Mark.Nought, Mark.Nought),
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Empty),
                    arrayOf(Mark.Cross, Mark.Empty, Mark.Cross)
            )
            val winLine = EndWinLine(Mark.Nought, Coord(0, 0), Coord(0, 2))
            setGameField(gameField)

            for (coord in arrayOf(Coord(0, 0), Coord(0, 1))) {
                assert(game.gameWon(Mark.Nought, coord) == winLine)
            }
        }

        @Test
        fun `Main diagonal line`() {
            val gameField = arrayOf<Array<Mark>>(
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Nought),
                    arrayOf(Mark.Cross, Mark.Cross, Mark.Empty),
                    arrayOf(Mark.Nought, Mark.Empty, Mark.Cross)
            )
            val winLine = EndWinLine(Mark.Cross, Coord(0, 0), Coord(2, 2))
            setGameField(gameField)

            for (coord in arrayOf(Coord(0, 0), Coord(1, 1))) {
                assert(game.gameWon(Mark.Cross, coord) == winLine)
            }

        }

        @Test
        fun `Antidiagonal line`() {
            val gameField = arrayOf<Array<Mark>>(
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Nought),
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Cross),
                    arrayOf(Mark.Nought, Mark.Empty, Mark.Cross)
            )
            val winLine = EndWinLine(Mark.Nought, Coord(2, 0), Coord(0, 2))
            setGameField(gameField)

            for (coord in arrayOf(Coord(0, 2), Coord(1, 1))) {
                assert(game.gameWon(Mark.Nought, coord) == winLine)
            }
        }

        @Test
        fun `Row line`() {
            val gameField = arrayOf<Array<Mark>>(
                    arrayOf(Mark.Cross, Mark.Empty, Mark.Nought),
                    arrayOf(Mark.Cross, Mark.Empty, Mark.Nought),
                    arrayOf(Mark.Cross, Mark.Empty, Mark.Empty)
            )
            val winLine = EndWinLine(Mark.Cross, Coord(0, 0), Coord(2, 0))
            setGameField(gameField)

            val result = game.gameWon(Mark.Cross, Coord(0, 0))
            assert(winLine == result)
        }

        @Test
        fun `Full board antidiagonal win`() {
            val gameField = arrayOf<Array<Mark>>(
                    arrayOf(Mark.Cross, Mark.Nought, Mark.Cross),
                    arrayOf(Mark.Cross, Mark.Cross, Mark.Nought),
                    arrayOf(Mark.Nought, Mark.Nought, Mark.Cross)
            )
            val winLine = EndWinLine(Mark.Cross, Coord(0, 0), Coord(2, 2))
            setGameField(gameField)

            val result = game.gameWon(Mark.Cross, Coord(0, 0))
            assert(winLine == result)
        }
    }

    private fun gameCycle(moves: ArrayList<Coord>): GameState {
        for (move in moves.withIndex()) {
            game.moveTo(move.value)
            val state = game.gameState(move.value)
            if (state !is Continues) {
                if (move.index != moves.lastIndex) {
                    throw IllegalArgumentException("expected to be last element in array")
                }
                return state
            }
        }
        throw IllegalArgumentException("expected game end state")
    }
}