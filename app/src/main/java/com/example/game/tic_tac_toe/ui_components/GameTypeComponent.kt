package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.game.controllers.models.GameType
import com.example.game.tic_tac_toe.databinding.GameChoiceBinding
import kotlinx.coroutines.flow.Flow


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
                    sendBlocking(when (v.id) {
                        locgame.id -> GameType.Local
                        btgame.id -> GameType.Bluetooth
                        netgame.id -> GameType.Network
                        else -> throw IllegalStateException()
                    })
                }
                locgame.setOnClickListener(clickListener)
                btgame.setOnClickListener(clickListener)
                netgame.setOnClickListener(clickListener)
                awaitClose {
                    locgame.setOnClickListener(null)
                    btgame.setOnClickListener(null)
                    netgame.setOnClickListener(null)
                }
            }
        }
    }
}