# VK Reborn v0.9.10

## Accelerated Delete Patch

### Fix / Improvement
- DEL 키 롱프레스 시 삭제 속도가 단계적으로 빨라지도록 개선
- 짧게 누르면 1글자 삭제
- 길게 누르면 반복 삭제
- 오래 누를수록 매우 빠른 삭제

### Regression 유지
- `1+3 → 가`
- 커서 중간 이동 후 DEL → 현재 커서 앞 문자 삭제
- 숫자 키보드 롱프레스 특수문자 유지

### Test
```text
긴 문장 입력 → DEL 길게 → 점점 빠르게 삭제
문장 중간 커서 이동 → DEL 길게 → 커서 기준 앞쪽으로 삭제
```
