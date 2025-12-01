# Spring Boot 초기 세팅 프로젝트

이 프로젝트는 Spring Boot 3.2 기반의 초기 세팅 템플릿입니다.

## 프로젝트 구조

```
demo-project/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/devpath/
│       │       ├── DemoApplication.java
│       │       ├── global/              # 공통 설정 및 유틸리티
│       │       │   ├── apiPayload/      # API 응답 형식 관리
│       │       │   ├── config/          # 설정 클래스 (S3 등)
│       │       │   ├── dto/             # 공통 DTO
│       │       │   ├── log/             # 로깅 AOP
│       │       │   └── s3/              # S3 관련 기능
│       │       └── domain/              # 도메인별 패키지
│       │           └── user/
│       │               ├── controller/
│       │               ├── service/
│       │               ├── repository/
│       │               ├── entity/
│       │               └── dto/
│       └── resources/
│           └── application.yml
├── build.gradle
└── settings.gradle
```

## 주요 기능

### 1. Global 패키지 구성

#### ApiPayload
- **ApiResponse**: 통일된 API 응답 형식 제공
- **BaseErrorCode / BaseSuccessCode**: 에러 및 성공 코드 인터페이스
- **GeneralException**: 커스텀 예외 처리
- **ExceptionAdvice**: 전역 예외 핸들러

#### Config
- **S3Config**: AWS S3 설정

#### Log
- **LoggingAspect**: AOP 기반 로깅 (요청/응답/실행시간/예외)

#### DTO
- **PageResponseDto**: 페이징 응답 DTO
- **CursorResponseDto**: 커서 기반 페이징 응답 DTO

#### S3
- **S3Service**: S3 파일 업로드/다운로드 서비스
- **S3Controller**: S3 관련 API 엔드포인트

### 2. API 응답 형식

모든 API는 다음과 같은 통일된 형식으로 응답합니다:

**성공 응답 예시:**
```json
{
  "isSuccess": true,
  "code": "2000",
  "message": "요청에 성공하였습니다.",
  "result": {
    "id": 1,
    "name": "홍길동",
    "email": "test@example.com"
  },
  "timestamp": "2024-12-02T00:30:00"
}
```

**실패 응답 예시:**
```json
{
  "isSuccess": false,
  "code": "4000",
  "message": "잘못된 요청입니다.",
  "result": null
}
```

### 3. 로깅 기능

LoggingAspect가 자동으로 다음을 로깅합니다:
- 메서드 요청 정보 (파라미터)
- 메서드 응답 정보 (반환값)
- 메서드 실행 시간
- 예외 발생 정보

### 4. 예외 처리

GeneralException을 통해 비즈니스 로직 예외를 처리하고,
ExceptionAdvice가 전역적으로 예외를 잡아 통일된 응답을 반환합니다.


## 기술 스택

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring AOP
- AWS SDK for Java v2
- Lombok
- H2 Database (개발용)

## 라이선스

MIT License
