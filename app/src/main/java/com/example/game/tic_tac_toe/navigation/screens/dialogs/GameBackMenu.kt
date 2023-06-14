package com.example.game.tic_tac_toe.navigation.screens.dialogs

import androidx.fragment.app.DialogFragment
import com.example.game.tic_tac_toe.dialogs.GameBackMenuDialog
import com.example.game.tic_tac_toe.dialogs.GameExitDialog
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBase
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBaseWithResult
import com.example.game.tic_tac_toe.navigation.base.dialogs.ResultHandler
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class GameBackMenu : DialogBaseWithResult<MenuResponse>() {
    override fun instantiateFragment(): DialogFragment {
        return GameBackMenuDialog.newInstance()
    }

    @IgnoredOnParcel
    override val resultHandler: ResultHandler<MenuResponse> = ResultHandler()
}

enum class MenuResponse {
    GiveUp, Exit
}