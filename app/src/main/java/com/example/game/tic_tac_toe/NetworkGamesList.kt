package com.example.game.tic_tac_toe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.appyvet.materialrangebar.RangeBar
import com.example.game.controllers.Created
import com.example.game.controllers.CreationFailure
import com.example.game.controllers.GameItem
import com.example.game.controllers.PlayerType
import com.example.game.domain.game.GameRules
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.databinding.GameListBinding
import com.example.game.tic_tac_toe.databinding.GameListItemBinding
import com.example.game.tic_tac_toe.viewmodels.GameInitializerModel
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.game_list_item.view.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameAdapter(context: Context, private val items: ArrayList<GameItem>) :
        ArrayAdapter<GameItem>(context, R.layout.game_list_item, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = GameListItemBinding.inflate(LayoutInflater.from(context))
        binding.listItem = items[position]

        return binding.root
    }
}

class NetworkGamesList : Fragment() {
    private val GIModel: GameInitializerModel by viewModel()
    private val GSViewModel: GameSetupViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = GameListBinding.inflate(inflater, container, false).apply {
            initializer = GIModel
            lifecycleOwner = this@NetworkGamesList
            gamesAdapter = GameAdapter(context!!, arrayListOf())
            rangeController = WinRangeController

            findGame.setOnClickListener { v ->
                gamesAdapter!!.clear()
                GIModel.viewModelScope.launch {
                    GIModel.listNt()
                            .buffer()
                            .onEach { gamesAdapter!!.add(it) }
                            .onCompletion { cause ->
                                val message = if (cause != null) {
                                    "Не удалось подключиться к серверу"
                                } else {
                                    if (gamesAdapter!!.count == 0) {
                                        "Игры не найдены "
                                    } else {
                                        null
                                    }
                                }
                                message?.let {
                                    Snackbar.make(v, message, Snackbar.LENGTH_LONG).show()
                                }
                            }
                            .catch { }
                            .collect()
                }
            }
            gameList.setOnItemClickListener { _, _, position, _ ->
                gameList.isClickable = false

                val item = gamesAdapter!!.getItem(position)!!
                GSViewModel.apply {
                    rows.apply { value = item.rows }
                    cols.apply { value = item.cols }
                    win.apply { value = item.win }
                    when (item.mark) {
                        Mark.Cross -> {
                            player1 = PlayerType.Network
                            player2 = PlayerType.Human
                        }
                        Mark.Nought -> {
                            player1 = PlayerType.Human
                            player2 = PlayerType.Network
                        }
                    }
                }

                GIModel.viewModelScope.launch {
                    val chan = GIModel.testJoinNt(item)

                    var state = withTimeoutOrNull(100L) {
                        return@withTimeoutOrNull chan.receive()
                    }
                    if (state == null) {
                        gameList.progress_bar.visibility = ProgressBar.VISIBLE
                        state = chan.receive()
                    }

                    when (state) {
                        is Created -> findNavController().navigate(R.id.action_gamesList_to_gameFragment)
                        is CreationFailure -> {
                            val snackbar = Snackbar.make(view!!, "Can not connect to server", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            Log.e("LOL LIST", "got error")
                        }
                    }
                    gameList.progress_bar.visibility = ProgressBar.GONE
                    gameList.isClickable = true
                }

            }

        }

        return binding.root
    }
}

object WinRangeController {
    fun controlRange(view: RangeBar, initializer: GameInitializerModel) {
        if (minOf(
                        maxOf(initializer.rowsLeft.value!!, initializer.rowsRight.value!!),
                        maxOf(initializer.colsLeft.value!!, initializer.colsRight.value!!)
                ) == GameRules.WIN_MIN) {
            view.setRangePinsByIndices(0, 0)
        }
    }
}

object GameFinder {
    fun findGames(view: Button, initializer: GameInitializerModel) {

    }
}


