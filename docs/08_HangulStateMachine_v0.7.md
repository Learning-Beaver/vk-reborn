# Hangul State Machine v0.7

## Fix Target

### 안녕

Input:

```text
0+3+2+2+33+한+0
```

Expected:

```text
안녕
```

Interpretation:

```text
0+3 = 아
2 = 받침 ㄴ => 안
2 = 다음 음절 초성 ㄴ
33+한 = ㅕ
0 = 받침 ㅇ
```

### 응이

Input:

```text
0+9+9+0+0+9
```

Expected:

```text
응이
```

Interpretation:

```text
0+99 = 으
0 = 받침 ㅇ => 응
0 = 다음 음절 초성 ㅇ
9 = ㅣ => 이
```

## Core Rule

If current composing buffer ends with `초성 + 중성 + 종성`, and the same consonant key is pressed again, do not replace the final consonant. Append it as the next syllable initial.
