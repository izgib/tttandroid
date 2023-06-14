package com.example.game.tic_tac_toe.navigation.base.dialogs

import android.os.Parcel
import kotlinx.parcelize.Parceler
import java.util.*
import kotlin.collections.ArrayDeque

@OptIn(ExperimentalStdlibApi::class)
object DialogHistoryParceler : Parceler<ArrayDeque<DialogBase>> {
    override fun create(parcel: Parcel): ArrayDeque<DialogBase> {
        val source = parcel.readParcelableArray(DialogBase::class.java.classLoader)!!
        val type = Arrays.copyOf(
                source,
                source.size,
                Array<DialogBase>::class.java
        ).toList()
        return ArrayDeque(type)
    }

    override fun ArrayDeque<DialogBase>.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray<DialogBase>(toTypedArray(), 0)
    }
}