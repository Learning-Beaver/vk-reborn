# VK Reborn v0.4 - Build / Install

## Android Studio

1. Android Studio에서 프로젝트 루트 `vk-reborn-v0.4` 열기
2. Gradle Sync 실행
3. `app` 모듈 빌드
4. 테스트 APK 생성
   - `Build > Build Bundle(s) / APK(s) > Build APK(s)`

## ADB 설치

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 갤럭시에서 활성화

1. 설정 > 일반 > 키보드 목록 및 기본값
2. `VK Reborn` 활성화
3. 기본 키보드에서 `VK Reborn` 선택

## v0.4 테스트 포인트

- 앱 아이콘 실행 시 입력기 설정 버튼 표시
- 키보드 목록에 VK Reborn 표시
- 한글 모드 키 표시
- `1 3` 입력 시 `가`
- `1 # 3` 입력 시 `카`
- `3 3 #` 입력 시 `ㅕ`
- 한글 키 롱프레스 시 숫자 입력
