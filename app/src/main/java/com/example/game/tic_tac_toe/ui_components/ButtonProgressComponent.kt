package com.example.game.tic_tac_toe.ui_components

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class ButtonProgressComponent(
    private val button: Button,
    private val progress: ProgressBar,
    val state: ProgressState
) : UIComponent<Boolean> {
    var enabled: Boolean
        get() = internalState
        set(value) {
            internalState = value
        }

    private val callback: Flow<Boolean>

    private var internalState: Boolean by Delegates.observable(state.enabled) { property, oldValue, newValue ->
        if (!newValue) {
            if (oldValue) internalJob?.cancel().also {
                internalJob = null
            }
            progress.progress = 0
        }
        button.text = buttonText
        progress.visibility = progressVisibility
    }

    override fun getUserInteractionEvents() = callback

    private val buttonText: String
        get() = if (internalState) state.enabledLabel else state.disabledLabel
    private val progressVisibility: Int
        get() = if (internalState) View.VISIBLE else View.GONE

    private var internalJob: Job? = null

    init {
        progress.max = state.steps
        progress.isIndeterminate = when (state) {
            is IndeterminateProgress -> true
            is DeterminateProgress -> false
        }

        button.text = buttonText
        progress.visibility = progressVisibility

        callback = callbackFlow<Boolean> {
            button.setOnClickListener {
                trySend(!internalState)
                if (internalState && state is DeterminateProgress) {
                    val step = state.timeMillis / state.steps
                    internalJob = launch {
                        repeat(state.steps) {
                            delay(step)
                            progress.incrementProgressBy(1)
                        }
                        internalJob = null
                        internalState = !internalState
                        trySend(internalState)
                    }
                }
            }
            awaitClose()
        }
    }
}

sealed class ProgressState(
    val enabled: Boolean,
    val steps: Int,
    val enabledLabel: String,
    val disabledLabel: String
)

class DeterminateProgress(
    enabled: Boolean, steps: Int, val timeMillis: Long, enabledLabel: String, disabledLabel: String
) : ProgressState(enabled, steps, enabledLabel, disabledLabel)

class IndeterminateProgress(
    enabled: Boolean, steps: Int, enabledLabel: String,
    disabledLabel: String
) : ProgressState(enabled, steps, enabledLabel, disabledLabel)