package com.example.transport.extensions

import com.example.controllers.ClientMove
import com.example.controllers.ClientResponse
import com.example.controllers.PlayerAction
import com.example.transport.ClientAction
import com.example.transport.ClientMessage

fun ClientMessage.toClientResponse(): ClientResponse {
    return when {
        hasMove() -> {
            println("received move")
            ClientMove(move.toCoord())
        }
        hasAction() -> {
            println("received action")
            com.example.controllers.ClientAction(
                when (action) {
                    ClientAction.CLIENT_ACTION_LEAVE -> PlayerAction.Leave
                    ClientAction.CLIENT_ACTION_GIVE_UP -> PlayerAction.GiveUp
                    else -> throw IllegalStateException()
                }
            )
        }
        else -> throw IllegalStateException()
    }
}