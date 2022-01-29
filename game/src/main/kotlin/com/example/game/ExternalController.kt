package com.example.game


class ExternalController(rows: Int, cols: Int, win: Int) : GameController(rows, cols, win) {
    fun sendState(state: GameState) {
        when (state) {
            is Continues -> turn++
            else -> {}
        }
    }
}