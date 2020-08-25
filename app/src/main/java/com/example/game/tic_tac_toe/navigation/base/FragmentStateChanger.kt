package com.example.game.tic_tac_toe.navigation.base

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogServiceDelegate
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.StateChanger

class FragmentStateChanger(
        private val fragmentManager: FragmentManager,
        private val containerId: Int
) : StateChanger {

    override fun handleStateChange(
            stateChange: StateChange,
            completionCallback: StateChanger.Callback
    ) {
        val top = stateChange.topNewKey<ScreenBase>()
        if (top is DialogServiceDelegate) {
            top.pendingStateChange?.run {
                val cfm =
                        fragmentManager.findFragmentByTag(top.fragmentTag)!!.childFragmentManager
                cfm.executePendingTransactions()
                when (this.direction) {
                    StateChange.FORWARD -> key.createFragment().show(cfm, key.fragmentTag)
                    StateChange.BACKWARD -> (cfm.findFragmentByTag(key.fragmentTag) as DialogFragment).dismissAllowingStateLoss()
                }
            }
            completionCallback.stateChangeComplete()
            return
        }

        val previousKeys: List<ScreenBase> = stateChange.getPreviousKeys()
        val newKeys: List<ScreenBase> = stateChange.getNewKeys()

        val prevTop = stateChange.topPreviousKey<ScreenBase>()
        if (prevTop is DialogServiceDelegate) {
            kotlin.run {
                if (prevTop !in newKeys && prevTop.curator != top) {
                    return@run
                }

                fragmentManager.findFragmentByTag(prevTop.fragmentTag)?.run {
                    childFragmentManager.run {
                        executePendingTransactions()
                        commit(true) {
                            for (dialogFragment in fragments) {
                                remove(dialogFragment)
                            }
                        }
                    }
                }

                if (prevTop.curator === top) {
                    completionCallback.stateChangeComplete()
                    return
                }

                val newHistory = ArrayList<ScreenBase>(newKeys).also {
                    it[newKeys.indexOf(prevTop)] = prevTop.curator
                }
                stateChange.backstack.setHistory(History.from(newHistory), StateChange.REPLACE)
                completionCallback.stateChangeComplete()
                return
            }
        }

        fragmentManager.executePendingTransactions()
        val fragmentTransaction =
                fragmentManager.beginTransaction().disallowAddToBackStack().apply {
                    when (stateChange.direction) {
                        StateChange.FORWARD -> setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        StateChange.BACKWARD -> setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        StateChange.REPLACE -> setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    }
                }

        for (oldKey in previousKeys) {
            fragmentManager.findFragmentByTag(oldKey.fragmentTag)?.let { fragment ->
                if (oldKey !in newKeys) {
                    fragmentTransaction.remove(fragment)
                } else if (!fragment.isDetached) {
                    fragmentTransaction.detach(fragment)
                }
                return@let
            }
        }

        for (newKey in newKeys) {
            var fragment = fragmentManager.findFragmentByTag(newKey.fragmentTag)
            if (newKey == stateChange.topNewKey()) {
                if (fragment != null) {
                    if (fragment.isRemoving) {
                        fragment = newKey.createFragment()
                        fragmentTransaction.replace(
                                containerId,
                                fragment,
                                newKey.fragmentTag
                        )
                    } else if (fragment.isDetached) fragmentTransaction.attach(fragment)
                } else {
                    fragment = newKey.createFragment()
                    fragmentTransaction.add(containerId, fragment, newKey.fragmentTag)
                }
            } else {
                if (fragment != null && !fragment.isDetached) {
                    fragmentTransaction.detach(fragment)
                }
            }
        }

        fragmentTransaction.commitAllowingStateLoss()
        completionCallback.stateChangeComplete()
    }
}