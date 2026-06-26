# Changelog

## v0.5
- Corrected communication mismatch: `#` is not a visible key. The visible `한` key is the conversion/toggle key.
- Hangul layout now follows original Vanchu screen:
  - Row 3 left: `SYM`
  - Row 4 right-center: `한`
- Implemented `한` toggle behavior:
  - `1 -> ㄱ`
  - `1 + 한 -> ㅋ`
  - `1 + 한 + 한 -> ㄱ`
  - `3 -> ㅏ`, `3 + 한 -> ㅑ`, `3 + 한 + 한 -> ㅏ`
- Preserved complex vowel examples:
  - `1 + 3 + 9 -> 개`
  - `1 + 3 + 3 + 9 -> 게`

## v0.4.2
- Java/Kotlin target aligned to 1.8 for broad local build compatibility.


## v0.6
- 복합모음/삭제/DEL 롱프레스 반복 삭제 반영
- 9번 키 3회 입력 `ㅢ` 지원
- 겹받침 1차 조합 지원
