package com.example.transport

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.example.controllers.GameSettings
import com.example.transport.BluetoothInteractor.Companion.MY_UUID
import com.example.transport.BluetoothInteractor.Companion.NAME
import com.google.protobuf.sourceContext
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.IOException
import java.util.*


object BluetoothInteractorImpl : BluetoothInteractor {
    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    @SuppressLint("MissingPermission")
    private fun serverSocket(): BluetoothServerSocket {
        while (true) {
            try {
                return mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
            } catch (e: IOException) {
                if (e.message == "Try again") {
                    continue
                }
                throw e
            }
        }
    }

    override fun createGame(settings: GameSettings): Flow<GameCreateStatus> {
        return channelFlow {
            val serverSocket: BluetoothServerSocket = serverSocket()
            send(Awaiting)
            /*invokeOnClose { error ->
                println(error)
                serverSocket.close()
                Log.d("BI", "server socket closed")
            }*/

            try {
                val socket = run<BluetoothSocket> socket@{
                    var socket: android.bluetooth.BluetoothSocket
                    while (true) {
                        try {
                            socket =
                                GlobalScope.async<android.bluetooth.BluetoothSocket>(Dispatchers.IO) {
                                    return@async serverSocket.accept(120)
                                }.await()
                            Log.d(
                                "BI",
                                "device ${socket.remoteDevice.name}:${socket.remoteDevice.address} have connected"
                            )
                            return@socket socket
                        } catch (e: IOException) {
                            if (e.message == "Try again") {
                                continue
                            }
                            Log.e("BI", "got error: ${e.message}")
                            throw e
                        }
                    }
                    return@socket socket
                }
                println("connected: ${socket.isConnected}")
                val connectionScope = CoroutineScope(Dispatchers.Default)
                val wrapper = BluetoothServerWrapper(connectionScope, socket.toConnectionWrapper())
                wrapper.sendParams(settings)
                send(Connected(wrapper))

                println("before this")
                close()
            } catch (e: IOException) {
                println("here")
                //Log.e("BI", "got error: ${e.message}")
                send(CreatingFailure)
                close(e)
            }

            awaitClose {
                serverSocket.close()
                Log.d("BI", "server socket closed")
            }
        }

    }

    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    override fun joinGame(device: BluetoothDevice): Flow<GameJoinStatus> {
        return channelFlow {
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                }
                send(NeedsPairing)
                return@channelFlow
            }

            println("here: try to connect")
            println("${device.name}: ${device.address}")

            val socket: BluetoothSocket
            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID).apply {
                    send(Loading)
                    val portField = javaClass.getDeclaredField("mPort")
                    portField.isAccessible = true
                    //portField.setInt(this, 5)
                    println("port: ${portField.get(this)}")
                    portField.isAccessible = false
                    try {
                        connect()
                    } catch (e: Throwable) {
                        println("here")
                        println(e.message)
                        Log.e("BI", "got error: $e")
                        send(JoinFailure)
                        close()
                        return@channelFlow
                    }
                }
                val uuidField = socket::class.java.getDeclaredField("mUuid")
                println(uuidField.type)
                uuidField.isAccessible = true
                val profileUUID = uuidField.get(socket) as ParcelUuid
                println("connected to profile $profileUUID")
                if (profileUUID.uuid != MY_UUID) {
                    send(JoinFailure)
                    return@channelFlow
                }
                val connectionScope = CoroutineScope(Dispatchers.Default)
                val client = BluetoothClientWrapper(connectionScope, socket.toConnectionWrapper())
                val settings = client.getParams()
                println("before settings sending")
                trySend(Joined(settings, client))
            } catch (e: IOException) {
                Log.e("BI", "got error: $e")
                send(JoinFailure)
                return@channelFlow
            } finally {
                println("final block")
                close()
            }

            println("here waiting")
            awaitClose {
                println("closing")
            }
        }
    }
}