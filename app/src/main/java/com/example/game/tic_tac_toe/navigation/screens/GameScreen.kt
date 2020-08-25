package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.controllers.models.GameModel
import com.example.game.tic_tac_toe.game.GameFragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.scopes.GameController
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import kotlinx.coroutines.CoroutineScope

@Parcelize
data class GameScreen constructor(
        @property:[IgnoredOnParcel] private val model: @RawValue GameModel,
        @property:[IgnoredOnParcel] private val scope: @RawValue CoroutineScope //override val history: List<DialogBase>
) : ScreenBase(), HasServices {  //, HasDialogs {
    override fun instantiateFragment(): Fragment {
        return GameFragment()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(GameController(backstack, model, scope))
        }
    }
}