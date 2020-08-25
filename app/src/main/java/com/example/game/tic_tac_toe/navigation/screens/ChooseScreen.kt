package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.CreatorStorage
import com.example.game.tic_tac_toe.network.NetworkGame
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.android.parcel.Parcelize

@Parcelize
class ChooseScreen : ScreenBase(), HasServices {
    override fun instantiateFragment(): Fragment {
        return NetworkGame()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(CreatorStorage(lookup(), backstack))
        }
    }
}