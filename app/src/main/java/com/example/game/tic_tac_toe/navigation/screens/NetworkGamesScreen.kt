package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameFindConfig
import com.example.game.tic_tac_toe.navigation.scopes.NetworkInitializer
import com.example.game.tic_tac_toe.network.NetworkGamesList
import com.example.transport.service.NetworkInteractor
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.parcelize.Parcelize

@Parcelize
class NetworkGamesScreen : ScreenBase(), HasServices {
    override fun instantiateFragment(): Fragment {
        return NetworkGamesList()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(GameConfig(lookup()))
            add(GameFindConfig())
            add(NetworkInitializer(NetworkInteractor.newInstance(), backstack))
        }
    }
}