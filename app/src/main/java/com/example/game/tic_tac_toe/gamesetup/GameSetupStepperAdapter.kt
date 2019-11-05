package com.example.game.tic_tac_toe.gamesetup

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter


class GameSetupStepperAdapter(fm: FragmentManager, context: Context) : AbstractFragmentStepAdapter(fm, context) {

    override fun getCount(): Int {
        return 2
    }

    override fun createStep(position: Int): Step {
        return when (position) {
            0 -> GameSetupSizer()
            1 -> GameSetupPlayers()
            else -> throw IllegalArgumentException("Unsupported position: $position")
        }
    }
}