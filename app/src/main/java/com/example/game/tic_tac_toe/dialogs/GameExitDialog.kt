package com.example.game.tic_tac_toe.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.viewmodels.DualResponse
import com.example.game.tic_tac_toe.viewmodels.NavigationViewModel

class GameExitDialog : DialogFragment() {
    private val navigationModel: NavigationViewModel by navGraphViewModels(R.id.game_configurator)

    companion object {
        const val G_EXIT_D_TAG = "GameExitDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Вы действительно хотите выйти?")
                .setPositiveButton("Да") { _, _ ->
                    navigationModel.result(DualResponse.Yes)
                    findNavController().navigate(R.id.action_gameExitDialog_to_myFragment)
                }
                .setNegativeButton("Нет") { _, _ ->
                    navigationModel.result(DualResponse.No)
                    dismiss()
                }
                .setCancelable(false)

        return builder.create()
    }
}