package com.example.game.tic_tac_toe.ui_components

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class ButtonProgressComponent(private val button: Button, private val progress: ProgressBar, state: ButtonProgressState) : UIComponent<Unit> {
    private val callback: Flow<Unit>

    override fun getUserInteractionEvents() = callback

    init {
        progress.max = state.steps
        callback = callbackFlow<Unit> {
            button.setOnClickListener { button ->
                button.isClickable = false
                sendBlocking(Unit)
                progress.visibility = View.VISIBLE
                this.launch {
                    repeat(state.steps) {
                        delay(state.totalTime.div(state.steps))
                        progress.incrementProgressBy(1)
                    }
                    progress.visibility = View.GONE
                    progress.progress = 0
                    button.isClickable = true
                }
            }
            awaitClose()
        }
    }
}

data class ButtonProgressState @ExperimentalTime constructor(val steps: Int, val totalTime: Duration)