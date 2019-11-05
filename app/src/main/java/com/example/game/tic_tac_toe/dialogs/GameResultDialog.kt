package com.example.game.tic_tac_toe.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.GameResultLayoutBinding

const val GRD_TAG = "GameResultDialog"

class GameResultDialog : DialogFragment() {
    private val args: GameResultDialogArgs by navArgs()
    override fun onCancel(dialog: DialogInterface) {
        Log.d(GRD_TAG, "dialog canceled")
        findNavController().navigate(R.id.action_gameResultDialog_to_myFragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = GameResultLayoutBinding.inflate(LayoutInflater.from(context))

        binding.result = args.gameResult
        binding.playerX = args.playerX
        binding.playerO = args.playerO
        return binding.root
    }
}

