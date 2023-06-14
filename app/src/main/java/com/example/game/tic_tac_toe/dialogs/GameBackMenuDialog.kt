package com.example.game.tic_tac_toe.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.game.tic_tac_toe.navigation.base.backstack
import com.example.game.tic_tac_toe.navigation.base.dialogs
import com.example.game.tic_tac_toe.navigation.base.dialogs.BaseDialogFragment
import com.example.game.tic_tac_toe.navigation.screens.dialogs.MenuResponse

class GameBackMenuDialog: BaseDialogFragment() {
    private val handler by lazy { getResultHandler<MenuResponse>() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(arrayOf("Сдаться", "Выйти"), object : OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                handler.result = when (which) {
                    0 -> MenuResponse.GiveUp
                    1 -> MenuResponse.Exit
                    else -> throw IllegalStateException()
                }
            }

        })
        return builder.create()
    }

    companion object {
        const val TAG = "GameBackMenuDialog"

        fun newInstance(cancelable: Boolean = true) = GameBackMenuDialog().apply {
            isCancelable = cancelable
        }
    }

}