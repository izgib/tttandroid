package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.controllers.models.GameType
import com.example.game.networking.BluetoothInteractorImpl
import com.example.game.networking.NetworkInteractorImpl
import com.example.game.tic_tac_toe.gamesetup.GameSetup
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.navigation.scopes.GameCreator
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.android.parcel.Parcelize

@Parcelize
class GameConfigurationScreen : ScreenBase(), HasServices {
    override fun instantiateFragment(): Fragment {
        return GameSetup()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            val config = GameConfig(lookup()).apply {
                add(this)
            }
            val creator: GameCreator = when (config.gameType) {
                GameType.Local -> GameCreator(backstack)
                GameType.Bluetooth -> GameCreator(backstack, btHandler = BluetoothInteractorImpl())
                GameType.Network -> GameCreator(backstack, ntHandler = NetworkInteractorImpl.newInstance())
            }
            add(creator)
        }
    }

}