package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.models.GameType
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.screens.BluetoothDevicesScreen
import com.example.game.tic_tac_toe.navigation.screens.GameConfigurationScreen
import com.example.game.tic_tac_toe.navigation.screens.NetworkGamesScreen
import com.example.game.tic_tac_toe.navigation.screens.BluetoothLEGamesScreen
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.Bundleable
import com.zhuinden.statebundle.StateBundle
import kotlin.properties.Delegates

class CreatorStorage(private val type: TypeStorage, private val backstack: Backstack) : Bundleable {
    var isCreator: Boolean by Delegates.observable(true) { _, _, newValue ->
        backstack.goTo(getDirection(newValue))
    }

    override fun toBundle(): StateBundle = StateBundle().apply {
        putBoolean("creator", isCreator)
    }

    override fun fromBundle(bundle: StateBundle?) {
        bundle?.run {
            isCreator = getBoolean("type")
        }
    }

    private fun getDirection(isCreator: Boolean): ScreenBase {
        return if (isCreator) GameConfigurationScreen() else when (type.gameType) {
            GameType.BluetoothClassic -> BluetoothDevicesScreen()
            GameType.BluetoothLE -> BluetoothLEGamesScreen()
            GameType.Network -> NetworkGamesScreen()
            else -> throw IllegalStateException()
        }
    }
}