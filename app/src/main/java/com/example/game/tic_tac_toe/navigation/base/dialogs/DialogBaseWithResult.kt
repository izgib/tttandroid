package com.example.game.tic_tac_toe.navigation.base.dialogs

import com.example.game.tic_tac_toe.navigation.base.add
import com.zhuinden.simplestack.ScopeKey
import com.zhuinden.simplestack.ServiceBinder


abstract class DialogBaseWithResult<T: Any?> : DialogBase(), ScopeKey {
    abstract val resultHandler: ResultHandler<T>
    fun bindResultRecorder(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(resultHandler)
        }
    }

    override fun getScopeTag(): String = javaClass.name
}