package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.game.Mark
import com.example.game.not
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.BluetoothGameItemBinding
import com.example.transport.BluetoothGameItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GameListComponent(
    private val container: RecyclerView,
    private val games: List<BluetoothGameItem>
) : UIComponent<BluetoothGameItem> {
    private val callbacks: Flow<BluetoothGameItem>
    override fun getUserInteractionEvents(): Flow<BluetoothGameItem> = callbacks

    private val adapter = object : ListComponentAdapter<BluetoothGameItem>() {
        var onGameClick: ((BluetoothGameItem) -> Unit)? = null

        override fun getComponentForList(viewType: Int): UIComponentForList<BluetoothGameItem> {
            println("getting component")
            return GameItemComponent(
                BluetoothGameItemBinding.inflate(
                    LayoutInflater.from(container.context),
                    container,
                    false
                )
            ).apply {
                binding.root.setOnClickListener { _ ->
                    println("clicked")
                    onGameClick?.let { click -> click(games[adapterPosition]) }
                }
            }
        }

        fun getItem(position: Int): BluetoothGameItem {
            println("gameList position: $position")
            return games[position]
        }

        override fun getItemCount(): Int {
            println("gameList size: ${games.size}")
            return games.size
        }

        override fun onBindViewHolder(
            holder: UIComponentForList<BluetoothGameItem>,
            position: Int
        ) {
            (holder as GameItemComponent).game = getItem(position)
        }
    }

    init {
        container.layoutManager = LinearLayoutManager(container.context)
        container.adapter = adapter

        callbacks = callbackFlow<BluetoothGameItem> {
            adapter.onGameClick  = { game ->
                println("getting game: $game")
                trySend(game)
            }
            awaitClose()
        }
    }

    fun notifyDataSetChanged() {
        container.adapter!!.notifyDataSetChanged()
    }

    class GameItemComponent(val binding: BluetoothGameItemBinding) :
        UIComponentForList<BluetoothGameItem>(binding.root) {
        var game: BluetoothGameItem? = null
            set(value) {
                requireNotNull(value)
                binding.deviceName.text = value.device.name
                binding.deviceMacAddress.text = value.device.address
                value.settings?.let { gameSettings ->
                    binding.gameRows.text = "rows: ${gameSettings.rows}"
                    binding.gameCols.text = "cols: ${gameSettings.cols}"
                    binding.gameWin.text = "win: ${gameSettings.win}"
                    binding.gameMark.setImageResource(
                        when (!gameSettings.creatorMark) {
                            Mark.Cross -> R.drawable.ic_cross
                            Mark.Nought -> R.drawable.ic_nought
                            Mark.Empty -> throw IllegalArgumentException("wrong mark")
                        }
                    )
                }
            }
    }
}

