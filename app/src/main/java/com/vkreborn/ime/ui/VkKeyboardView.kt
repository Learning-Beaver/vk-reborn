package com.vkreborn.ime.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.vkreborn.ime.engine.KeyboardLayoutModel
import com.vkreborn.ime.engine.KeyboardMode
import com.vkreborn.ime.engine.VkKey

class VkKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface Listener {
        fun onKey(key: VkKey)
        fun onLongKey(key: VkKey)
    }

    var listener: Listener? = null
    var mode: KeyboardMode = KeyboardMode.HANGUL
        set(value) {
            field = value
            downKey = null
            invalidate()
        }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xff202020.toInt() }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xffeeeeee.toInt() }
    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xffd8d8d8.toInt() }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xff9e9e9e.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xff111111.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 34f
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xff666666.toInt()
        textAlign = Paint.Align.RIGHT
        textSize = 18f
    }

    private val keyRects = mutableListOf<Pair<RectF, VkKey>>()
    private val handler = Handler(Looper.getMainLooper())
    private var downKey: VkKey? = null
    private var longFired = false
    private var deleteRepeatRunnable: Runnable? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = (width * 0.58f).toInt().coerceAtLeast(300)
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        keyRects.clear()
        val rows = KeyboardLayoutModel.forMode(mode)
        val gap = 5f
        val rowH = height.toFloat() / rows.size
        rows.forEachIndexed { r, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            var x = gap
            val y = r * rowH + gap
            row.forEach { key ->
                val keyW = (width - gap * (row.size + 1)) * (key.weight / totalWeight)
                val rect = RectF(x, y, x + keyW, y + rowH - gap)
                keyRects.add(rect to key)
                val paint = if (key == downKey) pressedPaint else keyPaint
                canvas.drawRoundRect(rect, 8f, 8f, paint)
                canvas.drawRoundRect(rect, 8f, 8f, strokePaint)
                drawLabel(canvas, key, rect)
                key.subLabel?.let { canvas.drawText(it, rect.right - 8f, rect.top + 22f, subPaint) }
                x += keyW + gap
            }
        }
    }

    private fun drawLabel(canvas: Canvas, key: VkKey, rect: RectF) {
        val lines = key.label.split("\n")
        if (lines.size == 1) {
            canvas.drawText(key.label, rect.centerX(), rect.centerY() + 12f, textPaint)
        } else {
            canvas.drawText(lines[0], rect.centerX(), rect.centerY() - 2f, textPaint)
            canvas.drawText(lines[1], rect.centerX(), rect.centerY() + 26f, textPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downKey = findKey(event.x, event.y)
                longFired = false
                downKey?.let { key ->
                    handler.postDelayed({
                        if (downKey == key) {
                            longFired = true
                            if (key.action.name == "DELETE") {
                                startDeleteRepeat(key)
                            } else {
                                listener?.onLongKey(key)
                            }
                            invalidate()
                        }
                    }, 420)
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                val key = downKey
                stopRepeats()
                downKey = null
                if (key != null && !longFired) listener?.onKey(key)
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                stopRepeats()
                downKey = null
                invalidate()
                return true
            }
        }
        return true
    }


    private fun startDeleteRepeat(key: VkKey) {
        val runnable = object : Runnable {
            override fun run() {
                if (downKey == key) {
                    listener?.onLongKey(key)
                    handler.postDelayed(this, 70)
                }
            }
        }
        deleteRepeatRunnable = runnable
        handler.post(runnable)
    }

    private fun stopRepeats() {
        deleteRepeatRunnable?.let { handler.removeCallbacks(it) }
        deleteRepeatRunnable = null
        handler.removeCallbacksAndMessages(null)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun findKey(x: Float, y: Float): VkKey? = keyRects.firstOrNull { it.first.contains(x, y) }?.second
}
