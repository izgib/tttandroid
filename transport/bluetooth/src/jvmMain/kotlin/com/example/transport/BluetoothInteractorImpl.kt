package com.example.transport

import com.example.controllers.GameSettings
import com.example.transport.BluetoothInteractor.Companion.MY_UUID
import com.example.transport.BluetoothInteractor.Companion.NAME
import com.github.hypfvieh.bluetooth.DeviceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.exceptions.DBusExecutionException
import java.io.IOException
import java.nio.channels.*
import java.util.*

object BluetoothInteractorImpl : BluetoothInteractor {
    private val deviceManager = DeviceManager.createInstance(false)

    override fun createGame(settings: GameSettings): Flow<GameCreateStatus> {
        var bounded = true
        return channelFlow<GameCreateStatus> {
            send(Awaiting)
            val con: BluetoothSocket
            var wrapper: BluetoothServerWrapper? = null
            close(try {
                val connectionScope = CoroutineScope(Dispatchers.Default)

                withContext(Dispatchers.IO + connectionScope.coroutineContext) {
                    con = deviceManager.startServer(NAME, MY_UUID).first()
                }
                wrapper = BluetoothServerWrapper(connectionScope, con.toConnectionWrapper())
                wrapper.sendParams(settings)
                bounded = false
                send(Connected(wrapper))
                null
            } catch (e: Throwable) {
                println(e)
                send(CreatingFailure)
                e
            })
            awaitClose {
                if (bounded) wrapper?.close()
            }
        }
    }

    override fun joinGame(device: BluetoothDevice): Flow<GameJoinStatus> {
        return channelFlow {
            val socket: BluetoothSocket
            try {
                send(Loading)
                //socket = device.createRfcommSocketToServiceRecord(MY_UUID)
                socket = device.startClient(BluetoothInteractor.NAME, BluetoothInteractor.MY_UUID)
                val connectionScope = CoroutineScope(Dispatchers.Default)
                val client = BluetoothClientWrapper(connectionScope, socket.toConnectionWrapper())
                val settings = client.getParams()
                send(Joined(settings, client))
            } catch (e: IOException) {
                send(JoinFailure)
                return@channelFlow
            } catch (e: DBusExecutionException) {
                println(e)
                send(JoinFailure)
                return@channelFlow
            }
            finally {
                close()
            }
            awaitClose {
                println("join close")
            }
        }
    }
}

/**
 *<code>SocketInputStream</code> is meant for reading input streams opened in non blocking
 * mode like common input stream, usually working in blocking mode.
 */


/**
 * Constructs a stream that reads bytes from the given channel.
 *
 * <p> The <tt>read</tt> methods of the resulting stream will throw an
 * {@link IllegalBlockingModeException} if invoked while the underlying
 * channel is in non-blocking mode.  The stream will not be buffered, and
 * it will not support the {@link InputStream#mark mark} or {@link
 * InputStream#reset reset} methods.  The stream will be safe for access by
 * multiple concurrent threads.  Closing the stream will in turn cause the
 * channel to be closed.  </p>
 *
 * @param  ch
 *         The channel from which bytes will be read
 *
 * @return  A new input stream
 */

/*private class SocketInputStream(private val chan: SocketChannel) : InputStream() {
    override fun close(): Unit = inp.close()
    override fun read(): Int = inp.read()
    override fun read(b: ByteArray): Int {
        val size = b.size
        var readCount: Int
        do {
            readCount = inp.read(b)
        } while (readCount == 0 && size != 0)
        return readCount
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val size = b.size
        var readCount: Int
        do {
            readCount = inp.read(b, off, len)
        } while (readCount == 0 && size != 0)
        return readCount
    }

    override fun skip(n: Long): Long = inp.skip(n)
    override fun available(): Int = inp.available()
    override fun mark(readlimit: Int) = inp.mark(readlimit)
    override fun reset(): Unit = inp.reset()
    override fun markSupported(): Boolean = inp.markSupported()


    companion object {
        @JvmStatic
        private fun read(
            ch: ReadableByteChannel, bb: ByteBuffer, block: Boolean
        ): Int {
            return if (ch is SelectableChannel) {
                synchronized(ch.blockingLock()) {
                    val bm: Boolean = ch.isBlocking
                    if (!bm) throw IllegalBlockingModeException()
                    if (bm != block) ch.configureBlocking(block)
                    val n: Int = ch.read(bb)
                    if (bm != block) ch.configureBlocking(bm)
                    return n
                }
            } else {
                ch.read(bb)
            }
        }
    }
}*/