package com.vkreborn.ime

/**
 * VK Reborn v0.6 Hangul composer.
 * - 자모 버퍼 기반 조합
 * - 복합모음 결합: ㅗ+ㅏ=ㅘ, ㅏ+ㅣ=ㅐ 등
 * - DEL: 종성/중성/초성 순서, 복합중성은 원본 방향으로 1단계 축소
 */
class HangulComposer {
    private val buffer = mutableListOf<Char>()

    private val choseong = charArrayOf('ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ')
    private val jungseong = charArrayOf('ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ')
    private val jongseong = charArrayOf('\u0000','ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ','ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ','ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ')

    private val consonants = choseong.toSet()
    private val vowels = jungseong.toSet()

    private val vowelCombinations = mapOf(
        'ㅏ' to mapOf('ㅣ' to 'ㅐ'),
        'ㅑ' to mapOf('ㅣ' to 'ㅒ'),
        'ㅓ' to mapOf('ㅣ' to 'ㅔ'),
        'ㅕ' to mapOf('ㅣ' to 'ㅖ'),
        'ㅗ' to mapOf('ㅏ' to 'ㅘ', 'ㅐ' to 'ㅙ', 'ㅣ' to 'ㅚ'),
        'ㅜ' to mapOf('ㅓ' to 'ㅝ', 'ㅔ' to 'ㅞ', 'ㅣ' to 'ㅟ'),
        'ㅡ' to mapOf('ㅣ' to 'ㅢ')
    )

    /** DEL 시 복합중성 축소 규칙: 과/괘/괴 -> 고, 긔 -> 그 */
    private val vowelDeleteFallback = mapOf(
        'ㅘ' to 'ㅗ', 'ㅙ' to 'ㅗ', 'ㅚ' to 'ㅗ',
        'ㅝ' to 'ㅜ', 'ㅞ' to 'ㅜ', 'ㅟ' to 'ㅜ',
        'ㅢ' to 'ㅡ'
    )

    private val finalCombinations = mapOf(
        "ㄱㅅ" to 'ㄳ', "ㄴㅈ" to 'ㄵ', "ㄴㅎ" to 'ㄶ',
        "ㄹㄱ" to 'ㄺ', "ㄹㅁ" to 'ㄻ', "ㄹㅂ" to 'ㄼ', "ㄹㅅ" to 'ㄽ',
        "ㄹㅌ" to 'ㄾ', "ㄹㅍ" to 'ㄿ', "ㄹㅎ" to 'ㅀ', "ㅂㅅ" to 'ㅄ'
    )

    fun replaceLastJamoOrAppend(ch: Char, replace: Boolean) {
        if (replace && buffer.isNotEmpty()) buffer[buffer.lastIndex] = ch else buffer.add(ch)
        tryCombineLastVowels()
    }

    /**
     * 같은 키 반복 입력 시 직전 자모를 대체할 수 있는지 판단한다.
     *
     * 반츄 규칙:
     * - 초성 단독 상태: 1 + 1 => ㄲ 이므로 대체
     * - 모음 단독/중성 입력 중: 3 + 3 => ㅓ 이므로 대체
     * - 초성+중성+종성 상태에서 같은 자음 재입력: 안 + ㄴ => 안ㄴ 이므로 append
     *   예: 0+3+2+2+33+한+0 => 안녕
     */
    fun canReplaceLastJamoOnRepeat(): Boolean {
        if (buffer.isEmpty()) return false
        val last = buffer.last()
        if (isVowel(last)) return true
        if (!isConsonant(last)) return true

        // C + V + C 상태에서 마지막 C는 종성으로 간주하므로 같은 키 반복이어도 대체하지 않는다.
        if (buffer.size >= 3) {
            val a = buffer[buffer.lastIndex - 2]
            val b = buffer[buffer.lastIndex - 1]
            if (isConsonant(a) && isVowel(b) && isConsonant(last)) {
                return false
            }
        }
        return true
    }

    fun backspace(): Boolean {
        if (buffer.isEmpty()) return false
        val last = buffer.last()
        val fallback = vowelDeleteFallback[last]
        if (fallback != null) {
            buffer[buffer.lastIndex] = fallback
        } else {
            buffer.removeAt(buffer.lastIndex)
        }
        return true
    }

    fun clear() = buffer.clear()
    fun text(): String = render()

    private fun tryCombineLastVowels() {
        if (buffer.size < 2) return
        val a = buffer[buffer.lastIndex - 1]
        val b = buffer[buffer.lastIndex]
        val combined = vowelCombinations[a]?.get(b) ?: return
        buffer.removeAt(buffer.lastIndex)
        buffer[buffer.lastIndex] = combined
    }

    private fun render(): String {
        val out = StringBuilder()
        var i = 0
        while (i < buffer.size) {
            val c = buffer[i]
            if (isConsonant(c) && i + 1 < buffer.size && isVowel(buffer[i + 1])) {
                val initial = c
                val medial = buffer[i + 1]

                // 겹받침 후보: C + V + C + C, 뒤에 모음이 없을 때 우선 결합
                if (i + 3 < buffer.size && isConsonant(buffer[i + 2]) && isConsonant(buffer[i + 3])) {
                    val nextAfterPairIsVowel = i + 4 < buffer.size && isVowel(buffer[i + 4])
                    val pair = "${buffer[i + 2]}${buffer[i + 3]}"
                    val combinedFinal = finalCombinations[pair]
                    if (!nextAfterPairIsVowel && combinedFinal != null) {
                        out.append(compose(initial, medial, combinedFinal) ?: "$initial$medial$combinedFinal")
                        i += 4
                        continue
                    }
                }

                if (i + 2 < buffer.size && isConsonant(buffer[i + 2])) {
                    val finalCandidate = buffer[i + 2]
                    val nextIsVowel = i + 3 < buffer.size && isVowel(buffer[i + 3])
                    if (!nextIsVowel && canBeFinal(finalCandidate)) {
                        out.append(compose(initial, medial, finalCandidate) ?: "$initial$medial$finalCandidate")
                        i += 3
                    } else {
                        out.append(compose(initial, medial, null) ?: "$initial$medial")
                        i += 2
                    }
                } else {
                    out.append(compose(initial, medial, null) ?: "$initial$medial")
                    i += 2
                }
            } else {
                out.append(c)
                i++
            }
        }
        return out.toString()
    }

    private fun isConsonant(ch: Char) = ch in consonants
    private fun isVowel(ch: Char) = ch in vowels
    private fun canBeFinal(ch: Char) = jongseong.indexOf(ch) > 0

    private fun compose(initial: Char, medial: Char, final: Char?): Char? {
        val l = choseong.indexOf(initial)
        val v = jungseong.indexOf(medial)
        val t = if (final == null) 0 else jongseong.indexOf(final)
        if (l < 0 || v < 0 || t < 0) return null
        return (0xAC00 + ((l * 21) + v) * 28 + t).toChar()
    }
}
