package com.example.game.networking.ext


import com.example.game.controllers.models.InterruptCause
import com.example.game.controllers.models.Interruption
import com.example.game.networking.ErrorDetails
import com.google.rpc.Status

internal fun Status.toInterruption(): Interruption {
    if (this.detailsList == null) {
        println(this.message)
        return Interruption(InterruptCause.Disconnected)
    }

    val cause = when (this.getDetails(0).unpack(ErrorDetails.InterruptionInfo::class.java).cause) {
        ErrorDetails.InterruptionCause.INVALID_MOVE -> InterruptCause.Disconnected
        ErrorDetails.InterruptionCause.OPP_INVALID_MOVE -> InterruptCause.OppCheating
        ErrorDetails.InterruptionCause.LEAVE -> InterruptCause.OppLeave
        ErrorDetails.InterruptionCause.DISCONNECT -> InterruptCause.Disconnected
        else -> throw IllegalArgumentException("illegal code")

    }
    return Interruption(cause)
}
