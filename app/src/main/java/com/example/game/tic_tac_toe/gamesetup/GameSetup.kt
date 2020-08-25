package com.example.game.tic_tac_toe.gamesetup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.game.tic_tac_toe.R
import com.stepstone.stepper.StepperLayout


class GameSetup : Fragment() {
    private lateinit var mStepperLayout: StepperLayout
    private lateinit var mContext: Context
    private lateinit var fm: FragmentManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (resources.getBoolean(R.bool.gameSetupFull)) {
            return inflater.inflate(R.layout.game_setup_full, container, false)
        }
        mStepperLayout = inflater.inflate(R.layout.stepper_view, container, false) as StepperLayout

        fm = childFragmentManager
        mContext = requireActivity().applicationContext
        mStepperLayout.adapter = GameSetupStepperAdapter(fm, mContext)
        return mStepperLayout
    }
}