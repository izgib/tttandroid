package com.example.game.tic_tac_toe.game

import com.example.game.domain.game.Mark

interface Drawer {
    fun drawField()
    fun putX(i: Int, j: Int)
    fun putO(i: Int, j: Int)
    fun putWLine(i1: Int, j1: Int, i2: Int, j2: Int, player: Mark)
    fun updateCanvas()
}