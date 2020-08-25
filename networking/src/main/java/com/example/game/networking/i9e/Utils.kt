package com.example.game.networking.i9e

import com.example.game.controllers.models.InterruptCause

fun event2Cause(eventType: Byte): InterruptCause {
    return when (eventType) {
        GameEventType.Cheating -> InterruptCause.Cheating
        GameEventType.OppCheating -> InterruptCause.OppCheating
        GameEventType.Disonnected -> InterruptCause.Disconnected
        GameEventType.OppDisconnected -> InterruptCause.OppLeave
        else -> throw IllegalStateException("wrong event")
    }
}

fun cause2Event(cause: InterruptCause): Byte {
    return when (cause) {
        InterruptCause.Disconnected -> GameEventType.Disonnected
        InterruptCause.Cheating -> GameEventType.Cheating
        InterruptCause.OppCheating -> GameEventType.OppCheating
        InterruptCause.OppLeave -> GameEventType.OppDisconnected
    }
}