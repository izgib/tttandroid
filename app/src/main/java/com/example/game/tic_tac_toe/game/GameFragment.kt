package com.example.game.tic_tac_toe.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.game.controllers.GameParamsData
import com.example.game.controllers.InterruptCause
import com.example.game.domain.game.Coord
import com.example.game.domain.game.Mark
import com.example.game.domain.game.Tie
import com.example.game.domain.game.Win
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.GameLayoutBinding
import com.example.game.tic_tac_toe.viewmodels.DualResponse
import com.example.game.tic_tac_toe.viewmodels.GameSetupViewModel
import com.example.game.tic_tac_toe.viewmodels.GameViewModel
import com.example.game.tic_tac_toe.viewmodels.NavigationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs


class GameFragment : Fragment() {
    private lateinit var gv: BoardView
    private lateinit var cDrawer: Drawer
    private var touchThreshold = -1
    private var lastTouchDown: Long = 0
    private var startTouchX = -1f
    private var startTouchY = -1f

    private val GSViewModel: GameSetupViewModel by sharedViewModel()

    @ExperimentalCoroutinesApi
    private val GVModel: GameViewModel by viewModel { parametersOf(with(GSViewModel) { GameParamsData(rows.value!!, cols.value!!, win.value!!, player1, player2) }, GSViewModel.getGameType(), GSViewModel.isCreator) }
    private val navVM: NavigationViewModel by navGraphViewModels(R.id.game_configurator)

    @ExperimentalCoroutinesApi
    private val gameListener = object : View.OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchDown = System.currentTimeMillis()
                    startTouchX = event.x
                    startTouchY = event.y
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHOLD) {
                        if (abs(event.x - startTouchX) < touchThreshold &&
                                abs(event.y - startTouchY) < touchThreshold) {
                            if (!((cDrawer.FIELD_OFFSET_x < event.x) && (event.x < cDrawer.FIELD_OFFSET_y + cDrawer.SQUARE_SIZE * GSViewModel.cols.value!!)))
                                return false

                            if (!((cDrawer.FIELD_OFFSET_x < event.y) && (event.y < cDrawer.FIELD_OFFSET_y + cDrawer.SQUARE_SIZE * GSViewModel.rows.value!!)))
                                return false

                            Log.d(GF_TAG, "sending screen coords to func")
                            val move = xy2ij(event.x, event.y)
                            if (GVModel.clickRegister().moveTo(move.i, move.j)) {
                                Log.d(GF_TAG, "move{${move.i},${move.j}} sent")
                                disableMoveListener()
                            }
                        }
                        return false
                    }
                    return false
                }
                else -> return true
            }
        }
    }

    companion object {
        const val GF_TAG = "GameFragment"
        private const val CLICK_ACTION_THRESHOLD = 200
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableMoveListener() {
        gv.setOnTouchListener(gameListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun disableMoveListener() {
        gv.setOnTouchListener(null)
    }

    @ExperimentalCoroutinesApi
    private fun setGameObservers() {
        GVModel.crossObserver.observe(this, Observer {
            cDrawer.putX(it.i, it.j)
            cDrawer.updateCanvas()
            Log.d(GF_TAG, "X: {${it.i},${it.j}}")
        })
        GVModel.noughtObserver.observe(this, Observer {
            cDrawer.putO(it.i, it.j)
            cDrawer.updateCanvas()
            Log.d(GF_TAG, "O: {${it.i},${it.j}}")
        })
        GVModel.endedObserver.observe(this, Observer {
            var result: String
            var playerX: Float
            var playerO: Float
            when (it) {
                is Win -> {
                    with(it.line) {
                        cDrawer.putWLine(start.i, start.j, end.i, end.j, mark)
                        cDrawer.updateCanvas()
                        val player = if (mark == Mark.Cross) {
                            playerX = 1f
                            playerO = .1f
                            "X"
                        } else {
                            playerX = .1f
                            playerO = 1f
                            "O"
                        }
                        result = "Выиграл игрок $player"
                        Log.d(GF_TAG, "player ${player}: won this game")
                    }
                }
                is Tie -> {
                    result = "Ничья"
                    playerX = .5f
                    playerO = .5f
                    Log.d(GF_TAG, "Game is ended tie")
                }
                else -> throw IllegalArgumentException("expected to be WIN or TIE State")
            }
            val action = GameFragmentDirections.actionGameFragmentToGameResultDialog(
                    result, playerX, playerO)
            findNavController().navigate(action)
        })
        GVModel.interruptObserver.observe(this, Observer {
            val cause = when (it.cause) {
                InterruptCause.OppDisconnected -> "Потеряно соединение с противником"
                InterruptCause.Disconnected -> "Потеряная связь с сервером"
                InterruptCause.OppCheating -> "Противник использует читы"
                InterruptCause.Cheating -> "Отсоединен от сервера из-за использования читов"
            }
            val action = GameFragmentDirections.actionGameFragmentToGameErrorDialog(cause)
            findNavController().navigate(action)
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = GameLayoutBinding.inflate(inflater, container, false)
        binding.viewmodel = GSViewModel
        gv = binding.root as BoardView
        cDrawer = gv
        Log.d(GF_TAG, "view created")
        return gv
    }

    private fun xy2ij(x: Float, y: Float): Coord {
        Log.d(GF_TAG, "calculating GF coord from (x=$x,y=$y)")
        return Coord(
                ((y - cDrawer.FIELD_OFFSET_y) / cDrawer.SQUARE_SIZE).toInt(),
                ((x - cDrawer.FIELD_OFFSET_x) / cDrawer.SQUARE_SIZE).toInt()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GSViewModel.apply {
            Log.d(GF_TAG, "Game initialized rows:$rows, cols:$cols, win:$win, mark:$mark, playerX:$player1, playerO:$player2, creator:$isCreator")
        }
        touchThreshold = ViewConfiguration.get(context).scaledTouchSlop
        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(GF_TAG, "back pressed")
                findNavController().navigate(R.id.action_gameFragment_to_gameExitDialog)
                navVM.dualResponse.observe(this@GameFragment, object : Observer<DualResponse> {
                    override fun onChanged(resp: DualResponse) {
                        when (resp) {
                            DualResponse.Yes -> {
                                Log.d(GF_TAG, "game canceled")
                                GVModel.cancelGame()
                            }
                            DualResponse.No -> {
                            }
                        }
                    }
                })
            }
        })
    }


    override fun onStart() {
        super.onStart()
        gv.doOnPreDraw {
            if (!GVModel.isStarted()) {
                GVModel.startGame()
            } else {
                val markLists = GVModel.reloadGame()
                for (cross in markLists.crosses) {
                    cDrawer.putX(cross.i, cross.j)
                }
                for (nought in markLists.noughts) {
                    cDrawer.putO(nought.i, nought.j)
                }
            }
            setGameObservers()
        }

        if (GVModel.haveHumanPlayer()) {
            Log.d(GF_TAG, "handler connected")
            with(GVModel.clickRegister()) {
                requestObserver.observe(this@GameFragment, Observer {
                    Log.d(GF_TAG, "move requested")
                    enableMoveListener()
                })
            }
        }

        Log.d(GF_TAG, "start game")
    }
}