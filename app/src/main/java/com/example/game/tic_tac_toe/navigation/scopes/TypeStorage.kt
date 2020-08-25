package com.example.game.tic_tac_toe.navigation.scopes

import com.example.game.controllers.models.GameType
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.screens.ChooseScreen
import com.example.game.tic_tac_toe.navigation.screens.GameConfigurationScreen
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.Bundleable
import com.zhuinden.statebundle.StateBundle
import kotlin.properties.Delegates

class TypeStorage(private val backstack: Backstack) : Bundleable {
    var gameType: GameType by Delegates.observable(GameType.Local) { _, _, newValue ->
        backstack.goTo(getDirection(newValue))
    }

    override fun toBundle(): StateBundle = StateBundle().apply {
        putByte("type", gameType.ordinal.toByte())
    }

    override fun fromBundle(bundle: StateBundle?) {
        bundle?.run {
            gameType = GameType.values()[getByte("type").toInt()]
        }
    }

    private fun getDirection(gameType: GameType): ScreenBase {
        return when (gameType) {
            GameType.Local -> GameConfigurationScreen()
            GameType.Bluetooth -> ChooseScreen()
            GameType.Network -> ChooseScreen()
        }
    }
}