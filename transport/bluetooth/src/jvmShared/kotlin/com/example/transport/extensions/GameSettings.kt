package com.example.transport.extensions

import com.example.controllers.GameSettings
import com.example.transport.GameParams
import com.example.transport.gameParams

fun GameSettings.toGameParams(): GameParams = gameParams {
    rows = this@toGameParams.rows
    cols = this@toGameParams.cols
    win = this@toGameParams.win
    mark = this@toGameParams.creatorMark.toMarkType()
}


fun GameParams.toGameSettings() = GameSettings(rows, cols, win, mark.toMark())