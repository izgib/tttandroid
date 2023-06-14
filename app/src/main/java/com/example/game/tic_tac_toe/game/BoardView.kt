package com.example.game.tic_tac_toe.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.game.Coord
import com.example.game.Mark
import com.example.game.MarkLists
import com.example.game.tic_tac_toe.R
import java.nio.InvalidMarkException
import kotlin.math.abs


//class BoardView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
class BoardView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    private val crossRes = ContextCompat.getDrawable(context, R.drawable.ic_cross)
    private val noughtRes = ContextCompat.getDrawable(context, R.drawable.ic_nought)
    private var SQUARE_SIZE: Int = 0
    private var FIELD_OFFSET_x: Float = 0F
    private var FIELD_OFFSET_y: Float = 0F

    private var drawField: Int = 0
    private lateinit var crossBitmap: Bitmap
    private lateinit var noughtBitmap: Bitmap
    private lateinit var extraBitmap: Bitmap
    private lateinit var extraCanvas: Canvas

    private var cols = 3
    private var rows = 3
    private var xMarks: List<Coord> = emptyList()
    private var oMarks: List<Coord> = emptyList()

    private val paint: Paint = Paint()
    private val crossPaint: Paint
    private val noughtPaint: Paint
    private val paints: Array<Paint>

    private var mListener: OnPlayerMoveListener? = null

    private var lastTouchDown: Long = 0
    private var startTouchX = -1f
    private var startTouchY = -1f
    private var touchThreshold = ViewConfiguration.get(context).scaledTouchSlop

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchDown = System.currentTimeMillis()
                startTouchX = event.x
                startTouchY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHOLD) {
                    if (abs(event.x - startTouchX) < touchThreshold &&
                            abs(event.y - startTouchY) < touchThreshold) {
                        if (!((FIELD_OFFSET_x < event.x) && (event.x < FIELD_OFFSET_y + SQUARE_SIZE * cols)))
                            return false

                        if (!((FIELD_OFFSET_x < event.y) && (event.y < FIELD_OFFSET_y + SQUARE_SIZE * rows)))
                            return false

                        val move = xy2ij(event.x, event.y)
                        Log.d(TAG, "move{${move.row},${move.col}} sent")
                        mListener?.onPlayerMove(this, move)
                    }
                    return false
                }
                return false
            }
            else -> return true
        }
    }

    companion object {
        const val fieldPaintWidth: Float = 8f
        const val crossPaintWidth: Float = 20f
        const val noughtPaintWidth: Float = 20f
        private const val CLICK_ACTION_THRESHOLD = 200

        const val TAG = "BoardView"
    }

    init {
        paint.strokeWidth = fieldPaintWidth

        crossPaint = Paint().apply {
            color = Color.RED
            strokeWidth = crossPaintWidth
        }

        noughtPaint = Paint().apply {
            color = Color.BLUE
            strokeWidth = noughtPaintWidth
            style = Paint.Style.STROKE
        }

        paints = arrayOf(crossPaint, noughtPaint)
    }

    override fun onDraw(canvas: Canvas) {
        Log.d(TAG, "onDraw call")
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "size changed from: $oldw:$oldh -> $w:$h")
        if (::extraBitmap.isInitialized) extraBitmap.recycle()

        extraBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(Color.WHITE)
        drawField = minOf(w, h)

        SQUARE_SIZE = minOf(drawField / cols, drawField / rows)
        FIELD_OFFSET_x = (drawField.rem(cols) / 2).toFloat()
        FIELD_OFFSET_y = (drawField.rem(rows) / 2).toFloat()

        val ier = SQUARE_SIZE / 100f
        paint.strokeWidth = fieldPaintWidth * ier / 2
        crossPaint.strokeWidth = crossPaintWidth * ier
        noughtPaint.strokeWidth = noughtPaintWidth * ier

        crossBitmap = vector2bitmap(crossRes!!, SQUARE_SIZE)
        noughtBitmap = vector2bitmap(noughtRes!!, SQUARE_SIZE)

        drawField()
    }

    fun initGame(rows: Int, cols: Int) {
        this.rows = rows
        this.cols = cols
    }

    fun initMoves(markLists: MarkLists) {
        xMarks = markLists.crosses
        oMarks = markLists.noughts
    }


    fun drawField() {
        if (rows == 0 && cols == 0) {
            throw IllegalStateException("game was not initialized")
        }

        for (i in 1 until rows) {
            extraCanvas.drawLine(
                    FIELD_OFFSET_x, SQUARE_SIZE * i + FIELD_OFFSET_y,
                    SQUARE_SIZE * cols + FIELD_OFFSET_x, SQUARE_SIZE * i + FIELD_OFFSET_y,
                    paint
            )
        }
        for (i in 1 until cols) {
            extraCanvas.drawLine(
                    SQUARE_SIZE * i + FIELD_OFFSET_x, FIELD_OFFSET_y,
                    SQUARE_SIZE * i + FIELD_OFFSET_x, SQUARE_SIZE * rows + FIELD_OFFSET_y,
                    paint
            )
        }
        for (move in xMarks) {
            putX(move.row, move.col)
        }
        for (move in oMarks) {
            putO(move.row, move.col)
        }

        Log.d("View", "field drawn")
    }

    fun putX(i: Int, j: Int) {
        extraCanvas.drawBitmap(crossBitmap, FIELD_OFFSET_x + SQUARE_SIZE * j,
                FIELD_OFFSET_y + SQUARE_SIZE * i, null)
    }


    fun putO(i: Int, j: Int) {
        extraCanvas.drawBitmap(noughtBitmap, FIELD_OFFSET_x + SQUARE_SIZE * j,
                FIELD_OFFSET_x + SQUARE_SIZE * i, null)
    }

    fun putWLine(i1: Int, j1: Int, i2: Int, j2: Int, player: Mark) {
        val paint: Paint = when (player) {
            Mark.Cross -> crossPaint
            Mark.Nought -> noughtPaint
            else -> throw InvalidMarkException()
        }

        Log.d(TAG, "drawing line from ($i1,$j1) to ($i2,$j2)")
        var x1: Float = (j1 + 0.5f) * SQUARE_SIZE + FIELD_OFFSET_x
        var y1: Float = (i1 + 0.5f) * SQUARE_SIZE + FIELD_OFFSET_y
        var x2: Float = (j2 + 0.5f) * SQUARE_SIZE + FIELD_OFFSET_x
        var y2: Float = (i2 + 0.5f) * SQUARE_SIZE + FIELD_OFFSET_y

        var ier = Integer.signum(i2 - i1)
        y2 += ier * 0.5f * SQUARE_SIZE
        y1 -= ier * 0.5f * SQUARE_SIZE

        ier = Integer.signum(j2 - j1)
        x2 += ier * 0.5f * SQUARE_SIZE
        x1 -= ier * 0.5f * SQUARE_SIZE

        extraCanvas.drawLine(x1, y1, x2, y2, paint)
        invalidate()
    }

    fun updateCanvas() {
        invalidate()
    }

    fun clearField() {
        extraBitmap.eraseColor(Color.TRANSPARENT)
        drawField()
        invalidate()
    }

    private fun vector2bitmap(drawable: Drawable, size: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }

    fun setOnPlayerMoveListener(listener: OnPlayerMoveListener?) {
        mListener = listener
    }

    private fun xy2ij(x: Float, y: Float): Coord {
        return Coord(
                ((y - FIELD_OFFSET_y) / SQUARE_SIZE).toInt(),
                ((x - FIELD_OFFSET_x) / SQUARE_SIZE).toInt()
        )
    }

    interface OnPlayerMoveListener {
        fun onPlayerMove(board: BoardView, move: Coord)
    }
}