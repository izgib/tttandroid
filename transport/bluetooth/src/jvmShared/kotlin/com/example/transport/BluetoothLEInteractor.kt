package com.example.transport

import com.example.controllers.GameSettings
import com.example.controllers.NetworkClient
import com.example.controllers.NetworkServer
import kotlinx.coroutines.flow.Flow
import java.util.*

interface BluetoothLEInteractor {
    fun connectGame(device: BluetoothDevice, gameParams: GameSettings?): Flow<ConnectionStatus>
    fun createService(params: GameSettings): Flow<ServiceStatus>
    fun createApplication(params: GameSettings): Application
    fun getDeviceList(): Flow<BluetoothGameItem>

    companion object {
        val service: UUID = UUID.fromString("f4c97db7-d56f-4d47-88f8-d856de80e301")
        val settings: UUID = UUID.fromString("8681b031-fb44-441b-bcc7-cb111b242dc7")
        val clientMessages: UUID = UUID.fromString("5d74df21-94d9-45c8-9939-a14291def778")
        val serverMessages: UUID = UUID.fromString("21dc9585-7c40-441d-b49c-fe489209acaf")
    }
}

class BluetoothGameItem(val device: BluetoothDevice, val settings: GameSettings?)

interface Application {
    var settings: GameSettings
    fun announceGame(): Flow<Announcement>
    fun registerApplication()
    fun unregisterApplication()
}

sealed class ConnectionStatus()
object Connecting : ConnectionStatus()
object ConnectingFailure : ConnectionStatus()
class ConnectedGame(val params: GameSettings, val client: NetworkClient) : ConnectionStatus()

sealed class Announcement
object Started : Announcement()
object Failed : Announcement()
class ClientJoined(val server: NetworkServer) : Announcement()

sealed class ServiceStatus
object Initialized : ServiceStatus()
object InitializationFailure : ServiceStatus()
class ServiceInstance(val server: NetworkServer) : ServiceStatus()