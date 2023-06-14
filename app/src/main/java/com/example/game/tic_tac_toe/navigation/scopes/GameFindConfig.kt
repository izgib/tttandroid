package com.example.game.tic_tac_toe.navigation.scopes

import com.example.controllers.models.Range
import com.example.game.GameRules
import com.example.game.Mark
import com.zhuinden.simplestack.Bundleable
import com.zhuinden.statebundle.StateBundle

data class MutableParamRange(override var start: Int, override var end: Int) : Range

class GameFindConfig : Bundleable {
    var rows = MutableParamRange(GameRules.ROWS_MIN, GameRules.ROWS_MAX)
        private set
    var cols = MutableParamRange(GameRules.COLS_MIN, GameRules.COLS_MAX)
        private set
    var win = MutableParamRange(GameRules.WIN_MIN, GameRules.WIN_MAX)
        private set

    var mark: Mark = Mark.Empty
    var searching = false

    override fun toBundle(): StateBundle = StateBundle().apply {
        putByte("rowMin", rows.start.toByte())
        putByte("rowMax", rows.end.toByte())
        putByte("colMin", cols.start.toByte())
        putByte("colMax", cols.end.toByte())
        putByte("winMin", win.start.toByte())
        putByte("winMax", win.end.toByte())
        putByte("mark", mark.ordinal.toByte())
    }

    override fun fromBundle(bundle: StateBundle?) {
        bundle?.run {
            rows = MutableParamRange(getByte("rowMin").toInt(), getByte("rowMax").toInt())
            cols = MutableParamRange(getByte("colMin").toInt(), getByte("colMax").toInt())
            win = MutableParamRange(getByte("winMin").toInt(), getByte("winMax").toInt())
            mark = Mark.values()[getByte("mark").toInt()]
        }
    }
}