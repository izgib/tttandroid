package com.example.game.tic_tac_toe.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.example.controllers.models.*
import com.example.game.Mark
import com.example.game.Tie
import com.example.game.Win
import com.example.game.tic_tac_toe.databinding.RootLinearLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.*
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameController
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameError
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameResult
import com.example.game.tic_tac_toe.ui_components.GameComponent
import com.example.game.tic_tac_toe.ui_components.GameState
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class GameFragment : BaseFragment() {
    private lateinit var game: GameComponent

    private val config by lazy<GameConfig> { lookup() }
    private val notifications by lazy { backstack.notifications }

    private val gameController by lazy<GameController> { lookup() }
    private var gameConfigured: Boolean = false

    companion object {
        const val TAG = "GameFragment"
    }

    private fun setGameObserver(scope: LifecycleCoroutineScope) {
        scope.launch {
            while (true) {
                gameController.started
                val final = gameController.gameFlow.onEach { signal ->
                    when (signal) {
                        is Cross -> game.putX(signal.move)
                        is Nought -> game.putO(signal.move)
                        else -> return@onEach
                    }
                }.last()
                when (final) {
                    is EndState -> {
                        val winner = when (val state = final.state) {
                            is Win -> with(state.line) {
                                if (start != null && end != null) {
                                    game.putWinLine(start!!, end!!, mark)
                                }
                                mark
                            }

                            is Tie -> Mark.Empty
                            else -> throw IllegalArgumentException("expected to be WIN or TIE State")
                        }
                        gameController.showGameResult(winner)
                        game.clear()
                    }

                    is GameInterruption -> {
                        backstack.dialogs.show(GameError(final.cause))
                        return@launch
                    }
                    else -> throw IllegalStateException()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return RootLinearLayoutBinding.inflate(inflater, container, false).apply {
            game = GameComponent(
                root,
                GameState(config.rows, config.cols, gameController.reloadGame())
            )
        }.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scope = viewLifecycleOwner.lifecycleScope
        scope.launch {
            gameController.moveRegister.listenerState.collect { accept ->
                if (accept) game.acceptMoves() else game.rejectMoves()
            }
        }
        scope.launch {
            game.getUserInteractionEvents().collect { move ->
                gameController.moveRegister.sendMove(move)
            }
        }
        setGameObserver(scope)

        //viewLifecycleOwner.lifecycleScope.
        view.doOnLayout {
            if (!gameConfigured) {
                configureGame()
                gameConfigured = true
            }
        }
    }

    private fun configureGame() {
        Log.d(TAG, "configured game")
        Log.d(TAG, "type: ${config.gameType}")
        when (val type = config.gameType) {
            GameType.Local -> {
                Log.d(TAG, "type: local game")
                gameController.setupLocalGame(config.player1, config.player2)
            }
            GameType.BluetoothClassic, GameType.BluetoothLE, GameType.Network -> {
                println("network game")
                val interconnectedPlayer = type.toPlayerType()
                println("player: $interconnectedPlayer")
                when {
                    config.player1 == interconnectedPlayer -> {
                        Log.d(TAG, "condition 1")
                        println("condition 1")
                        gameController.setupInterconnectedGame(Mark.Nought, config.player2)
                    }
                    config.player2 == interconnectedPlayer -> {
                        Log.d(TAG, "condition 2")
                        gameController.setupInterconnectedGame(Mark.Cross, config.player1)
                    }
                }
            }
        }
    }

    // return playerType for interconnected game
    private fun GameType.toPlayerType(): PlayerType {
        return when (this) {
            GameType.Network -> PlayerType.Network
            GameType.BluetoothLE, GameType.BluetoothClassic -> PlayerType.Bluetooth
            GameType.Local -> throw IllegalStateException()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config.apply {
            Log.d(TAG, "fragment: $this@GameFragment")
            Log.d(
                TAG,
                "Game initialized rows:$rows, cols:$cols, win:$win, playerX:$player1, playerO:$player2"
            )
            Log.d(
                TAG,
                "Game Type: $gameType"
            )
        }
    }

    override fun onStart() {
        super.onStart()
        if (notifications.shown) {
            notifications.closeNotification()
        }

        Log.d(TAG, "start: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "resume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "pause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "stop: ")
    }
}