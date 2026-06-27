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
import com.vkreborn.ime.engine.EnglishShiftState

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
            rebuildKeyRects()
            invalidate()
        }

    var englishShiftState: EnglishShiftState = EnglishShiftState.OFF
        set(value) {
            field = value
            invalidateShiftKeyOnly()
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
    private val blueDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xff3aa7ff.toInt() }

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildKeyRects()
    }

    private fun rebuildKeyRects() {
        if (width <= 0 || height <= 0) return
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
                x += keyW + gap
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        if (keyRects.isEmpty()) rebuildKeyRects()
        keyRects.forEach { (rect, key) ->
            val paint = if (key == downKey) pressedPaint else keyPaint
            canvas.drawRoundRect(rect, 8f, 8f, paint)
            canvas.drawRoundRect(rect, 8f, 8f, strokePaint)
            drawLabel(canvas, key, rect)
            key.subLabel?.let { canvas.drawText(it, rect.right - 8f, rect.top + 22f, subPaint) }
            if (mode == KeyboardMode.ENGLISH && key.action.name == "SHIFT" && englishShiftState == EnglishShiftState.LOCK) {
                canvas.drawCircle(rect.left + 16f, rect.top + 16f, 8f, blueDotPaint)
            }
        }
    }

    private fun drawLabel(canvas: Canvas, key: VkKey, rect: RectF) {
        val lines = key.label.split("\n")
        if (lines.size == 1) {
            val label = if (mode == KeyboardMode.ENGLISH && key.action.name == "INPUT" && key.id.length == 1 && key.id[0].isLetter()) {
                when (englishShiftState) {
                    EnglishShiftState.OFF -> key.label.lowercase()
                    EnglishShiftState.ONCE, EnglishShiftState.LOCK -> key.label.uppercase()
                }
            } else key.label
            canvas.drawText(label, rect.centerX(), rect.centerY() + 12f, textPaint)
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
                    // v0.9.2 Turbo: 일반 입력은 ACTION_DOWN에서 즉시 처리한다.
                    // ACTION_UP까지 기다리면 빠른 타자에서 입력이 늦거나 누락되는 느낌이 생긴다.
                    listener?.onKey(key)
                    invalidateKey(key)

                    handler.postDelayed({
                        if (downKey == key) {
                            longFired = true
                            if (key.action.name == "DELETE") {
                                startDeleteRepeat(key)
                            } else {
                                // 일반 입력은 이미 처리되었으므로, Service 쪽에서
                                // 직전 일반 입력을 롱프레스 문자로 치환한다.
                                listener?.onLongKey(key)
                            }
                            invalidateKey(key)
                        }
                    }, 280)
                } ?: invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val key = findKey(event.x, event.y)
                if (key != downKey) {
                    val old = downKey
                    stopRepeats()
                    downKey = null
                    old?.let { invalidateKey(it) }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                val key = downKey
                stopRepeats()
                downKey = null
                key?.let { invalidateKey(it) } ?: invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                stopRepeats()
                val key = downKey
                downKey = null
                key?.let { invalidateKey(it) } ?: invalidate()
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
        // 전체 Handler 메시지를 지우면 빠른 연속 터치 시 다음 키의 롱프레스 예약까지 지워질 수 있다.
        // 현재 눌림 키 관련 반복 삭제만 제거한다.
    }

    private fun invalidateKey(key: VkKey) {
        keyRects.firstOrNull { it.second == key }?.first?.let { rect ->
            invalidate(rect.left.toInt() - 4, rect.top.toInt() - 4, rect.right.toInt() + 4, rect.bottom.toInt() + 4)
        } ?: invalidate()
    }

    private fun invalidateShiftKeyOnly() {
        if (mode != KeyboardMode.ENGLISH) {
            invalidate()
            return
        }
        keyRects.firstOrNull { it.second.action.name == "SHIFT" }?.second?.let { invalidateKey(it) } ?: invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun findKey(x: Float, y: Float): VkKey? = keyRects.firstOrNull { it.first.contains(x, y) }?.second
}
