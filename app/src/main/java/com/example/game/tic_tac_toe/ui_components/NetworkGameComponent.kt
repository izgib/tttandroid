package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.game.tic_tac_toe.databinding.NetworkGameBinding
import kotlinx.coroutines.flow.Flow

class NetworkGameComponent(private val container: ViewGroup) : UIComponent<PlayerAction> {
    private val binding = inflateView()
    private var userEvents: Flow<PlayerAction>

    override fun getUserInteractionEvents(): Flow<PlayerAction> = userEvents

    private fun inflateView() = NetworkGameBinding.inflate(LayoutInflater.from(container.context), container)

    init {
        with(binding) {
            val flow = callbackFlow<PlayerAction> {
                val clickListener = View.OnClickListener { v ->
                    sendBlocking(when (v.id) {
                        netCreateGame.id -> Create
                        netFindEnemy.id -> Find
                        else -> throw IllegalStateException()
                    })
                }
                netCreateGame.setOnClickListener(clickListener)
                netFindEnemy.setOnClickListener(clickListener)
                awaitClose {
                    netCreateGame.setOnClickListener(null)
                    netFindEnemy.setOnClickListener(null)
                }
            }
            userEvents = flow
        }
    }
}

sealed class PlayerAction
object Create : PlayerAction()
object Find : PlayerAction()

