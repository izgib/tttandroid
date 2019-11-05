package com.example.game.tic_tac_toe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CANCEL_GAME = "com.example.game.action_game_cancel"
    }

    private lateinit var cancelCallback: () -> Unit

    fun registerCallback(callback: () -> Unit) {
        cancelCallback = callback
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == CANCEL_GAME) {
            cancelCallback()
        }
    }
}