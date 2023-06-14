package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.game.tic_tac_toe.databinding.NetworkGameBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkGameComponent(private val container: ViewGroup) : UIComponent<PlayerAction> {
    private val binding = inflateView()
    private var userEvents: Flow<PlayerAction>

    override fun getUserInteractionEvents(): Flow<PlayerAction> = userEvents

    private fun inflateView() = NetworkGameBinding.inflate(LayoutInflater.from(container.context), container)

    init {
        with(binding) {
            val flow = callbackFlow<PlayerAction> {
                val clickListener = View.OnClickListener { v ->
                    trySendBlocking(
                        when (v.id) {
                            netCreateGame.id -> Create
                            netFindEnemy.id -> Find
                            else -> throw IllegalStateException()
                        }
                    )
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

