package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.controllers.models.GameType
import com.example.game.tic_tac_toe.databinding.GameChoiceBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


@OptIn(ExperimentalCoroutinesApi::class)
class GameTypeComponent(private val container: ViewGroup) : UIComponent<GameType> {
    private val binding = inflateView()
    private var userEvents: Flow<GameType>


    override fun getUserInteractionEvents(): Flow<GameType> = userEvents

    private fun inflateView(): GameChoiceBinding {
        return GameChoiceBinding.inflate(LayoutInflater.from(container.context), container)
    }

    init {
        with(binding) {
            userEvents = callbackFlow<GameType> {
                val clickListener = View.OnClickListener { v ->
                    trySendBlocking(
                        when (v.id) {
                            locgame.id -> GameType.Local
                            btgame.id -> GameType.BluetoothClassic
                            btlegame.id -> GameType.BluetoothLE
                            netgame.id -> GameType.Network
                            else -> throw IllegalStateException()
                        }
                    )
                }
                locgame.setOnClickListener(clickListener)
                btgame.setOnClickListener(clickListener)
                btlegame.setOnClickListener(clickListener)
                netgame.setOnClickListener(clickListener)
                awaitClose {
                    locgame.setOnClickListener(null)
                    btgame.setOnClickListener(null)
                    btlegame.setOnClickListener(null)
                    netgame.setOnClickListener(null)
                }
            }
        }
    }
}