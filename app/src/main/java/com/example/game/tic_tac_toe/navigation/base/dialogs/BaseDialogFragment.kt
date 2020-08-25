package com.example.game.tic_tac_toe.navigation.base.dialogs

import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment : DialogFragment() {
    fun <T : DialogBase> getBase(): T = requireArguments().getParcelable(
            DialogBase.DialogKey)!!

    fun <T> getResultHandler(): ResultHandler<T> = requireArguments().getParcelable<DialogBaseWithResult<T>>(
            DialogBase.DialogKey)!!.resultHandler
}