package com.example.transport.impl

import java.nio.ByteBuffer
import java.util.UUID


@JvmInline
value class UUID16Bit(val uuid: UShort) {
    fun toUUID(): UUID {
        val buffer = ByteBuffer.wrap(UUID_BASE_bytes.copyOf()).putShort(2, uuid.toShort())
        val mostSig = buffer.getLong(0)
        val leasSig = buffer.getLong(8)
        return UUID(mostSig, leasSig)
    }
}


// base UUID "00000000-0000-1000-8000-00805F9B34FB"
val UUID_BASE_bytes = byteArrayOf(
    0x00,0x00,0x00,0x00,
    0x00,0x00, 0x10,0x00,
    0x80.toByte(),0x00,
    0x00, 0x80.toByte(),0x5F, 0x9B.toByte(),0x34, 0xFB.toByte()
)


