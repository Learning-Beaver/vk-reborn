package com.vkreborn.ime

import com.vkreborn.ime.engine.RuleTable

/**
 * VK Reborn v0.6 rule engine.
 * - 같은 키 반복: 직전 자모 대체 (예: 1 -> ㄱ, 1+1 -> ㄲ)
 * - [한] 키: 직전 자모 토글 (예: ㄱ <-> ㅋ, ㅏ <-> ㅑ)
 * - 9번 키: 9 -> ㅣ, 99 -> ㅡ, 999 -> ㅢ
 */
class VanchuEngine {
    private var lastKeyId: String? = null
    private var repeatCount: Int = 0
    private var toggled: Boolean = false

    var wasReplacement: Boolean = false
        private set

    fun press(keyId: String): Char {
        val sameKey = lastKeyId == keyId && !toggled
        if (sameKey) {
            repeatCount++
        } else {
            lastKeyId = keyId
            repeatCount = 1
            toggled = false
        }
        wasReplacement = sameKey
        return baseCharForCurrentState()
    }

    fun toggleHan(): Char? {
        val keyId = lastKeyId ?: return null
        val rule = RuleTable.hangulRules[keyId] ?: return null
        val hasToggle = if (repeatCount >= 2) rule.shiftRepeat != null else rule.shiftNormal != null
        if (!hasToggle) return null
        toggled = !toggled
        wasReplacement = true
        return if (toggled) toggleCharForCurrentState() else baseCharForCurrentState()
    }

    private fun baseCharForCurrentState(): Char {
        val keyId = lastKeyId ?: return '\u0000'
        val rule = RuleTable.hangulRules.getValue(keyId)
        return when {
            repeatCount >= 3 && rule.third != null -> rule.third.first()
            repeatCount >= 2 -> (rule.repeat ?: rule.normal).first()
            else -> rule.normal.first()
        }
    }

    private fun toggleCharForCurrentState(): Char {
        val keyId = lastKeyId ?: return '\u0000'
        val rule = RuleTable.hangulRules.getValue(keyId)
        return if (repeatCount >= 2) (rule.shiftRepeat ?: rule.shiftNormal ?: rule.repeat ?: rule.normal).first()
        else (rule.shiftNormal ?: rule.normal).first()
    }

    fun resetAfterEdit() {
        lastKeyId = null
        repeatCount = 0
        toggled = false
        wasReplacement = false
    }

    fun reset() = resetAfterEdit()
}
