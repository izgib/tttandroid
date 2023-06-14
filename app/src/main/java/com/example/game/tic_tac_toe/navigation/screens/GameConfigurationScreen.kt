package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.controllers.models.GameType
import com.example.game.tic_tac_toe.gamesetup.GameSetup
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.GameBluetoothLECreator
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameCreator
import com.example.game.tic_tac_toe.network.GameChooser
import com.example.transport.BluetoothInteractorImpl
import com.example.transport.BluetoothLEInteractor
import com.example.transport.BluetoothLEInteractorImpl
import com.example.transport.service.NetworkInteractor
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.parcelize.Parcelize

@Parcelize
class GameConfigurationScreen : ScreenBase(), HasServices {
    override fun instantiateFragment(): Fragment {
        return GameSetup()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(GameConfig(lookup()))
            add(GameCreator(backstack))
        }
    }
}