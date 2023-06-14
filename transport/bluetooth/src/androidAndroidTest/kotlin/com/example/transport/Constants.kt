package com.example.transport

actual val serverFirst: Boolean = when (BuildConfig.serverFirst) {
    0.toByte() -> {
        println("serverFirst is 0")
        false
    }
    1.toByte() -> {
        println("serverFirst is 1")
        true
    }
    else -> throw IllegalArgumentException()
}
