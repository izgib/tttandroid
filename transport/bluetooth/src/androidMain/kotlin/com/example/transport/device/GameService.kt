package com.example.transport.device

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.controllers.GameSettings
import com.example.transport.BluetoothLEInteractor
import com.example.transport.UUID16Bit
import com.example.transport.extensions.toGameParams

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class GameService(
    val settings: SettingsCharacteristic,
    val clientMessages: ClientMessageCharacteristic,
    val serverMessage: ServerMessageCharacteristic,
) :

    BluetoothGattService(BluetoothLEInteractor.service, SERVICE_TYPE_PRIMARY) {
    init {
        addCharacteristic(settings)
        addCharacteristic(clientMessages)
        addCharacteristic(serverMessage)
    }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class SettingsCharacteristic(params: GameSettings) : BluetoothGattCharacteristic(
    BluetoothLEInteractor.settings,
    PROPERTY_BROADCAST or PROPERTY_READ,
    PERMISSION_READ
) {
    var params: GameSettings = params
        set(p) {
            value = p.toGameParams().toByteArray()
            field = p
        }

    init {
        value = params.toGameParams().toByteArray()
    }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ClientMessageCharacteristic : BluetoothGattCharacteristic(
    BluetoothLEInteractor.clientMessages,
    PROPERTY_WRITE,
    PERMISSION_WRITE
)

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ServerMessageCharacteristic : BluetoothGattCharacteristic(
    BluetoothLEInteractor.serverMessages,
    PROPERTY_INDICATE,
    PERMISSION_READ
) {
    val notifications: ServerMessageNotifications = ServerMessageNotifications()

    init {
        addDescriptor(notifications)
    }
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class ServerMessageNotifications : BluetoothGattDescriptor(
    UUID16Bit(0x2902u).toUUID(), PERMISSION_READ or PERMISSION_WRITE,
)