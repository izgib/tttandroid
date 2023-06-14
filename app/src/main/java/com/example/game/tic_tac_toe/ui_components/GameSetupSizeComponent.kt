package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.example.controllers.models.Range
import com.example.game.tic_tac_toe.R
import com.example.game.tic_tac_toe.databinding.GameSetupStepSizerBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.min

class GameSetupSizeComponent private constructor(private val binding: GameSetupStepSizerBinding, state: GameSetupSizeState) : UIComponent<SizeSettings> {
    private var sizeSettings: Flow<SizeSettings>
    private val winRange = state.winRange

    override fun getUserInteractionEvents(): Flow<SizeSettings> = sizeSettings

    init {
        binding.apply {
            val context = root.context

            val rowsVal = state.rows - state.rowRange.start
            rows.max = state.rowRange.end - state.rowRange.start
            rows.progress = rowsVal
            rowsnum.text = context.getString(R.string.rows_num, state.rows)

            val colsVal = state.cols - state.colRange.start
            cols.max = state.colRange.end - state.colRange.start
            cols.progress = colsVal
            colsnum.text = context.getString(R.string.cols_num, state.cols)

            val winVal = min(rowsVal, colsVal)
            win.max = winVal
            win.progress = state.win - state.winRange.start
            winnum.text = context.getString(R.string.rowl_num, state.win)

            sizeSettings = callbackFlow<SizeSettings> {
                val listener = object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        println("seekBar: $seekBar")
                        val count: Int
                        trySendBlocking(
                            when (seekBar!!.id) {
                                rows.id -> {
                                    count = progress + state.rowRange.start
                                    rowsnum.text = context.getString(R.string.rows_num, count)
                                    if (!fromUser) return
                                    RowsCount(count)
                                }
                                cols.id -> {
                                    count = progress + state.colRange.start
                                    colsnum.text = context.getString(R.string.cols_num, count)
                                    if (!fromUser) return
                                    ColsCount(count)
                                }
                                win.id -> {
                                    count = progress + state.winRange.start
                                    winnum.text = context.getString(R.string.rowl_num, count)
                                    if (!fromUser) return
                                    WinCount(count)
                                }
                                else -> throw IllegalStateException()
                            }
                        )
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                }
                rows.setOnSeekBarChangeListener(listener)
                cols.setOnSeekBarChangeListener(listener)
                win.setOnSeekBarChangeListener(listener)
                awaitClose {
                    rows.setOnSeekBarChangeListener(null)
                    cols.setOnSeekBarChangeListener(null)
                    win.setOnSeekBarChangeListener(null)
                }
            }
        }
    }

    fun updateWin(constraint: Int) {
        if (constraint > winRange.end) {
            return
        }
        binding.win.max = constraint - winRange.start
    }

    companion object {
        fun bind(view: View, state: GameSetupSizeState): GameSetupSizeComponent {
            return GameSetupSizeComponent(GameSetupStepSizerBinding.bind(view), state)
        }

        fun inflate(container: ViewGroup, state: GameSetupSizeState): GameSetupSizeComponent {
            return GameSetupSizeComponent(
                    GameSetupStepSizerBinding.inflate(LayoutInflater.from(container.context), container),
                    state,
            )
        }
    }
}

data class GameSetupSizeState(
        val rowRange: Range, val rows: Int,
        val colRange: Range, val cols: Int,
        val winRange: Range, val win: Int
)

sealed class SizeSettings {
    abstract val value: Int
}

data class RowsCount(override val value: Int) : SizeSettings()
data class ColsCount(override val value: Int) : SizeSettings()
data class WinCount(override val value: Int) : SizeSettings()