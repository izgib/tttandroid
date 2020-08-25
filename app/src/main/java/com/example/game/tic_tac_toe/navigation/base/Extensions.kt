package com.example.game.tic_tac_toe.navigation.base

import androidx.fragment.app.Fragment
import com.example.game.networking.device.BluetoothSensor
import com.example.game.networking.device.NetworkSensor
import com.example.game.tic_tac_toe.navigation.base.dialogs.DialogService
import com.example.game.tic_tac_toe.notifications.NotificationsManager
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestack.navigator.Navigator

val Fragment.backstack: Backstack
    get() = Navigator.getBackstack(requireContext())

val Backstack.dialogs: DialogService
    get() = lookup()

val Backstack.bluetooth: BluetoothSensor
    get() = lookup()

val Backstack.network: NetworkSensor
    get() = lookup()

val Backstack.notifications: NotificationsManager
    get() = lookup()

inline fun <reified T> Backstack.lookup(serviceTag: String = T::class.java.name) = lookupService<T>(serviceTag)

inline fun <reified T> Fragment.lookup(serviceTag: String = T::class.java.name) = backstack.lookup<T>(serviceTag)

inline fun <reified T> ServiceBinder.add(service: T, serviceTag: String = T::class.java.name) {
    this.addService(serviceTag, service as Any)
}

inline fun <reified NAME> ServiceBinder.bindAs(service: Any, serviceTag: String = NAME::class.java.name) {
    this.addAlias(serviceTag, service)
}

inline fun <reified T> ServiceBinder.lookup(serviceTag: String = T::class.java.name) = lookupService<T>(serviceTag)

inline fun <reified T> ServiceBinder.get(serviceTag: String = T::class.java.name) = getService<T>(serviceTag)

inline fun <reified T> GlobalServices.Builder.add(service: T, serviceTag: String = T::class.java.name): GlobalServices.Builder =
        addService(serviceTag, service as Any)