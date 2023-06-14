package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.BotPlayer
import com.example.controllers.HumanPlayer
import com.example.controllers.LocalPlayer
import com.example.controllers.MoveRegister
import com.example.controllers.models.GameModel
import com.example.controllers.models.GameSignal
import com.example.controllers.models.PlayerType
import com.example.game.Coord
import com.example.game.Mark
import com.example.game.tic_tac_toe.navigation.base.dialogs
import com.example.game.tic_tac_toe.navigation.screens.dialogs.DualResponse
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameBackMenu
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameExit
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameResult
import com.example.game.tic_tac_toe.navigation.screens.dialogs.MenuResponse
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopedServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GameController(
    private val backstack: Backstack,
    private val model: GameModel,
    private val scope: CoroutineScope
) : ScopedServices.Registered, ScopedServices.HandlesBack {
    private var onBackHandler: Job? = null
    private val modelPlayerSetup = arrayOfNulls<LocalPlayer?>(2)
    private var haveHumanPlayer: Boolean = false
    private val paused = MutableStateFlow(true)

    val moveRegister = MoveRegister(model)
    var started = false
        private set

    override fun onServiceUnregistered() {
        scope.cancel()
    }

    override fun onServiceRegistered() = Unit

    val gameFlow: Flow<GameSignal>
        get() = model.gameFlow.onCompletion { started = false }

    private fun startGame() {
        modelPlayerSetup[0]?.let { model.setupPlayerX(it) }
        modelPlayerSetup[1]?.let { model.setupPlayerO(it) }
        paused.value = false
        model.start()
    }

    fun reloadGame() = model.reload()

    fun isStarted() = started

    fun cancelGame() {
        model.cancel()
    }

    //fun haveHumanPlayer() = player1 == PlayerType.Human || player2 == PlayerType.Human

    fun setupPlayers(setupArray: Array<PlayerType?>) {
        require(setupArray.isNotEmpty())
        require(setupArray.size in 1..2)
    }

    fun setupLocalGame(playerX: PlayerType, playerO: PlayerType) {
        modelPlayerSetup[0] = playerX.toLocalPlayer()
        modelPlayerSetup[1] = playerO.toLocalPlayer()
        startGame()
    }

    fun setupInterconnectedGame(mark: Mark, player: PlayerType) {
        val index = when (mark) {
            Mark.Cross -> 0
            Mark.Nought -> 1
            Mark.Empty -> throw IllegalStateException()
        }
        modelPlayerSetup[index] = player.toLocalPlayer()
        startGame()
    }

    private fun nextSet() {
        model.clearField()
        modelPlayerSetup[0].let { playerO ->
            modelPlayerSetup[0] = modelPlayerSetup[1]?.refreshIfNeed()
            modelPlayerSetup[1] = playerO?.refreshIfNeed()
        }
        startGame()
    }

    private fun LocalPlayer.refreshIfNeed(): LocalPlayer {
        return if (this !is HumanPlayer) botPlayer() else this
    }

    override fun onBackEvent(): Boolean {
        println("here")
        if (onBackHandler == null) {
            onBackHandler = scope.launch {
                paused.value = true
                println("inside job")
                if (haveHumanPlayer) showMenuBackDialog() else showExitDialog()
                paused.value = false
                onBackHandler = null
            }
        }
        return true
    }

    suspend fun showGameResult(winner: Mark) {
        backstack.dialogs.showTest(GameResult(winner))
        nextSet()
    }

    private suspend fun showMenuBackDialog() {
        when (backstack.dialogs.showTest(GameBackMenu())) {
            MenuResponse.GiveUp -> moveRegister.giveUp()
            MenuResponse.Exit -> exitGame()
            null -> throw IllegalStateException()
        }
    }

    private fun exitGame() {
        backstack.jumpToRoot()
    }

    private suspend fun showExitDialog() {
        when (backstack.dialogs.showTest(GameExit())) {
            DualResponse.Yes -> exitGame()
            DualResponse.No, null -> return
        }
    }

    private fun PlayerType.toLocalPlayer(): LocalPlayer = when (this) {
        PlayerType.Bot -> botPlayer()
        PlayerType.Human -> {
            haveHumanPlayer = true
            HumanPlayer(moveRegister)
        }

        else -> throw IllegalStateException()
    }

    private fun botPlayer(): LocalPlayer = PausableBotPlayer(model, paused)

    class PausableBotPlayer(
        model: GameModel,
        private val paused: StateFlow<Boolean>,
        private val delay: Duration = 1.toDuration(DurationUnit.SECONDS)
    ) : LocalPlayer {
        private val bot = BotPlayer(model)
        override suspend fun getMove(): Coord {
            delay(delay)
            val move = bot.getMove()
            if (paused.value) paused.first { !it }
            return move
        }
    }
}