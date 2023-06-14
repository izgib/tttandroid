package com.example.transport.extensions

import com.example.game.Coord
import com.example.transport.Move
import com.example.transport.move

fun Move.toCoord() = Coord(row, col)
fun Coord.toMove() = move {
    this.row = this@toMove.row
    this.col = this@toMove.col
}