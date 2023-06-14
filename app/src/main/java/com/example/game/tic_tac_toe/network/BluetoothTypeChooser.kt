package com.example.game.tic_tac_toe.network

import com.example.game.tic_tac_toe.navigation.base.BaseFragment
import com.example.game.tic_tac_toe.navigation.base.backstack
import com.example.game.tic_tac_toe.navigation.base.bluetooth
import com.example.game.tic_tac_toe.navigation.base.bluetoothLE

class BluetoothTypeChooser: BaseFragment() {
    private val btSensor by lazy { backstack.bluetooth }
    private val btLESensor by lazy { backstack.bluetoothLE }
}