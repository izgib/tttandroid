package com.example.game.tic_tac_toe.gamesetup/*

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.game.tic_tac_toe.navigation.scopes.GameConfig
import com.example.game.tic_tac_toe.ui_components.*
import com.stepstone.stepper.Step
import com.stepstone.stepper.adapter.AbstractStepAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class GameSetupStepperAdapter(context: Context,
                              private val scope: CoroutineScope,
                              private val config: GameConfig,
                              private val createGame: (GameConfig) -> Unit
) : AbstractStepAdapter(context) {
    private var currentPage = POSITION_NONE
    private val pages = ArrayList<Step>(count)
    private var observerJob: Job? = null

    override fun getCount(): Int {
        return 2
    }

    override fun createStep(position: Int): Step {
        return when (position) {
            0 -> GameSetupSizer(context).apply { orientation = LinearLayout.VERTICAL }
            1 -> GameSetupPlayers(context)
            else -> throw IllegalArgumentException("Unsupported position: $position")
        }
    }

    override fun findStep(position: Int): Step? {
        return if (pages.count() > 1) pages[position] else null
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (pages.count() - 1 < position) {
            pages.add(createStep(position))
        }
        val stepView = pages[position] as View
        container.addView(stepView)

        return stepView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        currentPage = position
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun finishUpdate(container: View) {
        observerJob?.cancel()
        observerJob = scope.launch {
            when (currentPage) {
                0 -> {
                    val sizeSetup = (pages[currentPage] as GameSetupSizer).component
                    scope.launch {
                        sizeSetup.getUserInteractionEvents().collect { settings ->
                            when (settings) {
                                is RowsCount -> {
                                    config.rows = settings.value
                                    sizeSetup.updateWin(kotlin.math.min(config.rows, config.cols))
                                }
                                is ColsCount -> {
                                    config.cols = settings.value
                                    sizeSetup.updateWin(kotlin.math.min(config.rows, config.cols))
                                }
                                is WinCount -> config.win = settings.value
                            }
                        }
                    }
                }
                1 -> {
                    val playerSetup = (pages[currentPage] as GameSetupPlayers).component
                    scope.launch {
                        playerSetup.getUserInteractionEvents().collect { settings ->
                            when (settings) {
                                is PlayerX -> config.player1 = settings.player
                                is PlayerO -> config.player2 = settings.player
                                is Reshuffle -> {
                                    config.reshuffle()
                                    playerSetup.setPlayerX(config.player1)
                                    playerSetup.setPlayerO(config.player2)
                                }
                                is CreateGame -> createGame(config)
                            }
                        }
                    }
                }
            }
        }
    }
}*/
