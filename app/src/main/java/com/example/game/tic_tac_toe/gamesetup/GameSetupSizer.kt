package com.example.game.tic_tac_toe.gamesetup

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.example.controllers.models.ParamRange
import com.example.game.GameRules
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.ui_components.*
/*import com.stepstone.stepper.Step
import com.stepstone.stepper.VerificationError*/

const val TAG_GCS = "GameSetupSizer"


class GameSetupSizer : LinearLayout { // , Step {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val config by lazy<GameConfig> { lookup() }
    val component: GameSetupSizeComponent

    init {
        component = GameSetupSizeComponent.inflate(this, GameSetupSizeState(
                ParamRange(GameRules.ROWS_MIN, GameRules.ROWS_MAX), config.rows,
                ParamRange(GameRules.COLS_MIN, GameRules.COLS_MAX), config.cols,
                ParamRange(GameRules.WIN_MIN, GameRules.WIN_MAX), config.win
        ))
    }

    /*override fun verifyStep(): VerificationError? {
        return null
    }

    override fun onSelected() = Unit

    override fun onError(error: VerificationError) {
        TODO("Not yet implemented")
    }*/
}
