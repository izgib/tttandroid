import com.example.game.AIPlayer
import com.example.game.Continues
import com.example.game.Coord
import com.example.game.Game
import com.example.game.GameState
import com.example.game.PotentialMap
import com.example.game.Tie
import com.example.game.Win
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.random.Random

private class GameCycle(val game: Game) {
    private val playerX = AIPlayer(game)
    private val playerO = AIPlayer(game)
    private val players = arrayOf(playerX, playerO)

    val moves = ArrayList<Coord>(game.rows * game.cols)
    private val plXownPotential = ArrayList<PotentialMap>(moves.size)
    private val plXoppPotential = ArrayList<PotentialMap>(moves.size)
    private val plOownPotential = ArrayList<PotentialMap>(moves.size)
    private val plOoppPotential = ArrayList<PotentialMap>(moves.size)

    val turns: Int
        get() = moves.size

    lateinit var state: GameState
        private set

    private fun notifyOppAndController(move: Coord) {
        players[game.otherPlayer()].sendMove(move)
        game.moveTo(move)
    }

    fun getMove() {
        saveMapInfo()
        val move = players[game.curPlayer()].getMove()
        moves.add(move)

        notifyOppAndController(move)
        gameContinues(move)
    }

    fun sendMove(move: Coord) {
        saveMapInfo()
        moves.add(move)

        players[game.curPlayer()].goTo(move)
        notifyOppAndController(move)
        gameContinues(move)
    }

    private fun gameContinues(move: Coord) {
        when (game.gameState(move)) {
            Continues -> {
            }
            else -> throw IllegalStateException("game must be continued, but got game termination event")
        }
    }

    fun goOn() {
        var state: GameState
        do {
            saveMapInfo()
            val move = players[game.curPlayer()].getMove()
            moves.add(move)

            notifyOppAndController(move)
            state = game.gameState(move)
        } while (state == Continues)
        this.state = state
    }

    fun printGameDebug() {
        for (i in moves.indices) {
            println("turn: $i")
            println("player X")
            printPotentialMap(plXownPotential[i])
            println()
            printPotentialMap(plXoppPotential[i])
            println()
            println("player O")
            printPotentialMap(plOownPotential[i])
            println()
            printPotentialMap(plOoppPotential[i])
            println("player${if (i and 1 == 0) "X" else "O"} move: ${moves[i]}")
            println("".padEnd(60, '-'))
        }
    }

    private fun printPotentialMap(map: PotentialMap) {
        val pad = 6
        for ((_, row) in map.withIndex()) {
            val part = StringBuilder("|")
            for ((_, potential) in row.withIndex()) {
                when (potential) {
                    playerX.usedCap -> part.append("X".padStart(pad))
                    -1 -> part.append("O".padStart(pad))
                    else -> part.append(potential.toString().padStart(6))
                }
                part.append('|')
            }
            println(part)
        }
    }

    private fun saveMapInfo() {
        plXownPotential.add(with(playerX.potentialMap) {
            PotentialMap(count()) { i -> get(i).copyOf() }
        })
        plXoppPotential.add(with(playerX.oppPotentialMap) {
            PotentialMap(count()) { i -> get(i).copyOf() }
        })

        plOownPotential.add(with(playerO.potentialMap) {
            PotentialMap(count()) { i -> get(i).copyOf() }
        })
        plOoppPotential.add(with(playerO.oppPotentialMap) {
            PotentialMap(count()) { i -> get(i).copyOf() }
        })
    }
}

private fun gameCycle(game: Game, init: GameCycle.() -> Unit): GameCycle {
    return GameCycle(game).apply(init)
}

internal class AIPlayerTest {
    private lateinit var player: AIPlayer

    @Nested
    inner class PotentialMaps() {
        private lateinit var game: Game

        @Test
        fun `3 rows 3 cols winLength 3`() {
            game = Game(3, 3, 3)
            player = AIPlayer(game)
            val potentials = arrayOf(
                    intArrayOf(9, 6, 9),
                    intArrayOf(6, 12, 6),
                    intArrayOf(9, 6, 9)
            )
            compareMap(potentials)

        }

        @Test
        fun `4 rows 3 cols winLength 3`() {
            game = Game(4, 3, 3)
            player = AIPlayer(game)
            val potentials = arrayOf(
                    intArrayOf(9, 6, 9),
                    intArrayOf(10, 13, 10),
                    intArrayOf(10, 13, 10),
                    intArrayOf(9, 6, 9)
            )
            compareMap(potentials)
        }

        @Test
        fun `3 rows 5 cols winLength 3`() {
            game = Game(3, 5, 3)
            player = AIPlayer(game)
            val potentials = arrayOf(
                    intArrayOf(9, 10, 15, 10, 9),
                    intArrayOf(6, 13, 15, 13, 6),
                    intArrayOf(9, 10, 15, 10, 9)
            )
            compareMap(potentials)
        }

        @Test
        fun `5 rows 5 cols winLength 3`() {
            game = Game(5, 5, 3)
            player = AIPlayer(game)
            val potentials = arrayOf(
                    intArrayOf(9, 10, 15, 10, 9),
                    intArrayOf(10, 15, 18, 15, 10),
                    intArrayOf(15, 18, 24, 18, 15),
                    intArrayOf(10, 15, 18, 15, 10),
                    intArrayOf(9, 10, 15, 10, 9)
            )
            compareMap(potentials)
        }

        private fun compareMap(comparableMap: Array<IntArray>) {
            for ((i, row) in comparableMap.withIndex()) {
                for ((j, potential) in row.withIndex()) {
                    assertEquals(player.potentialValue(i, j), potential, "($i,$j)")
                }
            }
        }
    }

    @Nested
    inner class Strategy {
        private lateinit var ownPotential: ArrayList<PotentialMap>
        private lateinit var oppPotential: ArrayList<PotentialMap>
        private lateinit var moves: ArrayList<Coord>

        @Test
        fun `X won on O bad move`() {
            val game = Game(3, 3, 3)
            val badMoves = arrayOf(Coord(0, 1), Coord(1, 0), Coord(1, 2), Coord(2, 1))
            val cycle = gameCycle(game) {
                getMove()
                sendMove(move = with(badMoves) {
                    get(Random.nextInt(count()))
                })
                goOn()
            }
            when (val state = cycle.state) {
                is Win -> {
                }
                else -> {
                    val res = when (state) {
                        Tie -> "game was tied"
                        Continues -> "game continues"
                        else -> throw IllegalStateException()
                    }
                    cycle.printGameDebug()
                    throw AssertionError({ "Have been Expected to playerX to won, but got: $res" })
                }
            }
        }

        @RepeatedTest(5)
        fun `check behaviour on non-standard move`() {
            val game = Game(3, 3, 3)

            val firstMove = Coord(1, 2)
            val secondMove = Coord(0, 0)

            val cycle = gameCycle(game) {
                sendMove(firstMove)
                getMove()
                sendMove(secondMove)
                goOn()
            }

            when (val state = cycle.state) {
                is Win -> {
                    cycle.printGameDebug()
                    throw AssertionError { "expect that game will be tied but, ${state.line.mark.name} won the game" }
                }
                is Tie -> return
            }
        }

        @Test
        fun `Tie on game 3 rows 3 cols winLength 3`() {
            val game = Game(3, 3, 3)

            val cycle = gameCycle(game) {
                goOn()
            }
            when (val state = cycle.state) {
                is Win -> {

                    AssertionError({ "expect that game will be tied but, ${state.line.mark.name} won the game" })
                }
                is Tie -> return
            }
        }

        @Test
        fun `player X won on field rows grater than 3`() {
            val game = Game(4, 3, 3)

            val cycle = gameCycle(game) {
                goOn()
            }

            when (cycle.state) {
                is Win -> return
                is Tie -> {
                    cycle.printGameDebug()
                    throw AssertionError({ "expect that playerX won this game but, it was tied" })
                }
            }
        }
    }
}