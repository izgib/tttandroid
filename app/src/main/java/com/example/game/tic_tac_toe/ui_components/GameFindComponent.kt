package com.example.game.tic_tac_toe.ui_components

import android.view.LayoutInflater
import android.view.ViewGroup
import com.appyvet.materialrangebar.RangeBar
import com.example.game.controllers.models.Range
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.databinding.GameFindSettingsBinding
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.properties.Delegates

class GameFindComponent(private val container: ViewGroup, state: GameFindState) : UIComponent<FindSettings> {
    private val binding = inflateView()
    private var findSettings: Flow<FindSettings>

    private val winRange = state.winRange
    private var winMax = winRange.end

    var searching: Boolean by Delegates.observable(true) { _, oldValue, newValue ->
        if (newValue != oldValue) binding.findGame.isEnabled = newValue
    }

    override fun getUserInteractionEvents(): Flow<FindSettings> = findSettings

    private fun inflateView() = GameFindSettingsBinding.inflate(LayoutInflater.from(container.context), container)

    fun updateWin(constraint: Int) {
        if (constraint > winRange.end) {
            return
        }
        winMax = constraint
        with(binding.winRange) {
            val restriction = if (constraint == winRange.start) {
                if (rightIndex != 0 || leftIndex != rightIndex) {
                    setRangePinsByIndices(0, 0)
                }
                constraint + 1
            } else {
                constraint
            }
            if (tickCount == restriction - winRange.start + 1) {
                return
            }
            binding.winRange.tickEnd = restriction.toFloat()
        }
    }

    fun fixWinRange() {
        with(binding.winRange) {
            if (winMax == winRange.start && (rightIndex != 0 || leftIndex != rightIndex)) {
                setRangePinsByIndices(0, 0)
            }
        }
    }

    init {
        with(binding) {
            val winR = winRange
            with(state) {
                rowsRange.apply {
                    tickEnd = rowRange.end.toFloat()
                    tickStart = rowRange.start.toFloat()
                    val start = rowRange.start
                    setRangePinsByIndices(rows.start - start, rows.end - start)
                }
                colsRange.apply {
                    tickStart = colRange.start.toFloat()
                    tickEnd = colRange.end.toFloat()
                    val start = colRange.start
                    setRangePinsByIndices(cols.start - start, cols.end - start)
                }
                winR.apply {
                    tickStart = winRange.start.toFloat()
                    tickEnd = winRange.end.toFloat()
                    val start = winRange.start
                    setRangePinsByIndices(win.start - start, win.end - start)
                }
            }
            findGame.isEnabled = !state.searching
            findSettings = callbackFlow {
                findGame.setOnClickListener {
                    sendBlocking(FindGames)
                }
                markConfig.setOnCheckedChangeListener { group, checkedId ->
                    sendBlocking(MarkSettings(when (checkedId) {
                        crossMark.id -> Mark.Cross
                        noughtMark.id -> Mark.Nought
                        anyMark.id -> Mark.Empty
                        else -> throw IllegalStateException()
                    }))
                }
                val rangeListener = object : RangeBar.OnRangeBarChangeListener {
                    override fun onTouchEnded(rangeBar: RangeBar) {
                        val start: Int
                        val end: Int
                        start = if (rangeBar.leftIndex <= rangeBar.rightIndex) {
                            end = rangeBar.rightIndex
                            rangeBar.leftIndex
                        } else {
                            end = rangeBar.leftIndex
                            rangeBar.rightIndex
                        }
                        sendBlocking(when (rangeBar.id) {
                            rowsRange.id -> Rows(start + state.rowRange.start, end + state.rowRange.start)
                            colsRange.id -> Cols(start + state.colRange.start, end + state.colRange.start)
                            winRange.id -> Win(start + state.winRange.start, end + state.winRange.start)
                            else -> throw IllegalStateException()
                        })
                    }

                    override fun onRangeChangeListener(rangeBar: RangeBar, leftPinIndex: Int, rightPinIndex: Int, leftPinValue: String?, rightPinValue: String?) {}
                    override fun onTouchStarted(rangeBar: RangeBar) {}
                }
                rowsRange.setOnRangeBarChangeListener(rangeListener)
                colsRange.setOnRangeBarChangeListener(rangeListener)
                winRange.setOnRangeBarChangeListener(rangeListener)
                awaitClose {
                    rowsRange.setOnRangeBarChangeListener(null)
                    colsRange.setOnRangeBarChangeListener(null)
                    winRange.setOnRangeBarChangeListener(null)
                }
            }
        }
    }
}

data class GameFindState(
        val rowRange: Range, val rows: Range,
        val colRange: Range, val cols: Range,
        val winRange: Range, val win: Range,
        val searching: Boolean
)

sealed class FindSettings
data class Rows(val start: Int, val end: Int) : FindSettings()
data class Cols(val start: Int, val end: Int) : FindSettings()
data class Win(val start: Int, val end: Int) : FindSettings()
data class MarkSettings(val mark: Mark) : FindSettings()
object FindGames : FindSettings()