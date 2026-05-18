# 작업 워크플로우

이 문서는 자주 발생하는 변경 유형별 체크리스트다. 실제 코드 규칙은 루트
`AGENTS.md`와 변경 위치의 하위 `AGENTS.md`를 함께 따른다.
필요한 섹션만 읽고 적용한다.

## 새 API 엔드포인트 추가

1. 변경 위치를 정한다.
   - 인증/토큰 관련: `auth`.
   - 비즈니스 기능: `domain/<feature>`.
   - 공통 응답/보안/설정: `global`.
2. controller method를 추가한다.
   - controller는 요청 매핑, 입력 처리, 응답 wrapping만 담당한다.
   - 응답은 `ApiResponse`를 사용한다.
3. 대응하는 `*ControllerDocs` 인터페이스를 같은 변경에서 수정한다.
   - Swagger/OpenAPI 어노테이션은 controller 구현체에 직접 쓰지 않는다.
4. service에 유스케이스를 둔다.
5. 필요한 DTO를 추가한다.
   - JPA entity를 controller 응답으로 직접 반환하지 않는다.
6. 기대 가능한 실패가 있으면 `GeneralException`과 상태 코드를 사용한다.
7. 검증한다.
   - controller: `@WebMvcTest` 또는 가까운 controller test.
   - service: Mockito 단위 테스트.
8. 문서 최신화가 필요한지 확인한다.
   - API 문서화 규칙, 응답 계약, 인증 흐름이 바뀌면 관련 `AGENTS.md`/`docs/*.md`를 갱신한다.

## 새 도메인 추가

1. `src/main/java/com/myApp/domain/<domain>` 아래에 패키지를 만든다.
2. 기본 구조는 `controller`, `service`, `dto`를 우선 사용한다.
3. 영속 entity/repository가 필요하면 기존 `Member`/repository 스타일과 충돌하지 않게 위치를 정한다.
4. controller가 생기면 `*ControllerDocs` 인터페이스도 함께 만든다.
5. `docs/ARCHITECTURE.md`의 디렉토리 구조 또는 변경 위치 선택 기준이 달라지면 갱신한다.
6. 하위 모듈 규칙이 필요할 만큼 도메인이 커지면 해당 디렉토리에 `AGENTS.md`를 추가한다.
7. controller/service 테스트를 추가하거나 가까운 테스트를 확장한다.

## 인증/토큰 로직 변경

1. 먼저 `src/main/java/com/myApp/auth/AGENTS.md`와 `docs/DOMAIN.md`를 읽는다.
2. JWT subject가 member email이라는 전제를 깨지 않는지 확인한다.
3. refresh token 저장/조회/삭제 또는 blacklist는 `TokenStore`를 통해 처리한다.
4. Redis 구현 상세를 controller/service에 직접 노출하지 않는다.
5. `AuthTestController`는 `dev` 전용으로 유지한다.
6. `AuthService.reissue`의 refresh token 회전을 유지한다.
7. 검증한다.
   - `AuthService`: Mockito 단위 테스트.
   - `JwtTokenProvider`: token 생성/만료/claim parsing 테스트.
   - controller: `@WebMvcTest`.
8. 인증 흐름 의미가 바뀌면 `docs/DOMAIN.md`와 관련 `AGENTS.md`를 갱신한다.

## 설정/Profile 변경

1. `docs/CONFIGURATION.md`에서 현재 profile/환경변수 계약을 확인한다.
2. 공통 기본값은 `application.yml`, profile별 값은 `application-dev.yml` 또는 `application-prod.yml`에 둔다.
3. `dev`가 외부 DB 없이 실행 가능한지 유지한다.
4. 실제 secret을 YAML에 넣지 않는다.
5. S3/profile/token-store 설정은 명시적 요청이 있을 때만 바꾼다.
6. 검증한다.
   - 가능한 경우 `./gradlew test`.
   - Gradle 실행이 불가능하면 `git diff --check`와 설정 파일 정적 검토.
7. profile 동작이나 필수 환경변수가 바뀌면 `docs/CONFIGURATION.md`와 루트 `AGENTS.md`를 갱신한다.

## 공통 응답/예외 변경

1. `src/main/java/com/myApp/global/AGENTS.md`를 먼저 읽는다.
2. 성공/실패 코드는 `global.apiPayload.code.status`에 둔다.
3. 기대 가능한 비즈니스 오류는 `GeneralException`으로 표현한다.
4. `ExceptionAdvice` 변경은 전체 API JSON 구조와 HTTP status에 영향을 주는지 확인한다.
5. controller 테스트에서 JSON 구조와 status를 함께 검증한다.
6. 응답 계약이 바뀌면 `docs/DOMAIN.md`, `docs/ARCHITECTURE.md`, 관련 하위 `AGENTS.md`를 갱신한다.
