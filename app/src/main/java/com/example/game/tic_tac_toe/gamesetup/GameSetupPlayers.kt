package com.example.game.tic_tac_toe.gamesetup

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.game.tic_tac_toe.navigation.base.*
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.ui_components.*
//import com.stepstone.stepper.Step
//import com.stepstone.stepper.VerificationError


class GameSetupPlayers(context: Context) : ConstraintLayout(context) { // Step {
    private val config by lazy<GameConfig> { lookup() }
    val component: GameSetupPlayersComponent

    init {
        component = GameSetupPlayersComponent.inflate(this,
                GameSetupPlayersState(config.gameType, config.player1, config.player2)
        )
    }

    companion object {
        const val TAG = "GameSetupPlayers"
    }

/*    override fun onSelected() = Unit

    override fun verifyStep(): VerificationError {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(error: VerificationError) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }*/
}
