package com.example.game.tic_tac_toe.navigation.screens

import androidx.fragment.app.Fragment
import com.example.game.networking.BluetoothInteractorImpl
import com.example.game.tic_tac_toe.navigation.base.*
import com.example.game.tic_tac_toe.navigation.scopes.BluetoothInitializer
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.network.DeviceChooser
import com.zhuinden.simplestack.ServiceBinder
import kotlinx.android.parcel.Parcelize
import kotlin.time.ExperimentalTime

@Parcelize
class BluetoothDevicesScreen : ScreenBase(), HasServices {
    @OptIn(ExperimentalTime::class)
    override fun instantiateFragment(): Fragment {
        return DeviceChooser()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(GameConfig(lookup()))
            add(BluetoothInitializer(backstack.bluetooth, BluetoothInteractorImpl(), backstack))
        }
    }
}