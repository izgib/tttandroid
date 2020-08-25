package com.example.game.tic_tac_toe.navigation.screens.dialogs

import androidx.fragment.app.DialogFragment
import com.example.game.controllers.models.InterruptCause
import com.example.game.tic_tac_toe.dialogs.GameErrorDialog
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBase
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GameError(private val cause: InterruptCause) : DialogBase() {
    override fun instantiateFragment(): DialogFragment {
        return GameErrorDialog.newInstance(cause)
    }
}