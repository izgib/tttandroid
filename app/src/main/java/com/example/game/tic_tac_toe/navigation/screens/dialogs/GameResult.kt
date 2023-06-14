package com.example.game.tic_tac_toe.navigation.screens.dialogs

import androidx.fragment.app.DialogFragment
import com.example.game.Mark
import com.example.game.tic_tac_toe.dialogs.GameResultDialog
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBase
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogBaseWithResult
import com.example.game.tic_tac_toe.navigation.base.dialogs.ResultHandler
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameResult(private val mark: Mark) : DialogBaseWithResult<Unit>() {
    @IgnoredOnParcel
    override val resultHandler: ResultHandler<Unit> = ResultHandler()

    override fun instantiateFragment(): DialogFragment {
        return GameResultDialog.newInstance(mark)
    }
}