package com.example.game.tic_tac_toe.gamesetup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.InverseMethod
import androidx.fragment.app.Fragment
import com.example.game.domain.game.GameRules
import com.example.game.tic_tac_toe.databinding.GameSetupStepSizerBinding
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.stepstone.stepper.BlockingStep
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

const val TAG_GCS = "GameSetupSizer"

class GameSetupSizer : Fragment(), BlockingStep {
    private val GSViewModel: GameSetupViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GSViewModel.sizeSetup()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = GameSetupStepSizerBinding.inflate(inflater, container, false)
        binding.setupViewModel = GSViewModel
        binding.lifecycleOwner = this
        return binding.root
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

object SizerConverter {
    @InverseMethod("set")
    @JvmStatic
    fun get(value: Int): Int {
        return value - GameRules.ROWS_MIN
    }

    @JvmStatic
    fun set(value: Int): Int {
        return value + GameRules.ROWS_MIN
    }
}
