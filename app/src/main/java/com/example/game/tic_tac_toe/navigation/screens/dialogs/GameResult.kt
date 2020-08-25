package com.example.game.tic_tac_toe.navigation.screens.dialogs

import androidx.fragment.app.DialogFragment
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.dialogs.GameResultDialog
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBase
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GameResult(private val mark: Mark) : DialogBase() {
    override fun instantiateFragment(): DialogFragment {
        return GameResultDialog.newInstance(mark)
    }
}