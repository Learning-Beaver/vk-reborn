package com.vkreborn.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import com.vkreborn.ime.engine.KeyAction
import com.vkreborn.ime.engine.KeyboardMode
import com.vkreborn.ime.engine.VkKey
import com.vkreborn.ime.engine.RuleTable
import com.vkreborn.ime.engine.EnglishShiftState
import com.vkreborn.ime.ui.VkKeyboardView

class VkRebornImeService : InputMethodService(), VkKeyboardView.Listener {
    private lateinit var keyboardView: VkKeyboardView
    private val composer = HangulComposer()
    private val engine = VanchuEngine()
    private var englishShiftState = EnglishShiftState.OFF
    private var ignoreNextSelectionUpdate = false

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
        if (::keyboardView.isInitialized) {
            keyboardView.mode = KeyboardMode.HANGUL
            keyboardView.englishShiftState = englishShiftState
        }
    }


    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)

        // v0.9.9 Compose Recovery:
        // setComposingText() 직후 Android가 selection update를 보내는데, 이를 사용자 커서 이동으로
        // 오인하면 composer가 즉시 초기화되어 1+3이 "ㄱㅏ"로 분리된다.
        // 내부 조합 갱신으로 발생한 selection update는 1회 무시하고, 그 외의 실제 커서 이동만
        // composer를 종료한다.
        if (ignoreNextSelectionUpdate) {
            ignoreNextSelectionUpdate = false
            return
        }

        if (newSelStart != oldSelStart || newSelEnd != oldSelEnd) {
            if (composer.text().isNotEmpty()) {
                currentInputConnection?.finishComposingText()
                composer.clear()
                engine.reset()
            }
        }
    }

    override fun onFinishInput() {
        commitComposer()
        super.onFinishInput()
    }

    override fun onKey(key: VkKey) {
        val ic = currentInputConnection ?: return
        when (key.action) {
            KeyAction.DELETE -> {
                ic.beginBatchEdit()
                try {
                    if (!composer.backspace()) {
                        engine.reset()
                        ic.deleteSurroundingText(1, 0)
                    } else {
                        engine.resetAfterEdit()
                    }
                    val text = composer.text()
                    if (text.isEmpty()) ic.finishComposingText() else setComposingTextSafe(text)
                } finally {
                    ic.endBatchEdit()
                }
            }
            KeyAction.SPACE -> { commitComposer(); ic.commitText(" ", 1) }
            KeyAction.ENTER -> handleEnter()
            KeyAction.HIDE -> requestHideSelf(0)
            KeyAction.MODE_HANGUL -> setMode(KeyboardMode.HANGUL)
            KeyAction.MODE_NUMBER -> { commitComposer(); setMode(KeyboardMode.NUMBER) }
            KeyAction.MODE_SYMBOL -> { commitComposer(); setMode(KeyboardMode.SYMBOL) }
            KeyAction.MODE_SYMBOL2 -> { commitComposer(); setMode(KeyboardMode.SYMBOL2) }
            KeyAction.MODE_ENGLISH -> { commitComposer(); setMode(KeyboardMode.ENGLISH) }
            KeyAction.SHIFT -> handleShift()
            KeyAction.INPUT -> {
                if (keyboardView.mode == KeyboardMode.HANGUL && RuleTable.hangulRules.containsKey(key.id)) {
                    val rawOutput = engine.press(key.id)
                    val replace = engine.wasReplacement && composer.canReplaceLastJamoOnRepeat(rawOutput)
                    val output = if (engine.wasReplacement && !replace) {
                        // v0.9.7 Hotfix: 호빵
                        // 호 + ㅂ + ㅂ + ㅏ + ㅇ => 호빵
                        // 종성 ㅂ 상태에서 같은 7키를 한 번 더 누르면 받침 ㅂ을 다음 초성 ㅃ으로 이동한다.
                        if (key.id == "7" && rawOutput == 'ㅃ' && composer.promoteFinalBieupToNextSsangBieup()) {
                            engine.restartCurrentKeyAsNew(key.id)
                            '\u0000'
                        } else {
                            // 반복 입력이 직전 자모 대체가 아니라 새 음절/새 자모 시작이어야 하는 경우.
                            // 예: 0+3+8+8+8 => 았ㅅ, 세 번째 8은 ㅆ이 아니라 새 ㅅ이다.
                            engine.restartCurrentKeyAsNew(key.id)
                            engine.normalChar(key.id)
                        }
                    } else {
                        rawOutput
                    }
                    ic.beginBatchEdit()
                    try {
                        if (output != '\u0000') {
                            composer.replaceLastJamoOrAppend(output, replace)
                        }
                        setComposingTextSafe(composer.text())
                    } finally {
                        ic.endBatchEdit()
                    }
                } else if (keyboardView.mode == KeyboardMode.ENGLISH && key.id.length == 1 && key.id[0].isLetter()) {
                    commitComposer()
                    val ch = when (englishShiftState) {
                        EnglishShiftState.OFF -> key.id.lowercase()
                        EnglishShiftState.ONCE, EnglishShiftState.LOCK -> key.id.uppercase()
                    }
                    ic.commitText(ch, 1)
                    if (englishShiftState == EnglishShiftState.ONCE) {
                        englishShiftState = EnglishShiftState.OFF
                        keyboardView.englishShiftState = englishShiftState
                    }
                } else {
                    commitComposer(); ic.commitText(key.label, 1)
                }
            }
        }
    }

    override fun onLongKey(key: VkKey) {
        val ic = currentInputConnection ?: return
        if (key.action == KeyAction.DELETE) {
            // ACTION_DOWN에서 이미 1회 삭제되었으므로, 롱프레스 반복은 추가 삭제만 수행한다.
            onKey(key)
            return
        }

        // v0.9.8: 롱프레스 문자는 현재 키보드 모드 기준으로 결정한다.
        // 숫자 키보드에서는 1 길게 -> !, 2 길게 -> @ 처럼 key.subLabel을 사용해야 한다.
        // 이전 버전은 숫자키 id가 한글 RuleTable과 겹쳐 1 길게 -> 1로 치환되어
        // 겉으로는 아무 동작도 하지 않는 것처럼 보였다.
        val hangulRule = if (keyboardView.mode == KeyboardMode.HANGUL) RuleTable.hangulRules[key.id] else null
        val text = hangulRule?.longPress ?: key.subLabel ?: return

        ic.beginBatchEdit()
        try {
            // 일반 입력은 ACTION_DOWN에서 이미 들어갔다.
            // 롱프레스 확정 시 직전 일반 입력을 제거하고 보조문자로 치환한다.
            if (keyboardView.mode == KeyboardMode.HANGUL && hangulRule != null) {
                if (composer.backspace()) {
                    engine.resetAfterEdit()
                    val composing = composer.text()
                    if (composing.isEmpty()) ic.finishComposingText() else setComposingTextSafe(composing)
                }
            } else {
                // 영문/숫자/SYM은 ACTION_DOWN에서 이미 commit된 글자를 현재 커서 기준으로 1글자 삭제 후 치환한다.
                commitComposer()
                ic.deleteSurroundingText(1, 0)
            }
            commitComposer()
            ic.commitText(text, 1)
        } finally {
            ic.endBatchEdit()
        }
    }

    private fun handleShift() {
        if (keyboardView.mode == KeyboardMode.ENGLISH) {
            englishShiftState = when (englishShiftState) {
                EnglishShiftState.OFF -> EnglishShiftState.ONCE
                EnglishShiftState.ONCE -> EnglishShiftState.LOCK
                EnglishShiftState.LOCK -> EnglishShiftState.OFF
            }
            keyboardView.englishShiftState = englishShiftState
            return
        }

        val ic = currentInputConnection ?: return
        val toggled = engine.toggleHan() ?: return
        ic.beginBatchEdit()
        try {
            composer.replaceLastJamoOrAppend(toggled, true)
            setComposingTextSafe(composer.text())
        } finally {
            ic.endBatchEdit()
        }
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
        if (mode != KeyboardMode.ENGLISH) {
            englishShiftState = EnglishShiftState.OFF
        }
        keyboardView.mode = mode
        keyboardView.englishShiftState = englishShiftState
    }


    private fun setComposingTextSafe(text: String) {
        val ic = currentInputConnection ?: return
        ignoreNextSelectionUpdate = true
        ic.setComposingText(text, 1)
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
