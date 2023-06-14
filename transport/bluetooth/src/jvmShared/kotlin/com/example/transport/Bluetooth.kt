package com.example.transport

import java.io.InputStream
import java.io.OutputStream
import java.util.*

expect class BluetoothDevice {
/*    @get:JvmName("getName")
    val name: String

    @get:JvmName("getAddress")
    val address: String*/

    fun getName(): String
    fun getAddress(): String

    //fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothSocket
}

expect class BluetoothSocket {
    /*@get:JvmName("getInputStream")
    val inputStream: InputStream

    @get:JvmName("getOutputStream")
    val outputStream: OutputStream*/

    fun getRemoteDevice(): BluetoothDevice
    fun getInputStream(): InputStream
    fun getOutputStream(): OutputStream
    fun close()
}

/*expect class BluetoothAdapter {
    fun listenUsingRfcommWithServiceRecord(name: String, uuid: UUID): BluetoothServerSocket
}*/

/*
expect class BluetoothServerSocket {

}*/
