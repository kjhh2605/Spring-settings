# Domain 모듈 가이드

범위: `src/main/java/com/myApp/domain` 아래 모든 파일.

## 책임

- 인증 메커니즘이나 인프라가 아닌 비즈니스 유스케이스.
- 현재 주요 도메인: `users`.
- controller는 요청을 service 호출로 변환하고 응답을 감싼다.
- service는 repository와 도메인 규칙을 조합한다.
- DTO는 API 입출력 형태를 정의한다.

## 경계

- JWT, OAuth2, Redis, S3, 프레임워크 설정 로직을 이 모듈에 넣지 않는다.
- controller에서 JPA entity를 직접 반환하지 않는다.
- service가 controller 클래스에 의존하지 않는다.
- 현재 사용자 정보가 필요하면 가능하면 `@AuthUser`를 사용한다.
- 명확한 유스케이스 없이 도메인끼리 직접 결합하지 않는다.
- Swagger/OpenAPI 어노테이션은 `*ControllerDocs` 인터페이스에만 둔다. controller 구현체에는 두지 않는다.

## Users 도메인

- `UserController`는 사용자 조회 엔드포인트를 담당한다.
- `UserService`는 member 조회 판단과 `Member -> UserResponseDto` 매핑을 담당한다.
- 사용자를 찾지 못한 경우 기존 exception/code 체계로 표현한다.

## 검증

- controller 변경은 대응하는 `*ControllerDocs`와 controller test를 함께 갱신한다.
- service 변경은 조회 성공/실패와 DTO 매핑 test를 우선한다.
