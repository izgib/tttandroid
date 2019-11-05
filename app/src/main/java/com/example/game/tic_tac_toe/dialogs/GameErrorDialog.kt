package com.example.game.tic_tac_toe.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.game.tic_tac_toe.R


class GameErrorDialog : DialogFragment() {
    private val args: GameErrorDialogArgs by navArgs()

    companion object {
        const val G_ERROR_D_TAG = "GameResultDialog"
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(args.cause)
                .setCancelable(false)
                .setNeutralButton("OK") { _, _ ->
                    findNavController().navigate(R.id.action_gameErrorDialog_to_myFragment)
                }

        return builder.create()
    }
}