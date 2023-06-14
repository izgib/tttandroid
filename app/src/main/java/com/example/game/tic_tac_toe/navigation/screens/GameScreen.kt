package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.controllers.models.GameModel
import com.example.controllers.models.LocalGameModel
import com.example.game.tic_tac_toe.game.GameFragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.scopes.GameController
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class GameScreen constructor(
    @property:[IgnoredOnParcel] private val model: @RawValue GameModel = LocalGameModel(3, 3, 3, CoroutineScope(Dispatchers.Default)),
    @property:[IgnoredOnParcel] private val scope: @RawValue CoroutineScope = CoroutineScope(Dispatchers.Default)
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