package com.example.transport

actual val serverFirst: Boolean = when (System.getProperty("serverFirst")) {
    "0" -> false
    "1" -> true
    else -> throw IllegalArgumentException()
}