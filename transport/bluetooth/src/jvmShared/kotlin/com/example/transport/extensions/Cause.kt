package com.example.transport.extensions

import com.example.controllers.models.InterruptCause
import com.example.transport.StopCause

fun StopCause.toInterruptCause(): InterruptCause {
    return when (this) {
        StopCause.STOP_CAUSE_LEAVE -> InterruptCause.Leave
        StopCause.STOP_CAUSE_DISCONNECT -> InterruptCause.Disconnected
        StopCause.STOP_CAUSE_INVALID_MOVE -> InterruptCause.InvalidMove
        StopCause.STOP_CAUSE_INTERNAL -> InterruptCause.Internal
        else -> throw IllegalStateException()
    }
}

fun InterruptCause.toStopCause(): StopCause {
    return when (this) {
        InterruptCause.Leave -> StopCause.STOP_CAUSE_LEAVE
        InterruptCause.InvalidMove -> StopCause.STOP_CAUSE_INVALID_MOVE
        InterruptCause.Internal -> StopCause.STOP_CAUSE_INTERNAL
        InterruptCause.Disconnected -> StopCause.STOP_CAUSE_DISCONNECT
    }
}

