# Samsung Health Example

Samsung Health SDK를 사용한 테스트 앱입니다.

## 기능

- ✅ Samsung Health 권한 요청 및 확인
- ✅ 걸음수 데이터 조회 (오늘, 이번 주, 날짜별)
- ✅ 운동 데이터 조회 (오늘, 날짜 범위)
- ✅ 운동 칼로리, 심박수 데이터 포함

## 요구사항

- Android Studio 최신 버전
- Android SDK 29 이상 (Samsung Health SDK 최소 요구사항)
- Java 17
- Samsung Health SDK AAR 파일

## 설정 방법

### 1. Samsung Health SDK AAR 파일 추가

다음 위치에 Samsung Health SDK AAR 파일을 추가하세요:

```
app/libs/samsung-health-data-api-1.0.0.aar
```

**AAR 파일 구하기:**
- [Samsung Health SDK 공식 문서](https://developer.samsung.com/health)에서 다운로드

### 2. 빌드 및 실행

- 빌드가 성공하면 Samsung 기기에서 실행
- Samsung Health 앱이 설치되어 있어야 합니다

## 프로젝트 구조

```
app/src/main/java/com/easyhooon/samsunghealthexample/
├── MainActivity.kt
├── SamsungHealthApplication.kt
├── health/
│   ├── SamsungHealthManager.kt
│   └── di/
│       └── SamsungHealthModule.kt
├── model/
│   ├── ExerciseData.kt
│   ├── HealthError.kt
│   └── DailyStepData.kt
└── ui/
    ├── health/
    │   ├── SamsungHealthScreen.kt
    │   └── SamsungHealthViewModel.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## 기술 스택

- Kotlin
- Jetpack Compose
- Hilt (의존성 주입)
- Timber (로깅)
- Samsung Health SDK

## 라이선스

이 프로젝트는 예제 목적으로만 제공됩니다.
Samsung Health SDK는 별도의 라이선스를 따릅니다.
