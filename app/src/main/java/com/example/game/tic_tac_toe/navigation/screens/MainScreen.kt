package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.tic_tac_toe.MainFragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.scopes.TypeStorage
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.android.parcel.Parcelize

@Parcelize
class MainScreen : ScreenBase(), HasServices {
    override fun instantiateFragment(): Fragment {
        return MainFragment()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(TypeStorage(backstack))
        }
    }
}