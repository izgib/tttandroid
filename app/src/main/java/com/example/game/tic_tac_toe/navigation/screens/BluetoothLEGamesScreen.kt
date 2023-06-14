package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.add
import com.example.game.tic_tac_toe.navigation.base.bluetooth
import com.example.game.tic_tac_toe.navigation.base.bluetoothLE
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.example.game.tic_tac_toe.navigation.scopes.BluetoothInitializer
import com.example.game.tic_tac_toe.navigation.scopes.BluetoothLEInitializer
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.network.GameChooser
import com.example.transport.BluetoothInteractorImpl
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.parcelize.Parcelize

@Parcelize
class BluetoothLEGamesScreen: ScreenBase(), HasServices {
    override fun instantiateFragment(): Fragment {
        return GameChooser()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(GameConfig(lookup()))
            add(BluetoothLEInitializer(backstack))
        }
    }
}