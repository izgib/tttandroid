package com.example.game.tic_tac_toe.navigation.base

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {
    fun <T : ScreenBase> getScreen(): T = requireArguments().getParcelable<T>(ScreenBase.ScreenKey)!!
}