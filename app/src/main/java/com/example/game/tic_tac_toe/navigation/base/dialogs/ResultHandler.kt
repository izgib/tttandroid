package com.example.game.tic_tac_toe.navigation.base.dialogs

class ResultHandler<T> {
    private var listener: Listener<T?>? = null
    var result: T? = null
        set(value) {
            listener?.onResult(value)
            field = value
        }

    fun onResultListener(listener: Listener<T?>) {
        this.listener = listener
    }

    interface Listener<in T> {
        fun onResult(result: T)
    }
}