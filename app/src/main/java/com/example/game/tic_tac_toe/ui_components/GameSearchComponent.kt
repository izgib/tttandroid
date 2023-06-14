package com.example.game.tic_tac_toe.ui_components

import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GameSearchComponent(
    private val button: Button,
    private val progress: ProgressBar,
    private val searchText: String,
    private val stopText: String,
    private val searching: Boolean,
) : UIComponent<Boolean> {
    private val callback: Flow<Boolean>
    private var internalState = searching

    private val buttonText: String
        get() = if (internalState) stopText else searchText
    private val progressVisibility: Int
        get() = if (internalState) View.VISIBLE else View.GONE

    override fun getUserInteractionEvents() = callback


    init {
        button.text = buttonText
        progress.isIndeterminate = true
        callback = callbackFlow<Boolean> {
            button.setOnClickListener {
                internalState = !internalState
                val button = it as Button
                button.text = buttonText
                progress.visibility = progressVisibility
                trySend(internalState)
            }
            awaitClose()
        }
    }
}