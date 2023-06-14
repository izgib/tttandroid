package com.example.game.tic_tac_toe.ui_components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import com.example.controllers.GameItem
import com.example.game.Mark
import com.example.game.not
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.GameListBinding
import com.example.game.tic_tac_toe.databinding.GameListItemBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GameFoundListComponent(private val container: ViewGroup) : UIComponent<Int> {
    private val binding = inflateView()
    private val listPos: Flow<Int>


    override fun getUserInteractionEvents(): Flow<Int> = listPos

    private val gamesAdapter = GameAdapter(container.context, arrayListOf())

    init {
        with(binding) {
            gameList.adapter = gamesAdapter

            listPos = callbackFlow<Int> {
                gameList.setOnItemClickListener { _, _, position, _ ->
                    trySendBlocking(position)
                }
                awaitClose {
                    gameList.onItemClickListener = null
                }
            }

        }
    }

    private fun inflateView() = GameListBinding.inflate(LayoutInflater.from(container.context), container)

    fun addItem(item: GameItem) {
        gamesAdapter.add(item)
    }

    fun count(): Int = gamesAdapter.count

    fun showLoading(position: Int) {
        binding.gameList.isClickable = false
        gamesAdapter.showLoading(position)
        updateListItem(position)
    }

    fun disableLoading(position: Int) {
        binding.gameList.isClickable = true
        gamesAdapter.disableLoading()
        updateListItem(position)
    }

    private fun updateListItem(position: Int) {
        binding.gameList.apply {
            val range = firstVisiblePosition..lastVisiblePosition
            if (position in range) {
                val listItem = getChildAt(position - range.first)
                adapter.getView(position, listItem, binding.gameList)
            }
        }
    }

    fun getItem(position: Int) = gamesAdapter.getItem(position)!!
}

class GameAdapter(context: Context, private val items: ArrayList<GameItem>) :
        ArrayAdapter<GameItem>(context, R.layout.game_list_item, items) {
    private var loadingItem: Int? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            GameListItemBinding.bind(convertView)
        } else {
            GameListItemBinding.inflate(LayoutInflater.from(context))
        }
        return binding.apply {
            items[position].apply {
                gameID.text = ID.toString()
                with(settings) {
                    gameRows.text = rows.toString()
                    gameCols.text = cols.toString()
                    gameWin.text = win.toString()
                    gameMark.setImageResource(
                            when (!creatorMark) {
                                Mark.Cross -> R.drawable.ic_cross
                                Mark.Nought -> R.drawable.ic_nought
                                Mark.Empty -> throw IllegalArgumentException("wrong mark")
                            }
                    )
                }
            }
            if (loadingItem != null && loadingItem == position) {
                progressBar.visibility = ProgressBar.VISIBLE
            }
        }.root
    }

    fun showLoading(position: Int) {
        loadingItem = position
    }

    fun disableLoading() {
        loadingItem = null
    }
}