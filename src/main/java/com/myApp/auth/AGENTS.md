# Auth 모듈 가이드

범위: `src/main/java/com/myApp/auth` 아래 모든 파일.

## 책임

- OAuth2 사용자 로딩과 최초 로그인 member 생성.
- JWT 생성, 파싱, 검증, 인증 필터 지원.
- `TokenStore`를 통한 refresh token 저장/조회/삭제와 access token blacklist 처리.
- Dev 전용 테스트 로그인 엔드포인트.
- `@CheckBlacklist` 같은 인증 어노테이션과 aspect.

## 경계

- 새 token 저장 동작은 `TokenStore`를 통해 구현한다.
- controller는 refresh token 저장 방식에 직접 의존하지 않는다.
- OAuth provider별 attribute parsing은 `OAuthAttributes`에 둔다.
- Spring Security용 member 조회는 `CustomUserDetailsService`에 둔다.

## Endpoint 규칙

- 실제 인증 엔드포인트는 `AuthController`에 둔다.
- Swagger/OpenAPI 어노테이션은 `AuthControllerDocs`에만 둔다.
- 공개 인증 엔드포인트를 추가하거나 바꾸면 같은 변경에서 `AuthControllerDocs`를 수정한다.
- `AuthTestController`는 반드시 `@Profile("dev")` 아래에 둔다.
- refresh token cookie는 `ResponseCookie`를 사용한다.
- 응답은 원본 DTO가 아니라 `ApiResponse`로 감싼다.

## 주의점

- JWT subject는 member email이다. 변경하면 refresh, logout, user lookup에 영향이 간다.
- Refresh token 회전은 보안 모델의 일부다.

## 검증

- `AuthService` 변경은 Mockito 단위 테스트를 우선한다.
- Auth controller 변경은 `@WebMvcTest`와 mocked `AuthService`를 우선한다.
- `JwtTokenProvider` 변경은 token 생성, 만료, claim parsing을 직접 검증하는 단위 테스트를 추가한다.
