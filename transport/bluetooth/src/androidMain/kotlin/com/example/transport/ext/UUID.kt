package com.example.transport.ext

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

val UUID.fixedOrder: UUID
    get() = ByteBuffer.allocate(16).run {
        putLong(leastSignificantBits)
        putLong(mostSignificantBits)
        rewind()
        order(ByteOrder.BIG_ENDIAN)
        return UUID(long, long)
    }

fun UUID.fixedOrder(): UUID {
    val buffer = ByteBuffer.allocate(16).apply {
        putLong(leastSignificantBits)
        putLong(mostSignificantBits)
        rewind()
        order(ByteOrder.LITTLE_ENDIAN)
    }
    return UUID(buffer.long, buffer.long)
}
