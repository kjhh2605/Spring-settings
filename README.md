# Spring Boot OAuth2 + JWT 초기 세팅 프로젝트

Spring Boot 3.2 기반의 엔터프라이즈급 초기 세팅 템플릿입니다.
OAuth2 소셜 로그인, JWT 인증, AWS S3 통합, 전역 예외 처리 등 실무에 필요한 핵심 기능을 포함합니다.

## 목차
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [설정 방법](#설정-방법)
- [사용 방법](#사용-방법)

---

## 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** + **OAuth2 Client**
- **Spring Data JPA** + **MySQL**
- **Spring Data Redis**
- **Spring AOP**

### 인증/인가
- **JWT (JJWT 0.12.3)** - Access Token (3분) + Refresh Token (14일)
- **OAuth2** - Google, Kakao, Naver 소셜 로그인
- **Redis** - Refresh Token 저장 및 블랙리스트 관리

### 인프라
- **AWS S3 SDK 2.21.1** - Presigned URL 기반 파일 업로드
- **Swagger/OpenAPI 3** - API 문서 자동화
- **Lombok** - 코드 간소화

---

## 프로젝트 구조

```
src/main/java/com/myApp/
├── MyApplication.java                     # 메인 애플리케이션
│
├── auth/                                  # 🔐 인증/인가 모듈
│   ├── annotation/
│   │   ├── AuthUser.java                  # 현재 로그인 사용자 주입
│   │   └── CheckBlacklist.java            # 블랙리스트 검증 AOP
│   ├── aspect/
│   │   └── BlacklistAspect.java           # 로그아웃된 토큰 검증
│   ├── controller/
│   │   └── AuthController.java            # 인증 API (재발급, 로그아웃)
│   ├── dto/
│   │   └── TokenDto.java                  # JWT 토큰 DTO
│   ├── entity/
│   │   ├── Member.java                    # 사용자 엔티티
│   │   └── Role.java                      # 권한 (USER, ADMIN)
│   ├── handler/
│   │   ├── AuthUserArgumentResolver.java # @AuthUser 리졸버
│   │   └── OAuth2SuccessHandler.java      # OAuth2 로그인 성공 핸들러
│   ├── jwt/
│   │   ├── JwtTokenProvider.java          # JWT 생성/검증
│   │   └── JwtAuthenticationFilter.java   # JWT 인증 필터
│   ├── redis/
│   │   └── RefreshToken.java              # Redis 리프레시 토큰
│   ├── repository/
│   │   ├── MemberRepository.java          # Member JPA Repository
│   │   └── RefreshTokenRepository.java    # RefreshToken Redis Repository
│   └── service/
│       ├── AuthService.java               # 인증 비즈니스 로직
│       ├── CustomOAuth2UserService.java   # OAuth2 사용자 서비스
│       ├── CustomUserDetailsService.java  # UserDetails 서비스
│       └── OAuthAttributes.java           # OAuth2 속성 매핑
│
├── domain/                                # 📦 도메인 모듈
│   └── users/
│       ├── controller/
│       │   └── UserController.java        # 사용자 API
│       ├── dto/
│       │   └── UserResponseDto.java       # 사용자 응답 DTO
│       └── service/
│           └── UserService.java           # 사용자 비즈니스 로직
│
└── global/                                # 🌐 전역 설정
    ├── apiPayload/
    │   ├── ApiResponse.java               # 통일된 API 응답 형식
    │   ├── code/status/
    │   │   ├── BaseCode.java              # 상태 코드 인터페이스
    │   │   ├── AuthErrorCode.java         # 인증 에러 코드
    │   │   ├── GeneralErrorCode.java      # 일반 에러 코드
    │   │   └── GeneralSuccessCode.java    # 성공 코드
    │   └── exception/
    │       ├── ExceptionAdvice.java       # 전역 예외 핸들러
    │       └── GeneralException.java      # 커스텀 예외
    ├── common/
    │   └── BaseEntity.java                # 공통 엔티티 (createdAt, updatedAt)
    ├── config/
    │   ├── SecurityConfig.java            # Spring Security 설정
    │   ├── SwaggerConfig.java             # Swagger 설정
    │   ├── S3Config.java                  # AWS S3 설정
    │   └── WebConfig.java                 # Web MVC 설정
    ├── dto/
    │   ├── PageResponseDto.java           # 페이지 페이징 응답
    │   └── CursorResponseDto.java         # 커서 페이징 응답
    ├── log/
    │   └── LoggingAspect.java             # AOP 로깅
    └── s3/
        ├── controller/
        │   └── S3Controller.java          # S3 API
        ├── dto/
        │   └── S3Dto.java                 # S3 DTO
        └── service/
            └── S3Service.java             # S3 비즈니스 로직
```

---

## 주요 기능

### 1. 🔐 인증/인가 시스템

#### OAuth2 소셜 로그인
- **지원 플랫폼**: Google, Kakao, Naver
- **자동 회원가입**: 소셜 로그인 시 이메일 기반으로 자동 가입
- **사용자 정보 동기화**: 이름 등 정보 자동 업데이트

#### JWT 토큰 기반 인증
```
Access Token (3분 유효)
- Header의 Authorization: Bearer {token} 형식
- 짧은 만료 시간으로 보안 강화

Refresh Token (14일 유효)
- HttpOnly 쿠키로 전송 (XSS 공격 방어)
- Redis에 저장 (빠른 조회 및 자동 만료)
- RTR (Refresh Token Rotation) 방식 지원
```

#### 로그아웃 및 블랙리스트
- Redis 기반 블랙리스트 관리
- Access Token 남은 시간만큼 블랙리스트 저장
- `@CheckBlacklist` AOP로 자동 검증

#### 커스텀 어노테이션
```java
// 현재 로그인 사용자 정보 주입
@GetMapping("/me")
public ApiResponse<UserResponseDto> getMyInfo(@AuthUser UserDetails userDetails) {
    // userDetails.getUsername()으로 이메일 획득
}
```

### 2. 📡 통일된 API 응답 형식

모든 API는 `ApiResponse`로 일관된 형식을 반환합니다.

**성공 응답:**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "id": 1,
    "name": "홍길동",
    "email": "user@example.com"
  },
  "timestamp": "2025-12-20T15:30:45"
}
```

**실패 응답:**
```json
{
  "isSuccess": false,
  "code": "AUTH4001",
  "message": "인증되지 않은 사용자입니다.",
  "result": null,
  "timestamp": "2025-12-20T15:30:45"
}
```

**사용 방법:**
```java
// 성공 응답
return ApiResponse.onSuccess(GeneralSuccessCode._OK, result);

// 실패 응답 (컨트롤러)
throw new GeneralException(AuthErrorCode.AUTH_UNAUTHORIZED);

// 실패 응답 (필터/Security)
ApiResponse.Body<?> errorBody = ApiResponse.createFailureBody(AuthErrorCode.AUTH_UNAUTHORIZED);
```

### 3. 🚨 전역 예외 처리

`ExceptionAdvice`가 모든 예외를 일관된 형식으로 처리합니다.

**처리하는 예외:**
- `ConstraintViolationException`: @Valid 검증 실패 (RequestParam, PathVariable)
- `MethodArgumentNotValidException`: @Valid 검증 실패 (RequestBody)
- `HttpMessageNotReadableException`: JSON 파싱 실패
- `GeneralException`: 비즈니스 로직 커스텀 예외
- `Exception`: 기타 모든 예외 (500 Internal Server Error)

**Security 레벨 예외:**
- 인증 실패 시 `AuthenticationEntryPoint`에서 401 JSON 응답 반환

### 4. 📝 AOP 로깅

`LoggingAspect`가 자동으로 요청/응답/실행시간/예외를 로깅합니다.

**대상:** `com.myApp.domain..controller..*` 및 `com.myApp.domain..service..*`

**로그 예시:**
```
▶️요청 - UserController.getMyInfo(..) | args = [org.springframework.security.core.userdetails.User@...]
✅응답 - UserController.getMyInfo(..) | result = UserResponseDto(id=1, name=홍길동, ...)
⏱️실행 시간 - UserController.getMyInfo(..) | 42 ms
❌예외 - UserController.getMyInfo(..) | message = 사용자를 찾을 수 없습니다.
```

### 5. ☁️ AWS S3 통합

Presigned URL 방식으로 클라이언트가 직접 파일을 업로드합니다.

**장점:**
- 서버 부하 감소
- 빠른 업로드 속도
- UUID로 파일명 중복 방지

**사용 예시:**
```
1. GET /api/v1/s3/presigned-url?prefix=profile&fileName=image.jpg
   → { "url": "https://...", "key": "profile/uuid-image.jpg" }

2. 클라이언트가 반환된 URL로 직접 PUT 요청
   → S3에 파일 업로드 완료

3. key를 DB에 저장하여 추후 조회
```

### 6. 🗄️ JPA Auditing

`BaseEntity`를 상속하면 생성/수정 시간이 자동으로 관리됩니다.

```java
@Entity
public class Member extends BaseEntity {
    // createdAt, updatedAt 자동 관리
}
```

### 7. 📚 Swagger/OpenAPI

API 문서가 자동으로 생성됩니다.

- **접속:** http://localhost:8080/swagger-ui/index.html
- **Controller별 Docs 인터페이스**: `@Tag`, `@Operation` 어노테이션으로 문서화

---

## API 엔드포인트

### 인증 API (`/api/v1/auth`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/v1/auth/reissue` | Access Token 재발급 (RTR) | Cookie |
| POST | `/api/v1/auth/logout` | 로그아웃 (토큰 무효화) | ✅ |

### 사용자 API (`/api/v1/users`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/v1/users/me` | 현재 로그인 사용자 정보 조회 | ✅ |
| GET | `/api/v1/users/{userId}` | 특정 사용자 정보 조회 | ✅ |

### S3 API (`/api/v1/s3`)

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/v1/s3/presigned-url` | S3 업로드용 Presigned URL 생성 | ❌ |

**쿼리 파라미터:**
- `prefix`: S3 폴더 경로 (예: `profile`, `posts`)
- `fileName`: 파일명 (예: `image.jpg`)

---

## 설정 방법

### 1. 환경 변수 설정

다음 환경 변수를 설정해야 합니다:

#### 데이터베이스
```bash
DB_URL=jdbc:mysql://localhost:3306/myapp
DB_USERNAME=root
DB_PASSWORD=password
```

#### JWT
```bash
JWT_SECRET=your-secret-key-min-32-characters-long
JWT_ACCESS_EXPIRATION=180000          # 3분 (밀리초)
JWT_REFRESH_EXPIRATION=1209600000     # 14일 (밀리초)
```

#### Redis
```bash
REDIS_HOST=localhost
REDIS_PORT=6379
```

#### OAuth2 (Google)
```bash
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
```

#### OAuth2 (Kakao)
```bash
KAKAO_CLIENT_ID=your-client-id
KAKAO_CLIENT_SECRET=your-client-secret
KAKAO_REDIRECT_URI=http://localhost:8080/login/oauth2/code/kakao
```

#### OAuth2 (Naver)
```bash
NAVER_CLIENT_ID=your-client-id
NAVER_CLIENT_SECRET=your-client-secret
NAVER_REDIRECT_URI=http://localhost:8080/login/oauth2/code/naver
```

#### AWS S3 (선택사항)
```bash
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_REGION=ap-northeast-2
AWS_S3_BUCKET=your-bucket-name

# S3 기능 활성화 여부 (dev 환경에서는 false)
SPRING_CLOUD_AWS_S3_ENABLED=true
```

### 2. 프로파일 선택

**개발 환경:**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**운영 환경:**
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### 3. 데이터베이스 초기화

프로젝트 실행 시 JPA가 자동으로 테이블을 생성합니다 (dev: `ddl-auto=update`)

---

## 사용 방법

### 1. OAuth2 소셜 로그인

```
1. 브라우저에서 접속:
   http://localhost:8080/oauth2/authorization/google
   http://localhost:8080/oauth2/authorization/kakao
   http://localhost:8080/oauth2/authorization/naver

2. 소셜 로그인 완료 후 리다이렉트:
   http://localhost:8080/?access_token={token}

3. 쿠키에 refresh_token 자동 저장 (HttpOnly)
```

### 2. API 호출 (Access Token 사용)

```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer {access_token}"
```

### 3. Access Token 재발급

```bash
curl -X POST http://localhost:8080/api/v1/auth/reissue \
  --cookie "refresh_token={refresh_token}"
```

**응답:**
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "accessToken": "new-access-token",
    "refreshToken": "new-refresh-token"
  }
}
```

### 4. 로그아웃

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer {access_token}" \
  --cookie "refresh_token={refresh_token}"
```

**동작:**
1. Access Token을 블랙리스트에 추가 (Redis)
2. Refresh Token을 Redis에서 삭제
3. 쿠키에서 refresh_token 삭제

---

## 에러 코드

### 인증 관련 (AUTH)

| 코드 | 메시지 | HTTP Status |
|------|--------|-------------|
| AUTH4001 | 인증되지 않은 사용자입니다. | 401 |
| AUTH4002 | 액세스 토큰이 만료되었습니다. | 401 |
| AUTH4003 | 유효하지 않은 액세스 토큰입니다. | 401 |
| AUTH4004 | 인증 토큰이 존재하지 않습니다. | 401 |
| AUTH4005 | 접근 권한이 없습니다. | 403 |
| AUTH4006 | 유효하지 않은 리프레시 토큰입니다. | 401 |
| AUTH4007 | 리프레시 토큰이 존재하지 않습니다. | 401 |
| AUTH4008 | 리프레시 토큰이 만료되었습니다. | 401 |
| AUTH4009 | 저장된 리프레시 토큰과 일치하지 않습니다. | 401 |

### 일반 에러 (COMMON)

| 코드 | 메시지 | HTTP Status |
|------|--------|-------------|
| COMMON4000 | 잘못된 요청입니다. | 400 |
| COMMON4001 | 지원하지 않는 HTTP 메서드입니다. | 405 |
| COMMON4004 | 요청한 리소스를 찾을 수 없습니다. | 404 |
| COMMON5000 | 서버 내부 오류가 발생했습니다. | 500 |

### 사용자 관련 (USER)

| 코드 | 메시지 | HTTP Status |
|------|--------|-------------|
| USER4001 | 회원을 찾을 수 없습니다. | 404 |
| USER4002 | 이미 존재하는 이메일입니다. | 409 |
| USER4003 | 로그인에 실패했습니다. | 401 |

---

## 테스트

### 테스트 코드 위치
```
src/test/java/com/myApp/
├── auth/
│   ├── controller/
│   │   └── MemberControllerTest.java
│   └── service/
│       ├── AuthServiceTest.java
│       └── CustomOAuth2MemberServiceTest.java
```

### 테스트 실행
```bash
./gradlew test
```

---

## 보안 고려사항

### 1. Refresh Token Rotation (RTR)
- 재발급 시마다 Refresh Token도 갱신
- 토큰 탈취 위험 최소화

### 2. HttpOnly 쿠키
- Refresh Token을 HttpOnly 쿠키로 전송
- JavaScript에서 접근 불가 (XSS 공격 방어)

### 3. 블랙리스트 관리
- 로그아웃된 Access Token을 Redis에 저장
- 남은 유효시간만큼만 저장 (자동 만료)

### 4. CSRF 비활성화
- Stateless JWT 방식이므로 CSRF 토큰 불필요
- API 서버로만 사용하는 경우 안전

### 5. 짧은 Access Token 만료시간
- 3분으로 설정하여 토큰 탈취 위험 최소화
- Refresh Token으로 자동 재발급

---

## 라이선스

MIT License

---

## 기여

이슈 및 Pull Request는 언제나 환영합니다!

1. Fork this repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 문의

프로젝트에 대한 문의사항은 이슈로 남겨주세요.
