# Vanchu Input Rule Spec v0.5

## Key naming

`#` is no longer used in documentation. The original visible key is `한`.

```text
1 = ㄱㅋ
2 = ㄴㅁ
3 = ㅏㅓ
4 = ㄷㅌ
5 = ㄹ
6 = ㅗㅜ
7 = ㅂㅍ
8 = ㅅ
9 = ㅣㅡ
* = ㅈㅊ
0 = ㅇㅎ
한 = conversion/toggle key
```

## Confirmed rules

| Input | Output |
|---|---|
| 1 | ㄱ |
| 11 | ㄲ |
| 1+한 | ㅋ |
| 1+한+한 | ㄱ |
| 3 | ㅏ |
| 33 | ㅓ |
| 3+한 | ㅑ |
| 33+한 | ㅕ |
| 1+3+9 | 개 |
| 1+33+9 | 게 |

## Delete behavior

- If a syllable is already committed, Delete removes it as one character.
- If a syllable is still composing, Delete removes the last internal jamo first.
  - composing `가` -> DEL -> `ㄱ` -> DEL -> empty
