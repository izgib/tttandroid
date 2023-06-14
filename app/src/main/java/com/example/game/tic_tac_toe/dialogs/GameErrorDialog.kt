package com.example.game.tic_tac_toe.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.controllers.models.InterruptCause
import com.example.game.tic_tac_toe.navigation.base.backstack


class GameErrorDialog : DialogFragment() {
    private val cause: InterruptCause
        get() = requireArguments().getSerializable(CAUSE_KEY) as InterruptCause

    companion object {
        private const val CAUSE_KEY = "cause"

        const val TAG = "GameResultDialog"
        private const val leave = "Потеряно соединение с противником"
        private const val disconnect = "Потеряная связь с сервером"
        private const val oppCheating = "Противник использует читы"
        private const val internal = "Внутреняя ошибка"

        fun newInstance(cause: InterruptCause) = GameErrorDialog().apply {
            arguments = (arguments ?: Bundle()).also { bundle ->
                bundle.putSerializable(CAUSE_KEY, cause)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val msg = when (cause) {
            InterruptCause.Leave -> leave
            InterruptCause.Disconnected -> disconnect
            InterruptCause.InvalidMove -> oppCheating
            InterruptCause.Internal -> internal
        }
        builder.setMessage(msg)
                .setCancelable(false)
                .setNeutralButton("OK") { _, _ ->
                    backstack.jumpToRoot()
                }

        return builder.create()

    }

}