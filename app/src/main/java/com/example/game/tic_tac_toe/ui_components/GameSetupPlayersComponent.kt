package com.example.game.tic_tac_toe.ui_components

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.game.controllers.models.GameType
import com.example.game.controllers.models.PlayerType
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.GameSetupStepPlayersBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GameSetupPlayersComponent(private val container: ViewGroup, state: GameSetupPlayersState) : UIComponent<PlayersSettings> {
    private val binding = inflateView()
    private var playerSettings: Flow<PlayersSettings>

    override fun getUserInteractionEvents(): Flow<PlayersSettings> = playerSettings

    private fun inflateView() = GameSetupStepPlayersBinding.inflate(LayoutInflater.from(container.context), container)

    init {
        val players = getOpponents(container.context, state.gameType)
        binding.apply {
            playerXSpinner.adapter = OpponentChoose(container.context, R.layout.opponent_spinner, players)
            setPlayerX(state.playerX)

            playerOSpinner.adapter = OpponentChoose(container.context, R.layout.opponent_spinner, players)
            setPlayerO(state.playerO)

            playerSettings = callbackFlow<PlayersSettings> {
                val playerListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>) {}
                    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        val playerType = typeFromPosition(state.gameType, position)
                        sendBlocking(when (parent.id) {
                            playerXSpinner.id -> PlayerX(playerType)
                            playerOSpinner.id -> PlayerO(playerType)
                            else -> throw IllegalStateException()
                        })
                    }
                }
                playerXSpinner.onItemSelectedListener = playerListener
                playerOSpinner.onItemSelectedListener = playerListener

                val clickListener = View.OnClickListener { v ->
                    sendBlocking(when (v.id) {
                        reshuffle.id -> Reshuffle
                        gameCreate.id -> CreateGame
                        else -> throw IllegalStateException()
                    })
                }
                reshuffle.setOnClickListener(clickListener)
                gameCreate.setOnClickListener(clickListener)
                awaitClose()
            }
        }
    }

    fun setPlayerX(type: PlayerType) {
        binding.playerXSpinner.setSelection(type.toPosition())
    }

    fun setPlayerO(type: PlayerType) {
        binding.playerOSpinner.setSelection(type.toPosition())
    }
}

data class GameSetupPlayersState(val gameType: GameType, val playerX: PlayerType, val playerO: PlayerType)

data class Opponent(val img: Drawable, val oName: String)
class OpponentChoose(context: Context, resource: Int, private val opps: ArrayList<Opponent>) : ArrayAdapter<Opponent>(context, resource, opps) {
    private val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getDropDownView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val opp = inflater.inflate(R.layout.opponent_spinner, parent, false)
        val name = opp.findViewById(R.id.opponent_name) as TextView
        name.setCompoundDrawablesWithIntrinsicBounds(opps[position].img, null, null, null)
        name.text = opps[position].oName
        return opp
    }
}

//Players
private const val Bot = 0
private const val Human = 1
private const val Network = 2

private fun typeFromPosition(gameType: GameType, position: Int): PlayerType {
    return when (position) {
        Bot -> PlayerType.Bot
        Human -> PlayerType.Human
        Network -> if (gameType == GameType.Bluetooth) {
            PlayerType.Bluetooth
        } else {
            PlayerType.Network
        }
        else -> throw IllegalStateException()
    }
}

private fun PlayerType.toPosition(): Int {
    return when (this) {
        PlayerType.Bot -> Bot
        PlayerType.Human -> Human
        PlayerType.Bluetooth, PlayerType.Network -> Network
    }
}


// 1. Bot, 2.Human, 3.(Nobody|Bluetooth|Network)
fun getOpponents(context: Context, gameType: GameType): ArrayList<Opponent> {
    val size = when (gameType) {
        GameType.Local -> 2
        GameType.Bluetooth, GameType.Network -> 3
    }
    val data = ArrayList<Opponent>(size)

    var img = ContextCompat.getDrawable(context, R.drawable.ic_android_green_24dp)!!
    var name = "bot"
    data.add(Opponent(img, name))

    img = ContextCompat.getDrawable(context, R.drawable.ic_person_blue_24dp)!!
    name = "human"
    data.add(Opponent(img, name))

    when (gameType) {
        GameType.Local -> return data
        GameType.Bluetooth -> {
            img = ContextCompat.getDrawable(context, R.drawable.ic_bluetooth_blue_24dp)!!
            name = "bluetooth"
        }
        GameType.Network -> {
            img = ContextCompat.getDrawable(context, R.drawable.ic_public_blue_24dp)!!
            name = "network"
        }
    }
    data.add(Opponent(img, name))

    return data
}

sealed class PlayersSettings
data class PlayerX(val player: PlayerType) : PlayersSettings()
data class PlayerO(val player: PlayerType) : PlayersSettings()
object Reshuffle : PlayersSettings()
object CreateGame : PlayersSettings()
