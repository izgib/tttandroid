package com.example.controllers

import com.example.controllers.models.GameModel
import com.example.game.Coord

class DummyBot(val moves: List<Coord>) : LocalPlayer {
    private var i: Int = 0

    override suspend fun getMove() = moves[i].also { i++ }
}

