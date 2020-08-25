package com.example.game.networking

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.game.controllers.*
import com.example.game.controllers.BluetoothInteractor.Companion.MY_UUID
import com.example.game.domain.game.Mark
import com.example.game.networking.i9e.BluetoothCreator
import com.example.game.networking.i9e.BluetoothCreatorMsg
import com.example.game.networking.i9e.GameParams
import com.google.flatbuffers.FlatBufferBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class BluetoothInteractorImpl : BluetoothInteractor {
    private val mBluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var gameSocket: BluetoothSocket? = null
    private var isCreator: Boolean = true

    private var servWrapper: NetworkServer? = null
    private var clientWrapper: NetworkClient? = null

    private val fbb = FlatBufferBuilder(1024)

    companion object {
        const val NAME = "TTT"

        @OptIn(ExperimentalTime::class)
        val ACCEPT_TIMEOUT = 120.seconds
    }

    private suspend fun connectClient2Server(device: BluetoothDevice) = withContext<BluetoothSocket>(Dispatchers.IO) {
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }
        try {
            val socket = device.createRfcommSocketToServiceRecord(MY_UUID).apply { connect() }
            Log.d("BI", "connected to device ${device.name}:${device.address}")
            return@withContext socket
        } catch (e: IOException) {
            Log.e("BI", "unable to connect for device ${device.name}:${device.address}")
            Log.e("BI", "got error: $e")
            throw e
        }
    }

    private suspend fun serverSocket(): BluetoothServerSocket = withContext(Dispatchers.IO) {
        return@withContext mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun connectServer2Client(serverSocket: BluetoothServerSocket): BluetoothSocket = withContext(Dispatchers.IO) {
        try {
            return@withContext serverSocket.accept(ACCEPT_TIMEOUT.toInt(DurationUnit.MILLISECONDS))
        } catch (e: IOException) {
            throw e
        }
    }

    private suspend fun startServer(): BluetoothSocket {
        var counter = 0
        while (true) {
            try {
                val sSocket = serverSocket()
                Log.d("BI", "listening started at ${mBluetoothAdapter.name}")
                val socket = connectServer2Client(sSocket)
                Log.d("BI", "device ${socket.remoteDevice.name}:${socket.remoteDevice.address} have connected")
                return socket
            } catch (e: IOException) {
                if (e.message == "Try again") {
                    continue
                }
                Log.e("BI", "unable to start server")
                Log.e("BI", "got error: ${e.message}")
                throw e
            }
        }
    }

    override fun serverWrapper(): NetworkServer {
        if (servWrapper == null) {
            throw IllegalStateException("game is not initialized")
        }
        return servWrapper!!
    }

    override fun clientWrapper(): NetworkClient {
        if (clientWrapper == null) {
            throw IllegalStateException("game is not initialized")
        }
        return clientWrapper!!
    }

    @ExperimentalCoroutinesApi
    override fun createGame(settings: GameSettings): ReceiveChannel<GameInitStatus> {
        isCreator = true
        return GlobalScope.produce(capacity = Channel.CONFLATED) {
            try {
                gameSocket = startServer()
            } catch (e: IOException) {
                Log.e("BI", "got error: ${e.message}")
                send(GameInitStatus.Failure)
                return@produce
            }

            fbb.finish(BluetoothCreator.createBluetoothCreator(fbb, BluetoothCreatorMsg.GameParams,
                    with(settings) { GameParams.createGameParams(fbb, rows.toShort(), cols.toShort(), win.toShort(), creatorMark.mark) }
            ))

            try {
                gameSocket!!.outputStream
                gameSocket!!.outputStream.run {
                    withContext(Dispatchers.IO) {
                        write(fbb.sizedByteArray())
                        flush()
                    }
                }
                val serv = BluetoothServerWrapper(gameSocket!!)
                servWrapper = serv
                send(GameInitStatus.Connected(serv))
            } catch (e: IOException) {
                send(GameInitStatus.Failure)
            }
            close()
        }
    }

    @ExperimentalCoroutinesApi
    override fun joinGame(device: BluetoothDevice): ReceiveChannel<GameInitStatus> {
        isCreator = false
        return GlobalScope.produce(Dispatchers.IO, capacity = Channel.CONFLATED) {
            try {
                gameSocket = connectClient2Server(device)
            } catch (e: IOException) {
                Log.e("BI", "got error")
                send(GameInitStatus.Failure)
                return@produce
            }

            val buffer = ByteArray(1024)
            val readCount = gameSocket!!.inputStream.read(buffer)

            Log.d("BI", "read $readCount bytes")
            val bb = ByteBuffer.wrap(buffer, 0, readCount)

            val resp = BluetoothCreator.getRootAsBluetoothCreator(bb)
            if (resp.msgType() == BluetoothCreatorMsg.GameParams) {
                val params = resp.msg(GameParams()) as GameParams
                val settings = GameSettings(params.rows().toInt(), params.cols().toInt(), params.win().toInt(), Mark.values()[params.mark().toInt()])

                val client = BluetoothClientWrapper(gameSocket!!.inputStream, gameSocket!!.outputStream)
                clientWrapper = client
                send(GameInitStatus.OppConnected(settings, client))
            } else {
                throw IllegalArgumentException("expect game params, got: ${BluetoothCreatorMsg.name(resp.msgType().toInt())}")
            }
            close()
        }
    }
}

