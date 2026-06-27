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


## v0.7

- Fix: same-key consonant after final consonant should start next syllable.
- Test: `안녕` input path fixed.
- Test: `응이` input path fixed.
- Add: `canReplaceLastJamoOnRepeat()` composer guard.


## v0.7.3
- ENTER key now respects EditorInfo IME actions.
- Chrome/search fields execute SEARCH instead of inserting whitespace/newline.
- Text editors still receive KEYCODE_ENTER fallback for newline.
