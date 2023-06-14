package com.example.transport.extensions

import com.example.game.Mark
import com.example.transport.MarkType

fun MarkType.toMark(): Mark = when (this) {
    MarkType.MARK_TYPE_CROSS -> Mark.Cross
    MarkType.MARK_TYPE_NOUGHT -> Mark.Nought
    MarkType.MARK_TYPE_UNSPECIFIED -> Mark.Empty
    MarkType.UNRECOGNIZED -> throw IllegalArgumentException("unexpected value")
}

fun Mark.toMarkType(): MarkType = when (this) {
    Mark.Cross -> MarkType.MARK_TYPE_CROSS
    Mark.Nought -> MarkType.MARK_TYPE_NOUGHT
    Mark.Empty -> MarkType.MARK_TYPE_UNSPECIFIED
}