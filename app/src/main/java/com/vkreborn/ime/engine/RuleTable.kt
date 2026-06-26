package com.vkreborn.ime.engine

data class KeyRule(
    val keyId: String,
    val label: String,
    val normal: String,
    val repeat: String? = null,
    val third: String? = null,
    val shiftNormal: String? = null,
    val shiftRepeat: String? = null,
    val longPress: String? = null
)

object RuleTable {
    val hangulRules: Map<String, KeyRule> = listOf(
        KeyRule("1", "ㄱㅋ", normal = "ㄱ", repeat = "ㄲ", shiftNormal = "ㅋ", longPress = "1"),
        KeyRule("2", "ㄴㅁ", normal = "ㄴ", shiftNormal = "ㅁ", longPress = "2"),
        KeyRule("3", "ㅏㅓ", normal = "ㅏ", repeat = "ㅓ", shiftNormal = "ㅑ", shiftRepeat = "ㅕ", longPress = "3"),
        KeyRule("4", "ㄷㅌ", normal = "ㄷ", repeat = "ㄸ", shiftNormal = "ㅌ", longPress = "4"),
        KeyRule("5", "ㄹ", normal = "ㄹ", longPress = "5"),
        KeyRule("6", "ㅗㅜ", normal = "ㅗ", repeat = "ㅜ", shiftNormal = "ㅛ", shiftRepeat = "ㅠ", longPress = "6"),
        KeyRule("7", "ㅂㅍ", normal = "ㅂ", repeat = "ㅃ", shiftNormal = "ㅍ", longPress = "7"),
        KeyRule("8", "ㅅ", normal = "ㅅ", repeat = "ㅆ", longPress = "8"),
        KeyRule("9", "ㅣㅡ", normal = "ㅣ", repeat = "ㅡ", third = "ㅢ", longPress = "9"),
        KeyRule("*", "ㅈㅊ", normal = "ㅈ", repeat = "ㅉ", shiftNormal = "ㅊ", longPress = "?"),
        KeyRule("0", "ㅇㅎ", normal = "ㅇ", shiftNormal = "ㅎ", longPress = "0"),
    ).associateBy { it.keyId }
}
