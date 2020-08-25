package com.example.game.tic_tac_toe.game

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.game.controllers.models.Cross
import com.example.game.controllers.models.EndState
import com.example.game.controllers.models.GameInterruption
import com.example.game.controllers.models.Nought
import com.example.game.domain.game.Mark
import com.example.game.domain.game.Tie
import com.example.game.domain.game.Win
import com.example.game.tic_tac_toe.databinding.RootLinearLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.*
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameController
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameError
import com.example.game.tic_tac_toe.navigation.screens.dialogs.GameResult
import com.example.game.tic_tac_toe.ui_components.GameComponent
import com.example.game.tic_tac_toe.ui_components.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GameFragment : BaseFragment() {
    private lateinit var game: GameComponent

    private val config by lazy<GameConfig> { lookup() }
    private val notifications by lazy { backstack.notifications }

    @ExperimentalCoroutinesApi
    private val gameController by lazy<GameController> { lookup() }

    companion object {
        const val TAG = "GameFragment"
    }

    @ExperimentalCoroutinesApi
    private fun setGameObserver(scope: CoroutineScope) {
        scope.launch {
            gameController.gameFlow.collect { signal ->
                when (signal) {
                    is Cross -> {
                        game.putX(signal.move)
                    }
                    is Nought -> {
                        game.putO(signal.move)
                    }
                    is EndState -> {
                        val winner = when (val state = signal.state) {
                            is Win -> state.line.mark
                            is Tie -> Mark.Empty
                            else -> throw IllegalArgumentException("expected to be WIN or TIE State")
                        }
                        backstack.dialogs.show(GameResult(winner))
                    }
                    is GameInterruption -> {
                        backstack.dialogs.show(GameError(signal.cause))
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RootLinearLayoutBinding.inflate(inflater, container, false).apply {
            game = GameComponent(root, GameState(config.rows, config.cols, gameController.reloadGame()))
        }.getRoot()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val moveLisener = gameController.clickRegister()
        val scope = viewLifecycleOwner.lifecycleScope
        scope.launch {
            moveLisener.listenerStateFlow.collect { accept ->
                if (accept) game.acceptMoves() else game.rejectMoves()
            }
        }
        scope.launch {
            game.getUserInteractionEvents().collect { move ->
                moveLisener.moveTo(move)
            }
        }
        setGameObserver(scope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config.apply {
            Log.d(TAG, "fragment: $this@GameFragment")
            Log.d(TAG, "Game initialized rows:$rows, cols:$cols, win:$win, playerX:$player1, playerO:$player2")
        }
        if (!gameController.isStarted()) {
            gameController.startGame()
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