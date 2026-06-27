# VK Reborn v0.9

반츄 키보드 복원 프로젝트 v0.7.3입니다.

## 반영 내용
- `한` 키 토글 유지
- `9 -> ㅣ`, `99 -> ㅡ`, `999 -> ㅢ` 반영
- 복합모음 조합 추가: `ㅏ+ㅣ=ㅐ`, `ㅓ+ㅣ=ㅔ`, `ㅗ+ㅏ=ㅘ`, `ㅗ+ㅣ=ㅚ`, `ㅜ+ㅓ=ㅝ`, `ㅡ+ㅣ=ㅢ` 등
- 삭제 규칙 개선
  - `과/괘/괴 + DEL -> 고`
  - `긔 + DEL -> 그`
  - `깨 + DEL -> ㄲ`
- DEL 길게 누름 반복 삭제 구현
- 겹받침 1차 지원: 값/읽/닭/앉/않/없 등 기본 조합

## 우선 테스트
- `1+3+9 = 개`
- `1+3+3+9 = 게`
- `1+1+3+9 = 깨`, `DEL -> ㄲ`, `DEL -> 없음`
- `1+6+3 = 과`, `DEL -> 고`
- `1+6+3+9 = 괘`, `DEL -> 고`
- `1+6+9 = 괴`, `DEL -> 고`
- `1+9+9+9 = 긔`, `DEL -> 그`
- DEL 길게 누르면 연속 삭제


## v0.7.3 Notes

- 종성 뒤 같은 키 반복 입력 처리 수정
  - `0+3+2+2+33+한+0` => `안녕`
  - `0+9+9+0+0+9` => `응이`
- 같은 키 반복이 항상 직전 자모 대체로 처리되던 문제 수정
- `HangulComposer.canReplaceLastJamoOnRepeat()` 추가



## v0.7.3
- Fix: `겠` input rule.
- `1+33+9+8+8` now composes `겠` instead of splitting the repeated `ㅅ`.
- Keeps `안녕` rule: `0+3+2+2+33+한+0` remains `안녕`.


## v0.7.3

Fix repeated final consonant overflow.

- `0+3+8+8+8` now outputs `았ㅅ` instead of `았ㅆ`.
- `1+33+9+8+8` still outputs `겠`.
- `1+33+9+8+8+8` outputs `겠ㅅ`.


## v0.8

English keyboard restoration.

- Original-style QWERTY layout
- Shift OFF / ONCE / LOCK state machine
- Shift 3rd press returns to lowercase
- Caps Lock blue dot indicator
- English long-press sub labels
- Common DEL / SPACE / ENTER behavior retained


## v0.9

Symbol keyboard restoration.

- Added SYMBOL2 mode.
- ALT toggles between SYM Page 1 and SYM Page 2.
- Restored original second symbol page layout.
- Common DEL / SPACE / ENTER behavior retained.


## v0.9.2
- Performance patch
- Cached key rectangles
- Partial key invalidation
- Batch edit for composing text
- Safer Handler cleanup


## v0.9.2 Turbo Patch
- ACTION_DOWN 즉시 입력 처리
- 롱프레스와 일반 입력 분리
- 롱프레스 시 직전 일반 입력을 보조문자로 치환
- 롱프레스 임계값 280ms로 단축
- 빠른 타자 입력 누락 완화
