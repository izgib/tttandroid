package com.example.game.tic_tac_toe.navigation.base.dialogs

import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.zhuinden.simplestack.ScopeKey
import com.zhuinden.simplestack.ServiceBinder

interface DialogServiceDelegate : ScopeKey.Child, HasServices {
    val curator: ScreenBase
    val pendingStateChange: DialogHistoryChange?
    val history: List<DialogBase>
    val haveScope: Boolean
    fun bindDialogServices(serviceBinder: ServiceBinder)
}