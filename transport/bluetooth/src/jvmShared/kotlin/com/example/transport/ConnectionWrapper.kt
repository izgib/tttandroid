package com.example.transport

import java.io.InputStream
import java.io.OutputStream

interface ConnectionWrapper {
    val inputStream: InputStream
    val outputStream: OutputStream
    fun close()
}

expect fun BluetoothSocket.toConnectionWrapper() : ConnectionWrapper