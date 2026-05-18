# 도메인 노트

이 문서는 코드만 보면 놓치기 쉬운 도메인 의미와 인증 규칙을 기록한다.
모듈 경계와 요청 흐름은 `docs/ARCHITECTURE.md`를 기준으로 본다.

## Member 모델

`Member`는 사용자 식별을 담당하는 JPA entity다.

- 기본 키: 생성되는 `Long id`.
- 로그인 식별자: `email`.
- 권한: `Role` enum.
- 소셜 식별자: `socialType`, `socialId`.
- 생성/수정 시각: `BaseEntity`에서 상속.
- controller 응답에는 `Member`를 직접 노출하지 않고 DTO를 사용한다.

## 인증 의미

- JWT subject는 현재 member email로 해석한다.
- OAuth2 login은 provider attribute로 member를 찾거나 생성한 뒤 JWT를 발급한다.
- Dev login은 로컬 개발 편의를 위한 흐름이며 OAuth2 browser flow를 건너뛴다.
- Refresh token 재발급은 저장된 refresh token 값과 요청 token 값이 일치해야 한다.
- Refresh token은 재발급 시 새 값으로 회전한다.
- Logout은 refresh token을 삭제하고 남은 access token 수명만큼 blacklist를 기록한다.
- `@CheckBlacklist`가 붙은 경로는 `BlacklistAspect`를 통해 logout된 access token을 거부한다.

## TokenStore 규칙

- 인증 로직은 Redis 구현체에 직접 의존하지 않고 `TokenStore`에 의존한다.
- profile별 token 저장 구현은 이미 세팅되어 있으므로, auth storage나 profile 동작 변경 요청이 있을 때만 수정한다.

## API 응답 계약

Public API controller는 `ApiResponse`를 반환한다.

- 성공: `ApiResponse.onSuccess(GeneralSuccessCode._OK, result)`.
- 기대 가능한 실패: 구체적인 error code를 담은 `GeneralException`.
- 보안 필터 레벨 실패: `ApiResponse.createFailureBody` 사용 가능.
- 일반 controller에서 응답 JSON을 직접 조립하지 않는다.
- Swagger/OpenAPI 어노테이션은 controller 구현체가 아니라 `*ControllerDocs` 인터페이스에 둔다.

## 프로파일과 인프라

- 기본 로컬 동작은 `dev` 기준으로 이미 설정되어 있다.
- 안정화된 profile/S3/token-store 설정은 요청이 명시된 경우에만 재작업한다.
