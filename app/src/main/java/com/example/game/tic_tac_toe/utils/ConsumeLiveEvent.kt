package com.example.game.tic_tac_toe.utils

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class ConsumeLiveEvent<T> : MutableLiveData<T>() {
    private val consumed = AtomicBoolean(true)
    private val mPending = AtomicBoolean(false)

    override fun onActive() {
        if (consumed.compareAndSet(false, true)) {
            value = super.getValue()
        }
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
        super.observe(owner, Observer { t ->
            if (mPending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        consumed.set(false)
        mPending.set(true)
        super.setValue(t)
    }

    fun hadConsumed() {
        consumed.set(true)
    }

    @MainThread
    fun call() {
        value = null
    }

    companion object {
        private const val TAG = "ConsumedLiveEvent"
    }
}