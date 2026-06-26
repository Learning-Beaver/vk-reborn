package com.vkreborn.ime.engine

data class VkKey(
    val id: String,
    val label: String,
    val subLabel: String? = null,
    val weight: Float = 1f,
    val action: KeyAction = KeyAction.INPUT
)

enum class KeyAction { INPUT, DELETE, SPACE, ENTER, SHIFT, MODE_HANGUL, MODE_NUMBER, MODE_SYMBOL, MODE_ENGLISH, HIDE }

object KeyboardLayoutModel {
    val hangul = listOf(
        listOf(VkKey("hide", "▦\n▼", action = KeyAction.HIDE), VkKey("1", "ㄱㅋ", "1"), VkKey("2", "ㄴㅁ", "2"), VkKey("3", "ㅏㅓ", "3"), VkKey("del", "DEL", action = KeyAction.DELETE)),
        listOf(VkKey("num", "123", action = KeyAction.MODE_NUMBER), VkKey("4", "ㄷㅌ", "4"), VkKey("5", "ㄹ", "5"), VkKey("6", "ㅗㅜ", "6"), VkKey("space", "Space", action = KeyAction.SPACE)),
        listOf(VkKey("sym", "SYM", action = KeyAction.MODE_SYMBOL), VkKey("7", "ㅂㅍ", "7"), VkKey("8", "ㅅ", "8"), VkKey("9", "ㅣㅡ", "9"), VkKey("enter", "ENTER", action = KeyAction.ENTER)),
        listOf(VkKey("eng", "ABC", action = KeyAction.MODE_ENGLISH), VkKey("*", "ㅈㅊ", "?"), VkKey("0", "ㅇㅎ", "0"), VkKey("han", "한", "~", action = KeyAction.SHIFT), VkKey(".", "."))
    )

    val number = listOf(
        listOf(VkKey("hide", "▦\n▼", action = KeyAction.HIDE), VkKey("1", "1"), VkKey("2", "2"), VkKey("3", "3"), VkKey("del", "DEL", action = KeyAction.DELETE)),
        listOf(VkKey("sym", "SYM", action = KeyAction.MODE_SYMBOL), VkKey("4", "4"), VkKey("5", "5"), VkKey("6", "6"), VkKey("space", "Space", action = KeyAction.SPACE)),
        listOf(VkKey("eng", "ABC", action = KeyAction.MODE_ENGLISH), VkKey("7", "7"), VkKey("8", "8"), VkKey("9", "9"), VkKey("enter", "ENTER", action = KeyAction.ENTER)),
        listOf(VkKey("han", "한글", action = KeyAction.MODE_HANGUL), VkKey(",", ","), VkKey("0", "0"), VkKey("-", "-"), VkKey(".", "."))
    )

    val english = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0").map { VkKey(it, it) },
        "qwertyuiop".map { VkKey(it.toString(), it.toString()) },
        "asdfghjkl".map { VkKey(it.toString(), it.toString()) },
        listOf(VkKey("han", "한글", action = KeyAction.MODE_HANGUL)) + "zxcvbnm".map { VkKey(it.toString(), it.toString()) } + VkKey("del", "DEL", action = KeyAction.DELETE)
    )

    val symbol = listOf(
        listOf("@", "#", "$", "%", "&", "*", "(", ")" ).map { VkKey(it, it) },
        listOf("!", "\"", "'", ":", ";", "/", "?" ).map { VkKey(it, it) },
        listOf(VkKey("num", "123", action = KeyAction.MODE_NUMBER), VkKey("han", "한글", action = KeyAction.MODE_HANGUL), VkKey("del", "DEL", action = KeyAction.DELETE))
    )

    fun forMode(mode: KeyboardMode): List<List<VkKey>> = when (mode) {
        KeyboardMode.HANGUL -> hangul
        KeyboardMode.NUMBER -> number
        KeyboardMode.SYMBOL -> symbol
        KeyboardMode.ENGLISH -> english
    }
}
