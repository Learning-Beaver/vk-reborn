package com.vkreborn.ime.engine

data class VkKey(
    val id: String,
    val label: String,
    val subLabel: String? = null,
    val weight: Float = 1f,
    val action: KeyAction = KeyAction.INPUT
)

enum class KeyAction { INPUT, DELETE, SPACE, ENTER, SHIFT, MODE_HANGUL, MODE_NUMBER, MODE_SYMBOL, MODE_SYMBOL2, MODE_ENGLISH, HIDE }

enum class EnglishShiftState { OFF, ONCE, LOCK }

object KeyboardLayoutModel {
    val hangul = listOf(
        listOf(VkKey("hide", "▦\n▼", action = KeyAction.HIDE), VkKey("1", "ㄱㅋ", "1"), VkKey("2", "ㄴㅁ", "2"), VkKey("3", "ㅏㅓ", "3"), VkKey("del", "DEL", action = KeyAction.DELETE)),
        listOf(VkKey("num", "123", action = KeyAction.MODE_NUMBER), VkKey("4", "ㄷㅌ", "4"), VkKey("5", "ㄹ", "5"), VkKey("6", "ㅗㅜ", "6"), VkKey("space", "▔▔", action = KeyAction.SPACE)),
        listOf(VkKey("sym", "SYM", action = KeyAction.MODE_SYMBOL), VkKey("7", "ㅂㅍ", "7"), VkKey("8", "ㅅ", "8"), VkKey("9", "ㅣㅡ", "9"), VkKey("enter", "ENTER", action = KeyAction.ENTER)),
        listOf(VkKey("eng", "ABC", action = KeyAction.MODE_ENGLISH), VkKey("*", "ㅈㅊ", "?"), VkKey("0", "ㅇㅎ", "0"), VkKey("han", "한", "~", action = KeyAction.SHIFT), VkKey(".", ".", ","))
    )

    val number = listOf(
        listOf(VkKey("hide", "▦\n▼", action = KeyAction.HIDE), VkKey("1", "1", "!"), VkKey("2", "2", "@"), VkKey("3", "3", "#"), VkKey("del", "DEL", action = KeyAction.DELETE)),
        listOf(VkKey("han", "한글", action = KeyAction.MODE_HANGUL), VkKey("4", "4", "$"), VkKey("5", "5", "%"), VkKey("6", "6", "^"), VkKey("space", "▔▔", action = KeyAction.SPACE)),
        listOf(VkKey("sym", "SYM", action = KeyAction.MODE_SYMBOL), VkKey("7", "7", "-"), VkKey("8", "8", "|"), VkKey("9", "9", "("), VkKey("enter", "ENTER", action = KeyAction.ENTER)),
        listOf(VkKey("eng", "ABC", action = KeyAction.MODE_ENGLISH), VkKey(",", ",", ";"), VkKey("0", "0", ")"), VkKey("-", "-", "="), VkKey(".", ".", "?"))
    )

    val english = listOf(
        listOf(
            VkKey("q", "q", "1"), VkKey("w", "w", "2"), VkKey("e", "e", "3"), VkKey("r", "r", "4"), VkKey("t", "t", "5"),
            VkKey("y", "y", "6"), VkKey("u", "u", "7"), VkKey("i", "i", "8"), VkKey("o", "o", "9"), VkKey("p", "p", "0")
        ),
        listOf(
            VkKey("a", "a", "~"), VkKey("s", "s", "!"), VkKey("d", "d", "@"), VkKey("f", "f", "#"), VkKey("g", "g", "^"),
            VkKey("h", "h", "&"), VkKey("j", "j", "("), VkKey("k", "k", ")"), VkKey("l", "l", "-")
        ),
        listOf(
            VkKey("shift", "⇧", action = KeyAction.SHIFT, weight = 1.55f),
            VkKey("z", "z", "."), VkKey("x", "x", ";"), VkKey("c", "c", "/"), VkKey("v", "v", "\""),
            VkKey("b", "b", "'"), VkKey("n", "n", "?"), VkKey("m", "m", "+"),
            VkKey("del", "DEL", action = KeyAction.DELETE, weight = 1.55f)
        ),
        listOf(
            VkKey("han", "한글", action = KeyAction.MODE_HANGUL, weight = 1.35f),
            VkKey("sym", "SYM", action = KeyAction.MODE_SYMBOL, weight = 1.35f),
            VkKey("space", "▔▔", action = KeyAction.SPACE, weight = 4.0f),
            VkKey(".", ".", "♥", weight = 0.85f),
            VkKey("enter", "ENTER", action = KeyAction.ENTER, weight = 1.85f)
        )
    )

    val symbol = listOf(
        listOf(
            VkKey("1", "1", "⅛"), VkKey("2", "2", "⅔"), VkKey("3", "3", "⅜"), VkKey("4", "4"), VkKey("5", "5", "⅝"),
            VkKey("6", "6"), VkKey("7", "7", "⅞"), VkKey("8", "8"), VkKey("9", "9"), VkKey("0", "0", "Ø")
        ),
        listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")").map { VkKey(it, it) },
        listOf(
            VkKey("alt", "ALT", action = KeyAction.MODE_SYMBOL2, weight = 1.5f),
            VkKey("!", "!", "¡"), VkKey("\"", "\"", "“"), VkKey("'", "'"), VkKey(":", ":"), VkKey(";", ";"),
            VkKey("/", "/"), VkKey("?", "?", "¿"), VkKey("del", "DEL", action = KeyAction.DELETE, weight = 1.5f)
        ),
        listOf(
            VkKey("eng", "ABC", action = KeyAction.MODE_ENGLISH, weight = 1.4f),
            VkKey("han", "한글", action = KeyAction.MODE_HANGUL, weight = 1.4f),
            VkKey(",", ",", "”"), VkKey("space", "▔▔", action = KeyAction.SPACE, weight = 4.0f),
            VkKey(".", ".", "…"), VkKey("enter", "ENTER", action = KeyAction.ENTER, weight = 1.6f)
        )
    )

    val symbol2 = listOf(
        listOf(
            VkKey("~", "~"), VkKey("`", "`"), VkKey("|", "|"), VkKey("•", "•"), VkKey("√", "√"),
            VkKey("Π", "Π"), VkKey("÷", "÷"), VkKey("×", "×"), VkKey("{", "{"), VkKey("}", "}")
        ),
        listOf(
            VkKey("€", "€", "¢"), VkKey("♡", "♡"), VkKey("♥", "♥"), VkKey("☆", "☆"), VkKey("★", "★"),
            VkKey("^", "^"), VkKey("_", "_"), VkKey("=", "=", "∞"), VkKey("[", "["), VkKey("]", "]")
        ),
        listOf(
            VkKey("alt", "ALT", action = KeyAction.MODE_SYMBOL, weight = 1.5f),
            VkKey("™", "™"), VkKey("®", "®"), VkKey("©", "©"), VkKey("※", "※"), VkKey("\\", "\\"),
            VkKey("<", "<", "‹"), VkKey(">", ">", "›"), VkKey("del", "DEL", action = KeyAction.DELETE, weight = 1.5f)
        ),
        listOf(
            VkKey("han", "한글", action = KeyAction.MODE_HANGUL, weight = 1.4f),
            VkKey("eng", "ABC", action = KeyAction.MODE_ENGLISH, weight = 1.4f),
            VkKey("”", "”"), VkKey("space", "▔▔", action = KeyAction.SPACE, weight = 4.0f),
            VkKey("…", "…"), VkKey("enter", "ENTER", action = KeyAction.ENTER, weight = 1.6f)
        )
    )

    fun forMode(mode: KeyboardMode): List<List<VkKey>> = when (mode) {
        KeyboardMode.HANGUL -> hangul
        KeyboardMode.NUMBER -> number
        KeyboardMode.SYMBOL -> symbol
        KeyboardMode.SYMBOL2 -> symbol2
        KeyboardMode.ENGLISH -> english
    }
}
