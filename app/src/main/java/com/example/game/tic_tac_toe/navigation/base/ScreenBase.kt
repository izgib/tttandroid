package com.example.game.tic_tac_toe.navigation.base

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment

abstract class ScreenBase : Parcelable {
    open val fragmentTag: String
        get() = toString()

    protected abstract fun instantiateFragment(): Fragment

    open fun createFragment(): Fragment = instantiateFragment().apply {
        arguments = (arguments ?: Bundle()).also { bundle ->
            bundle.putParcelable(ScreenKey, this@ScreenBase)
        }
    }

    companion object {
        const val ScreenKey = "FRAGMENT_KEY"
    }
}

