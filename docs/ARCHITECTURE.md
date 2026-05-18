# 아키텍처

이 문서는 디렉토리 구조, 레이어 패턴, 데이터 흐름, 상태 관리 기준을 정리한다.
구현 위치를 고를 때는 이 문서를 먼저 보고, 세부 규칙은 가까운 `AGENTS.md`를 따른다.

## 디렉토리 구조

```text
src/
├── main/
│   ├── java/com/myApp/
│   │   ├── MyApplication.java
│   │   ├── auth/            # 인증/인가, OAuth2, JWT, 토큰 저장 경계
│   │   ├── domain/          # 비즈니스 도메인 유스케이스
│   │   └── global/          # 공통 설정, 응답/예외, 로깅, 선택적 인프라
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
└── test/
    └── java/com/myApp/      # controller/service 중심 테스트
```

## 패턴

- 기본 구조는 Layered Spring MVC다: `Controller -> Service -> Repository`.
- controller는 HTTP 요청/응답 경계만 담당한다.
- service는 유스케이스와 비즈니스 판단을 담당한다.
- repository는 persistence 접근만 담당한다.
- DTO는 API 입출력 형태를 담당하고, JPA entity는 영속 모델로 유지한다.
- Swagger/OpenAPI 엔드포인트 문서는 `*ControllerDocs` 인터페이스에 둔다.
- profile별 인프라 차이는 Spring profile과 bean 경계로 분리한다.

## 데이터 흐름

일반 API:

```text
클라이언트 요청
-> 보안 필터 체인
-> Controller
-> Service
-> Repository / 외부 연동 경계
-> Service 결과
-> ApiResponse
-> 클라이언트 응답
```

인증 사용자 조회:

```text
Authorization 헤더
-> JwtAuthenticationFilter
-> SecurityContext
-> @AuthUser argument resolver
-> Domain controller/service
```

OAuth2 로그인:

```text
OAuth2 provider callback 요청
-> CustomOAuth2UserService
-> Member 조회/생성
-> OAuth2SuccessHandler
-> JwtTokenProvider
-> TokenStore
-> refresh cookie와 함께 redirect
```

Dev 로그인:

```text
GET /api/v1/auth/test/login
-> AuthTestController
-> Member 조회/생성
-> JwtTokenProvider
-> TokenStore(dev에서는 in-memory)
-> ApiResponse<TokenDto>
```

## 상태 관리

- 서버 영속 상태: JPA entity와 repository.
- 인증 토큰 상태: `TokenStore`.
  - `dev`: 메모리 구현체.
  - non-`dev`: Redis 기반 구현체.
- 요청 인증 상태: Spring Security `SecurityContext`.
- API 응답 상태는 저장하지 않는다. controller는 `ApiResponse` envelope를 반환한다.

## 변경 위치 선택

- 새 인증/토큰 동작: `auth`.
- 새 비즈니스 기능: `domain/<feature>`.
- 응답 envelope, 에러 코드, 보안/CORS/Swagger 설정: `global`.
- profile별 실행 설정: `src/main/resources/application-*.yml`.
- 프로젝트 운영 규칙 변경: 루트 `AGENTS.md` 또는 하위 `AGENTS.md`.
