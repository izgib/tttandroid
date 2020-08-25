package com.example.game.tic_tac_toe.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.game.tic_tac_toe.navigation.base.dialogs.BaseDialogFragment
import com.example.game.tic_tac_toe.navigation.screens.dialogs.DualResponse

class GameExitDialog : BaseDialogFragment() {
    private val handler by lazy { getResultHandler<DualResponse>() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Вы действительно хотите выйти?")
                .setPositiveButton("Да") { _, _ ->
                    handler.result = DualResponse.Yes
                }
                .setNegativeButton("Нет") { _, _ ->
                    handler.result = DualResponse.No
                }

        return builder.create()
    }

    companion object {
        const val TAG = "GameExitDialog"

        fun newInstance(cancelable: Boolean = false) = GameExitDialog().apply {
            isCancelable = cancelable
        }
    }
}
