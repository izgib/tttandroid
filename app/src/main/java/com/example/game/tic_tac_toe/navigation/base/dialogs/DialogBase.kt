package com.example.game.tic_tac_toe.navigation.base.dialogs

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.game.tic_tac_toe.navigation.base.ScreenBase


abstract class DialogBase : ScreenBase() {
    abstract override fun instantiateFragment(): DialogFragment

    override fun createFragment(): DialogFragment = instantiateFragment().apply {
        arguments = (arguments ?: Bundle()).apply {
            putParcelable(DialogKey, this@DialogBase)
        }
    }

    companion object {
        const val DialogKey = "DIALOG_FRAGMENT_KEY"
    }
}