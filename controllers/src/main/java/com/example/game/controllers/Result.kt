package com.example.game.controllers

sealed class Result<T, E>
data class Success<T, E>(val value: T) : Result<T, E>()
data class Failure<T, E>(val value: E) : Result<T, E>()