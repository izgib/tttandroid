package com.example.game.tic_tac_toe.navigation.base

import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogServiceDelegate
import com.zhuinden.simplestack.ScopedServices
import com.zhuinden.simplestack.ServiceBinder

class ServiceProvider : ScopedServices {
    override fun bindServices(serviceBinder: ServiceBinder) {
        val key = serviceBinder.getKey<ScreenBase>()

        val scope = serviceBinder.scopeTag

        if (key is HasServices && key.scopeTag == scope) {
            key.bindServices(serviceBinder)
        }

        if (key is DialogServiceDelegate) {
            if (key.haveScope && key.scopeTag == scope) {
                key.bindServices(serviceBinder)
            } else {
                key.bindDialogServices(serviceBinder)
            }
        }
    }

}