package com.example.game.tic_tac_toe.utils

import android.widget.ImageView
import androidx.databinding.*
import com.appyvet.materialrangebar.RangeBar
import com.example.game.domain.game.Mark
import com.example.game.tic_tac_toe.R

object TicTacToeBindingAdapter {
    @BindingAdapter("gameMark")
    @JvmStatic
    fun setMark(view: ImageView, mark: Mark) {
        when (mark) {
            Mark.Cross -> view.setImageResource(R.drawable.ic_cross)
            Mark.Nought -> view.setImageResource(R.drawable.ic_nought)
            Mark.Empty -> {
            }
        }
    }

    @BindingAdapter("mrb_tickStart")
    @JvmStatic
    fun setTickStart(view: RangeBar, tickStart: Float) {
        view.tickStart = tickStart
    }

    @BindingAdapter("mrb_tickEnd")
    @JvmStatic
    fun setTickEnd(view: RangeBar, tickEnd: Float) {
        if (view.tickEnd != tickEnd) {
            view.tickEnd = tickEnd
        }
    }

    @InverseBindingAdapter(attribute = "mrb_tickEnd", event = "mrb_tickEndAttrChanged")
    @JvmStatic
    fun getTickEnd(view: RangeBar) = view.tickEnd

    @BindingAdapter("mrb_tickEndAttrChanged")
    @JvmStatic
    fun setListeners(view: RangeBar, attrChanged: InverseBindingListener) {

    }
}

@InverseBindingMethods(
        InverseBindingMethod(type = RangeBar::class, attribute = "leftIndex", event = "leftIndexAttrChanged"),
        InverseBindingMethod(type = RangeBar::class, attribute = "rightIndex", event = "rightIndexAttrChanged")
)
object RangeBarBindingAdapter {
    @BindingAdapter(value = ["leftIndex", "rightIndex"])
    @JvmStatic
    fun setPinIndexes(view: RangeBar, leftIndex: Int, rightIndex: Int) {
        if (view.leftIndex != leftIndex || view.rightIndex != rightIndex) {
            view.setRangePinsByIndices(leftIndex, rightIndex)
        }
    }

    @BindingAdapter(value = ["onTouchStarted", "onTouchEnded", "onRangeChanged", "leftIndexAttrChanged", "rightIndexAttrChanged"], requireAll = false)
    @JvmStatic
    fun setOnRangeBarChangeListener(view: RangeBar,
                                    start: OnTouchStarted?,
                                    end: OnTouchEnded?,
                                    rangeChanged: OnRangeChanged?,
                                    leftIndexChanged: InverseBindingListener?,
                                    rightIndexChanged: InverseBindingListener?
    ) {
        if (start == null && end == null && rangeChanged == null && leftIndexChanged == null && rightIndexChanged == null) {
            view.setOnRangeBarChangeListener(null)
        } else {
            view.setOnRangeBarChangeListener(object : RangeBar.OnRangeBarChangeListener {
                var leftIndex = 0
                var rightIndex = 0
                override fun onTouchEnded(rangeBar: RangeBar) {
                    end?.onTouchEnded(rangeBar)
                }

                override fun onRangeChangeListener(rangeBar: RangeBar?, leftPinIndex: Int, rightIndex: Int, leftValue: String?, rightPinValue: String?) {
                    rangeChanged?.onRangeChanged(rangeBar, leftPinIndex, rightIndex, leftValue, rightPinValue)
                    if (leftIndex != leftPinIndex) {
                        leftIndexChanged?.onChange()
                    }
                    if (this.rightIndex != rightIndex) {
                        rightIndexChanged?.onChange()
                    }
                }

                override fun onTouchStarted(rangeBar: RangeBar) {
                    leftIndex = rangeBar.leftIndex
                    rightIndex = rangeBar.rightIndex
                    start?.onTouchStarted(rangeBar)
                }
            })
        }
    }

    interface OnTouchStarted {
        fun onTouchStarted(rangeBar: RangeBar)
    }

    interface OnTouchEnded {
        fun onTouchEnded(rangeBar: RangeBar)
    }

    interface OnRangeChanged {
        fun onRangeChanged(rangeBar: RangeBar?, leftIndex: Int, rightIndex: Int, leftPinValue: String?, rightPinValue: String?)
    }
}

object RangeConverter {
    @InverseMethod("set")
    @JvmStatic
    fun get(value: Int): Float {
        return value.toFloat()
    }

    @JvmStatic
    fun set(value: Float): Int {
        return value.toInt()
    }
}
