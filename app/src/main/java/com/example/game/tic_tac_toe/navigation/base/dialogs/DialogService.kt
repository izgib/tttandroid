package com.example.game.tic_tac_toe.navigation.base.dialogs

import androidx.fragment.app.Fragment
import com.example.game.tic_tac_toe.navigation.base.HasServices
import com.example.game.tic_tac_toe.navigation.base.ScreenBase
import com.example.game.tic_tac_toe.navigation.base.dialogs
import com.example.game.tic_tac_toe.navigation.base.lookup
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ScopeKey
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestack.StateChange
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class DialogService(private val backstack: Backstack) {
    val history: List<DialogBase>
        get() = (backstack.top() as? DialogCurator)?.history ?: emptyList()

    fun show(dialog: DialogBase) {
        val top = backstack.top<ScreenBase>()
        notifyCurator((top as? DialogCurator
                ?: DialogCurator(
                        top
                )).apply {
            add(dialog)
        })
    }

    suspend fun <T> showTest(dialog: DialogBaseWithResult<T>): T? = suspendCoroutine { cont ->
        println("try to show")
        backstack.addStateChangeCompletionListener(object : Backstack.CompletionListener {
            override fun stateChangeCompleted(stateChange: StateChange) {
                if (dialog === backstack.dialogs.history.last()) {
                    backstack.removeStateChangeCompletionListener(this)
                    backstack.lookup<ResultHandler<T>>()
                            .onResultListener(object : ResultHandler.Listener<T?> {
                                override fun onResult(result: T?) {
                                    cont.resume(result)
                                }
                            })
                }
            }

        })
        show(dialog)
    }

    fun goBack(notify: Boolean = true) {
        (backstack.top<ScreenBase>() as? DialogCurator)?.run {
            remove(notify)
            notifyCurator(
                    if (history.isEmpty()) {
                        this.curator
                    } else {
                        this
                    }
            )
        }
    }

    private fun notifyCurator(screen: ScreenBase) {
        backstack.replaceTop(screen, StateChange.REPLACE)
    }
}

//for param 'direction' uses com.zhuinden.simplestack direction constants
data class DialogHistoryChange(val key: DialogBase, val direction: Int)

@Parcelize
private data class DialogCurator
@OptIn(ExperimentalStdlibApi::class) constructor(
        override val curator: ScreenBase,
        private val historyStack: @WriteWith<DialogHistoryParceler> ArrayDeque<DialogBase> = ArrayDeque(
                1
        )
) : ScreenBase(),
        DialogServiceDelegate {
    @IgnoredOnParcel
    override var pendingStateChange: DialogHistoryChange? = null
        private set
    override val history: List<DialogBase> get() = historyStack
    override val haveScope: Boolean
        get() = scopeServices != null
    override val fragmentTag: String
        get() = curator.fragmentTag

    @IgnoredOnParcel
    private val scopeServices: HasServices? = curator as? HasServices

    override fun bindDialogServices(serviceBinder: ServiceBinder) {
        println("try to inflate dialog services")
        for (dialog in history) {
            if (dialog is ScopeKey && dialog.scopeTag == serviceBinder.scopeTag) {
                if (dialog is DialogBaseWithResult<*>) {
                    dialog.bindResultRecorder(serviceBinder)
                }
                if (dialog is HasServices) {
                    dialog.bindServices(serviceBinder)
                }
                return
            }
        }
        throw IllegalStateException("dialog with tag: ${serviceBinder.scopeTag} doesn't exit")
    }

    override fun instantiateFragment(): Fragment {
        return curator.createFragment()
    }

    override fun createFragment(): Fragment {
        return instantiateFragment()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        scopeServices?.bindServices(serviceBinder)
    }

    override fun getScopeTag(): String {
        return scopeServices?.scopeTag ?: super.getScopeTag()
    }

    override fun getParentScopes(): MutableList<String> {
        val scopes = ArrayList<String>(history.count())
        for (dialog in history) {
            if (dialog is ScopeKey) {
                scopes.add(dialog.scopeTag)
            }
        }
        return scopes
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun add(dialog: DialogBase) {
        historyStack.addLast(dialog)
        val stateChange =
                DialogHistoryChange(
                        dialog,
                        StateChange.FORWARD
                )
        pendingStateChange = stateChange
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun remove(notify: Boolean = true) {
        val removable = historyStack.removeLast()
        pendingStateChange = if (notify) {
            DialogHistoryChange(
                    removable,
                    StateChange.BACKWARD
            )
        } else {
            null
        }
    }
}