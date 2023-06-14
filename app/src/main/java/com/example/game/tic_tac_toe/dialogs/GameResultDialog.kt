package com.example.game.tic_tac_toe.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.game.Mark
import com.example.game.tic_tac_toe.databinding.GameResultLayoutBinding
import com.example.game.tic_tac_toe.navigation.base.backstack
import com.example.game.tic_tac_toe.navigation.base.dialogs
import com.example.game.tic_tac_toe.navigation.base.dialogs.BaseDialogFragment
import com.example.game.tic_tac_toe.navigation.screens.dialogs.MenuResponse

const val GRD_TAG = "GameResultDialog"

class GameResultDialog : BaseDialogFragment() {
    private val handler by lazy { getResultHandler<Unit>() }

    override fun onCancel(dialog: DialogInterface) {
        Log.d(GRD_TAG, "dialog canceled")
        handler.result = Unit
    }

    private fun getWinner(): Mark = requireArguments().getSerializable(WINNER_KEY) as Mark

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return GameResultLayoutBinding.inflate(LayoutInflater.from(context)).apply {
            when (getWinner()) {
                Mark.Cross -> {
                    result.text = "Выиграл игрок X"
                    playerX.alpha = winnerAlpha
                    playerO.alpha = loserAlpha
                }
                Mark.Nought -> {
                    result.text = "Выиграл игрок O"
                    playerX.alpha = loserAlpha
                    playerO.alpha = winnerAlpha
                }
                Mark.Empty -> {
                    result.text = "Ничья"
                    playerX.alpha = tieAlpha
                    playerO.alpha = tieAlpha
                }
            }
        }.root.apply {
            setOnClickListener {
                dialog!!.cancel()
            }
        }
    }

    companion object {
        private val WINNER_KEY = "winner"

        private const val winnerAlpha = 1f
        private const val loserAlpha = .1f
        private const val tieAlpha = .5f

        fun newInstance(winner: Mark) = GameResultDialog().apply {
            arguments = (arguments ?: Bundle()).also { bundle ->
                bundle.putSerializable(WINNER_KEY, winner)
            }
        }

    }
}

