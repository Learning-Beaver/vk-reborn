package com.vkreborn.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import com.vkreborn.ime.engine.KeyAction
import com.vkreborn.ime.engine.KeyboardMode
import com.vkreborn.ime.engine.VkKey
import com.vkreborn.ime.engine.RuleTable
import com.vkreborn.ime.ui.VkKeyboardView

class VkRebornImeService : InputMethodService(), VkKeyboardView.Listener {
    private lateinit var keyboardView: VkKeyboardView
    private val composer = HangulComposer()
    private val engine = VanchuEngine()

    override fun onCreateInputView(): View {
        keyboardView = VkKeyboardView(this)
        keyboardView.listener = this
        keyboardView.mode = KeyboardMode.HANGUL
        return keyboardView
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        composer.clear()
        engine.reset()
        if (::keyboardView.isInitialized) keyboardView.mode = KeyboardMode.HANGUL
    }

    override fun onFinishInput() {
        commitComposer()
        super.onFinishInput()
    }

    override fun onKey(key: VkKey) {
        val ic = currentInputConnection ?: return
        when (key.action) {
            KeyAction.DELETE -> {
                if (!composer.backspace()) {
                    engine.reset()
                    ic.deleteSurroundingText(1, 0)
                } else {
                    engine.resetAfterEdit()
                }
                val text = composer.text()
                if (text.isEmpty()) ic.finishComposingText() else ic.setComposingText(text, 1)
            }
            KeyAction.SPACE -> { commitComposer(); ic.commitText(" ", 1) }
            KeyAction.ENTER -> handleEnter()
            KeyAction.HIDE -> requestHideSelf(0)
            KeyAction.MODE_HANGUL -> setMode(KeyboardMode.HANGUL)
            KeyAction.MODE_NUMBER -> { commitComposer(); setMode(KeyboardMode.NUMBER) }
            KeyAction.MODE_SYMBOL -> { commitComposer(); setMode(KeyboardMode.SYMBOL) }
            KeyAction.MODE_ENGLISH -> { commitComposer(); setMode(KeyboardMode.ENGLISH) }
            KeyAction.SHIFT -> handleShift()
            KeyAction.INPUT -> {
                if (keyboardView.mode == KeyboardMode.HANGUL && RuleTable.hangulRules.containsKey(key.id)) {
                    val rawOutput = engine.press(key.id)
                    val replace = engine.wasReplacement && composer.canReplaceLastJamoOnRepeat(rawOutput)
                    val output = if (engine.wasReplacement && !replace) {
                        // 반복 입력이 직전 자모 대체가 아니라 새 음절/새 자모 시작이어야 하는 경우.
                        // 예: 0+3+8+8+8 => 았ㅅ, 세 번째 8은 ㅆ이 아니라 새 ㅅ이다.
                        engine.restartCurrentKeyAsNew(key.id)
                        engine.normalChar(key.id)
                    } else {
                        rawOutput
                    }
                    composer.replaceLastJamoOrAppend(output, replace)
                    ic.setComposingText(composer.text(), 1)
                } else {
                    commitComposer(); ic.commitText(key.label, 1)
                }
            }
        }
    }

    override fun onLongKey(key: VkKey) {
        val ic = currentInputConnection ?: return
        if (key.action == KeyAction.DELETE) {
            onKey(key)
            return
        }
        val rule = RuleTable.hangulRules[key.id]
        val text = rule?.longPress ?: key.subLabel ?: return
        commitComposer()
        ic.commitText(text, 1)
    }

    private fun handleShift() {
        val ic = currentInputConnection ?: return
        val toggled = engine.toggleHan() ?: return
        composer.replaceLastJamoOrAppend(toggled, true)
        ic.setComposingText(composer.text(), 1)
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        commitComposer()

        val imeOptions = currentInputEditorInfo?.imeOptions ?: 0
        val action = imeOptions and EditorInfo.IME_MASK_ACTION

        when (action) {
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_PREVIOUS -> {
                ic.performEditorAction(action)
            }
            else -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
        }
    }

    private fun setMode(mode: KeyboardMode) {
        engine.reset()
        keyboardView.mode = mode
    }

    private fun commitComposer() {
        val ic = currentInputConnection ?: return
        val text = composer.text()
        if (text.isNotEmpty()) {
            ic.commitText(text, 1)
            ic.finishComposingText()
            composer.clear()
            engine.reset()
        }
    }
}
