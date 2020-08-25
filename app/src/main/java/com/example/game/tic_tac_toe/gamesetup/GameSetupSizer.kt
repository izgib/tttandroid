package com.example.game.tic_tac_toe.gamesetup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.game.controllers.models.ParamRange
import com.example.game.domain.game.GameRules
import com.example.game.tic_tac_toe.databinding.RootLinearLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.ui_components.*
import com.stepstone.stepper.BlockingStep
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val TAG_GCS = "GameSetupSizer"

class GameSetupSizer : Fragment(), BlockingStep {
    private val config by lazy<GameConfig> { lookup() }
    private lateinit var gameSize: GameSetupSizeComponent

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = RootLinearLayoutBinding.inflate(inflater, container, false)
        gameSize = GameSetupSizeComponent(binding.root, GameSetupSizeState(
                ParamRange(GameRules.ROWS_MIN, GameRules.ROWS_MAX), config.rows,
                ParamRange(GameRules.COLS_MIN, GameRules.COLS_MAX), config.cols,
                ParamRange(GameRules.WIN_MIN, GameRules.WIN_MAX), config.win
        ))
        viewLifecycleOwner.lifecycleScope.launch {
            gameSize.getUserInteractionEvents().collect { settings ->
                when (settings) {
                    is RowsCount -> {
                        config.rows = settings.value
                        updateWinConstraint()
                    }
                    is ColsCount -> {
                        config.cols = settings.value
                        updateWinConstraint()
                    }
                    is WinCount -> config.win = settings.value
                }
            }
        }
        return binding.getRoot()
    }

    private fun updateWinConstraint() {
        gameSize.updateWin(kotlin.math.min(config.rows, config.cols))
    }

    override fun onBackClicked(callback: StepperLayout.OnBackClickedCallback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCompleteClicked(callback: StepperLayout.OnCompleteClickedCallback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onNextClicked(callback: StepperLayout.OnNextClickedCallback?) {
        Log.d(TAG_GCS, "Переход на другой ход")
        callback?.goToNextStep()
    }

    override fun onSelected() {

    }

    override fun verifyStep(): VerificationError? {
        return null
    }

    override fun onError(error: VerificationError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
