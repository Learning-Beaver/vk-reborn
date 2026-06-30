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

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xffcfcfcf.toInt() }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xfff7f7f7.toInt() }
    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xffdddddd.toInt() }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xffa8a8a8.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.4f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xff111111.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 30f
    }
    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xff8f8f8f.toInt()
        textAlign = Paint.Align.RIGHT
        textSize = 19f
    }
    private val blueDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xff39aaff.toInt() }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33000000 }

    private val keyRects = mutableListOf<Pair<RectF, VkKey>>()
    private val handler = Handler(Looper.getMainLooper())
    private var downKey: VkKey? = null
    private var longFired = false
    private var deleteRepeatRunnable: Runnable? = null
    private var longPressRunnable: Runnable? = null
    private var touchSequence = 0

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
        val gap = dp(3f)
        val outerTop = dp(8f)
        val outerBottom = dp(8f)
        val rowH = (height.toFloat() - outerTop - outerBottom) / rows.size
        rows.forEachIndexed { r, row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            var x = gap
            val y = outerTop + r * rowH + gap
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
            drawKeyBackground(canvas, rect, key == downKey)
            drawLabel(canvas, key, rect)
            key.subLabel?.let { canvas.drawText(it, rect.right - dp(4f), rect.top + dp(14f), subPaint) }
            if (mode == KeyboardMode.ENGLISH && key.action.name == "SHIFT" && englishShiftState == EnglishShiftState.LOCK) {
                canvas.drawCircle(rect.left + dp(9f), rect.top + dp(9f), dp(5f), blueDotPaint)
            }
        }
    }

    private fun drawKeyBackground(canvas: Canvas, rect: RectF, pressed: Boolean) {
        val radius = dp(5f)
        val shadowRect = RectF(rect.left + dp(1f), rect.top + dp(2f), rect.right + dp(1f), rect.bottom + dp(2f))
        canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint)
        val paint = if (pressed) pressedPaint else keyPaint
        canvas.drawRoundRect(rect, radius, radius, paint)
        canvas.drawRoundRect(rect, radius, radius, strokePaint)
        if (!pressed) {
            // 원본 반츄처럼 상단은 밝고 하단은 약간 어두운 느낌을 주기 위한 얇은 하이라이트
            val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x55ffffff
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val inset = dp(1f)
            val inner = RectF(rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset)
            canvas.drawRoundRect(inner, radius - dp(1f), radius - dp(1f), highlight)
        }
    }

    private fun drawLabel(canvas: Canvas, key: VkKey, rect: RectF) {
        val lines = key.label.split("\n")
        textPaint.textSize = when {
            key.label == "ENTER" || key.label == "DEL" -> dp(18f)
            key.action.name.startsWith("MODE") || key.action.name == "SHIFT" || key.action.name == "SPACE" || key.action.name == "HIDE" -> dp(20f)
            else -> dp(24f)
        }
        val label = if (mode == KeyboardMode.ENGLISH && key.action.name == "INPUT" && key.id.length == 1 && key.id[0].isLetter()) {
            when (englishShiftState) {
                EnglishShiftState.OFF -> key.label.lowercase()
                EnglishShiftState.ONCE, EnglishShiftState.LOCK -> key.label.uppercase()
            }
        } else key.label

        when {
            key.action.name == "DELETE" -> drawDeleteKey(canvas, rect)
            key.action.name == "SPACE" -> drawSpaceKey(canvas, rect)
            lines.size == 1 -> canvas.drawText(label, rect.centerX(), rect.centerY() + dp(3f), textPaint)
            else -> {
                textPaint.textSize = dp(18f)
                canvas.drawText(lines[0], rect.centerX(), rect.centerY() - dp(5f), textPaint)
                canvas.drawText(lines[1], rect.centerX(), rect.centerY() + dp(18f), textPaint)
            }
        }
    }

    private fun drawSpaceKey(canvas: Canvas, rect: RectF) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xff111111.toInt()
            style = Paint.Style.STROKE
            strokeWidth = dp(3f)
            strokeCap = Paint.Cap.SQUARE
        }
        val w = rect.width() * 0.22f
        val x1 = rect.centerX() - w / 2
        val x2 = rect.centerX() + w / 2
        val y = rect.centerY() + dp(3f)
        canvas.drawLine(x1, y, x1, y - dp(8f), p)
        canvas.drawLine(x1, y, x2, y, p)
        canvas.drawLine(x2, y, x2, y - dp(8f), p)
    }

    private fun drawDeleteKey(canvas: Canvas, rect: RectF) {
        textPaint.textSize = dp(18f)
        canvas.drawText("DEL", rect.centerX(), rect.centerY() - dp(8f), textPaint)
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xff111111.toInt()
            style = Paint.Style.STROKE
            strokeWidth = dp(2.5f)
        }
        val cx = rect.centerX()
        val cy = rect.centerY() + dp(15f)
        val w = rect.width() * 0.28f
        val h = dp(18f)
        val path = android.graphics.Path().apply {
            moveTo(cx - w / 2, cy)
            lineTo(cx - w / 2 + dp(10f), cy - h / 2)
            lineTo(cx + w / 2, cy - h / 2)
            lineTo(cx + w / 2, cy + h / 2)
            lineTo(cx - w / 2 + dp(10f), cy + h / 2)
            close()
        }
        canvas.drawPath(path, p)
        textPaint.textSize = dp(18f)
        canvas.drawText("×", cx + dp(8f), cy + dp(6f), textPaint)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // v0.9.5: 탭 입력과 롱프레스 판정을 완전히 분리한다.
                // 이전 구현은 ACTION_UP에서 예약 콜백을 제거하지 않아 빠른 반복 입력 시
                // 이전 탭의 롱프레스 콜백이 다음 탭에 붙어 오동작할 수 있었다.
                cancelPendingLongPress()
                touchSequence++

                downKey = findKey(event.x, event.y)
                longFired = false
                downKey?.let { key ->
                    val seq = touchSequence

                    // 일반 입력은 즉시 처리해 반응속도를 유지한다.
                    listener?.onKey(key)
                    invalidateKey(key)

                    val runnable = Runnable {
                        if (touchSequence == seq && downKey == key) {
                            longFired = true
                            if (key.action.name == "DELETE") {
                                startDeleteRepeat(key)
                            } else {
                                // 롱프레스가 확정된 경우에만 직전 일반 입력을 보조문자로 치환한다.
                                listener?.onLongKey(key)
                            }
                            invalidateKey(key)
                        }
                    }
                    longPressRunnable = runnable
                    handler.postDelayed(runnable, 300)
                } ?: invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val key = findKey(event.x, event.y)
                if (key != downKey) {
                    val old = downKey
                    stopRepeats()
                    cancelPendingLongPress()
                    downKey = null
                    old?.let { invalidateKey(it) }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                val key = downKey
                stopRepeats()
                cancelPendingLongPress()
                downKey = null
                key?.let { invalidateKey(it) } ?: invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                stopRepeats()
                cancelPendingLongPress()
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
    }

    private fun cancelPendingLongPress() {
        longPressRunnable?.let { handler.removeCallbacks(it) }
        longPressRunnable = null
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
