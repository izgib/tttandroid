package com.example.game.tic_tac_toe

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class GameApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            //modules(listOf(viewModelsModule, networkModule, playersModule))
            androidContext(this@GameApplication)
            modules(listOf(viewModelsModule, bluetoothModule, networkModule))
        }
    }

}