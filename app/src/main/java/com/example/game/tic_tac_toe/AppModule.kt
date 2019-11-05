package com.example.game.tic_tac_toe

import android.app.Application
import com.example.game.controllers.BluetoothInteractor
import com.example.game.controllers.GameParamsData
import com.example.game.controllers.GameType
import com.example.game.controllers.NetworkInteractor
import com.example.game.networking.BluetoothInteractorImpl
import com.example.game.networking.NetworkInteractorImpl
import com.example.game.networking.device.BluetoothSensor
import com.example.game.networking.device.NetworkSensor
import com.example.game.tic_tac_toe.viewmodels.GameInitializerModel
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.example.game.tic_tac_toe.viewmodels.GameViewModel
import com.example.game.tic_tac_toe.viewmodels.SensorsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val networkModule = module {
    single { NetworkInteractorImpl() as NetworkInteractor }
    single { NetworkSensor(androidContext()) }
}

val bluetoothModule = module {
    single { BluetoothSensor(androidContext()) }
    single { BluetoothInteractorImpl() as BluetoothInteractor }
}

val viewModelsModule = module {
    viewModel { SensorsViewModel(application = androidContext() as Application) }
    viewModel { GameSetupViewModel() }
    viewModel { GameInitializerModel() }
    viewModel { (gameParamsData: GameParamsData, gameType: GameType, isCreator: Boolean)
        ->
        GameViewModel(gameParamsData, gameType, isCreator)
    }
}
