package com.example.game.tic_tac_toe.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.controllers.CreationFailure
import com.example.controllers.GameItem
import com.example.controllers.models.ParamRange
import com.example.game.GameRules
import com.example.game.tic_tac_toe.databinding.RootConstraintLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.GameFindConfig
import com.example.game.tic_tac_toe.navigation.scopes.NetworkInitializer
import com.example.game.tic_tac_toe.ui_components.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NetworkGamesList : Fragment() {
    private val findConfig by lazy<GameFindConfig> { lookup() }
    private val initializer by lazy<NetworkInitializer> { lookup() }

    private lateinit var gameFind: GameFindComponent
    private lateinit var list: GameFoundListComponent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RootConstraintLayoutBinding.inflate(inflater, container, false)
        val _container = binding.root
        gameFind = GameFindComponent(_container, GameFindState(
                ParamRange(GameRules.ROWS_MIN, GameRules.ROWS_MAX), findConfig.rows,
                ParamRange(GameRules.COLS_MIN, GameRules.COLS_MAX), findConfig.cols,
                ParamRange(GameRules.WIN_MIN, GameRules.WIN_MAX), findConfig.win,
                findConfig.searching
        ))
        list = GameFoundListComponent(_container)

        viewLifecycleOwner.lifecycleScope.launch {
            gameFind.getUserInteractionEvents().collect { setting ->
                when (setting) {
                    is Rows -> findConfig.rows.apply {
                        start = setting.start
                        end = setting.end
                        updateWinConstraint()
                    }
                    is Cols -> findConfig.cols.apply {
                        start = setting.start
                        end = setting.end
                        updateWinConstraint()
                    }
                    is Win -> findConfig.win.apply {
                        start = setting.start
                        end = setting.end
                        gameFind.fixWinRange()
                    }
                    is MarkSettings -> findConfig.mark = setting.mark
                    is FindGames -> {
                        findGames()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            list.getUserInteractionEvents().collect { position ->
                list.showLoading(position)
                val gameParams: GameItem
                gameParams = list.getItem(position)
                initializer.joinGame(gameParams).collect { state ->
                    if (state is CreationFailure) Snackbar.make(requireView(), "Can not connect to server", Snackbar.LENGTH_LONG).show()
                }
                list.disableLoading(position)
            }
        }

        return binding.getRoot()
    }

    private fun findGames() {
        findConfig.searching = true
        gameFind.searching = true
        viewLifecycleOwner.lifecycleScope.launch {
            initializer.findGames(findConfig).onEach { game ->
                list.addItem(game)
            }.onCompletion { cause ->
                val message = if (cause != null) {
                    "Не удалось подключиться к серверу"
                } else if (list.count() == 0) {
                    "Игры не найдены"
                } else {
                    null
                }
                if (message != null) Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
            }.catch { }.collect()
        }
    }

    private fun updateWinConstraint() {
        gameFind.updateWin(kotlin.math.min(findConfig.rows.end, findConfig.cols.end))
    }
}


