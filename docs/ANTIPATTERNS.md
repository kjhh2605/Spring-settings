# 안티패턴

이 문서는 프로젝트 변경 전에 확인해야 할 금지 패턴을 정리한다.

## 설정

- 실제 secret을 `application*.yml`에 넣지 않는다.
- `dev`를 외부 MySQL에 의존하게 만들지 않는다.
- entity DDL 동작을 확인하지 않고 H2 MySQL 호환 설정을 제거하지 않는다.
- S3를 무조건 활성화하지 않는다. 로컬 개발은 AWS credential 없이 가능해야 한다.
- prod 전용 인프라 가정을 `application.yml`에 섞지 않는다. profile별 설정 파일을 사용한다.

## 인증

- 새 인증 동작에서 Redis repository/template에 직접 의존하지 않는다. `TokenStore`를 사용한다.
- `AuthService.reissue`의 refresh token 회전을 우회하지 않는다.
- refresh token을 JPA entity에 저장하지 않는다.
- 기존 JWT 필터나 resolver로 처리 가능한 인증 정보를 controller에서 직접 파싱하지 않는다.
- `AuthTestController`를 `dev` 외 profile에 노출하지 않는다.
- JWT subject 의미를 가볍게 바꾸지 않는다. 현재 subject는 member email이다.

## 도메인과 API

- Swagger/OpenAPI 어노테이션을 controller 구현체 메서드에 직접 붙이지 않는다. `*ControllerDocs`를 사용한다.
- JPA entity를 API controller에서 직접 반환하지 않는다.
- 임의 응답 구조를 만들지 않는다. `ApiResponse`를 사용한다.
- 기대 가능한 비즈니스 오류에 raw `RuntimeException`을 던지지 않는다. `GeneralException`과 상태 코드를 사용한다.
- 비즈니스 도메인 로직을 `global`에 넣지 않는다.
- `domain/users`가 login, token refresh, OAuth provider 매핑을 책임지게 만들지 않는다.

## 영속성

- controller 직렬화에서 lazy-loaded entity graph에 기대지 않는다.
- JSON 직렬화와 query 동작을 확인하지 않고 양방향 entity 관계를 추가하지 않는다.
- H2에서 동작한다고 MySQL production SQL 세부 동작까지 검증됐다고 가정하지 않는다.
- 명시적 결정 없이 Flyway/Liquibase 같은 migration 도구를 추가하지 않는다.

## 테스트

- auth service test를 Redis 구현 상세에 묶지 않는다.
- WebMvc test나 Mockito test로 충분한데 전체 Spring context test를 사용하지 않는다.
- Gradle wrapper jar 누락을 애플리케이션 테스트 실패로 해석하지 않는다.
- formatting/static check만 실행하고 동작을 검증했다고 주장하지 않는다.

## 문서

- 미래 작업자가 지켜야 할 규칙을 README에만 기록하지 않는다. `AGENTS.md`나 관련 하위 가이드에 둔다.
- 긴 코드 목록을 여러 문서에 중복하지 않는다. 파일명과 결정만 기록한다.
- 모듈 책임, profile 동작, API 문서화 규칙, 응답 계약, 검증 방식이 바뀐 뒤 `AGENTS.md`를 방치하지 않는다.
