package com.example.game.tic_tac_toe

import com.example.game.controllers.*
import org.koin.core.KoinComponent

class GameModelBuilder : KoinComponent {
    fun getModel(gameParamsData: GameParamsData, gameType: GameType, isCreator: Boolean = true): GameModel {
        return when (gameType) {
            GameType.Network -> NetworkGameModel(gameParamsData.rows, gameParamsData.cols, gameParamsData.win, getKoin().get<NetworkInteractor>().getGameClientWrapper())
            GameType.Local -> LocalGameModel(gameParamsData.rows, gameParamsData.cols, gameParamsData.win)
            GameType.Bluetooth -> {
                if (isCreator) {
                    BluetoothServerGameModel(gameParamsData.rows, gameParamsData.cols, gameParamsData.win, getKoin().get<BluetoothInteractor>().serverWrapper())
                } else {
                    NetworkGameModel(gameParamsData.rows, gameParamsData.cols, gameParamsData.win, getKoin().get<BluetoothInteractor>().clientWrapper())
                }
            }
        }
    }
}