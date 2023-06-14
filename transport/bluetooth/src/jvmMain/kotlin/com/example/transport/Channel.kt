package com.example.transport

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.IllegalBlockingModeException
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SelectableChannel
import kotlin.experimental.and

// Construct a input stream that reads bytes from the given channel in blocking mode.
internal fun ReadableByteChannel.toInputStream(): InputStream = object : InputStream() {
    private val b1 = ByteBuffer.allocate(1)

    override fun close() = this@toInputStream.close()

    @Synchronized
    override fun read(): Int {
        b1.clear()
        val n = read(b1)
        return if (n == 1) (b1[0] and 0xff.toByte()).toInt() else -1
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if ((off < 0) || (off > b.size) || (len < 0) || ((off + len) > b.size) || (off + len) < 0) {
            throw IndexOutOfBoundsException()
        }
        if (len == 0) return 0

        return read(ByteBuffer.wrap(b).apply {
            position(off)
            limit(off + len)
        })
    }

    private fun read(bb: ByteBuffer): Int {
        val size = bb.capacity()
        if (size == 0) return 0
        var readCount: Int
        do {
            readCount = this@toInputStream.read(bb, true)
        } while (readCount == 0)
        return readCount
    }

    private fun ReadableByteChannel.read(bb: ByteBuffer, block: Boolean): Int {
        return if (this is SelectableChannel) {
            synchronized(this.blockingLock()) {
                val bm: Boolean = this.isBlocking
                if (!bm) throw IllegalBlockingModeException()
                if (bm != block) this.configureBlocking(block)
                val n: Int = this.read(bb)
                if (bm != block) this.configureBlocking(bm)
                return n
            }
        } else {
            this.read(bb)
        }
    }
}