package com.example.game.tic_tac_toe.navigation.screens.dialogs

import androidx.fragment.app.DialogFragment
import com.example.game.tic_tac_toe.dialogs.GameExitDialog
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBaseWithResult
import com.example.game.tic_tac_toe.navigation.base.dialogs.ResultHandler
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class GameExit : DialogBaseWithResult<DualResponse?>() {
    override fun instantiateFragment(): DialogFragment {
        return GameExitDialog.newInstance()
    }

    @IgnoredOnParcel
    override val resultHandler: ResultHandler<DualResponse?> = ResultHandler()
}

enum class DualResponse {
    Yes, No
}