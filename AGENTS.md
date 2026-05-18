# 프로젝트 작업 가이드

프로젝트: Spring Boot OAuth2 + JWT 초기 세팅 프로젝트

이 파일은 AI 작업자가 가장 먼저 읽는 루트 가이드다. 변경은 작게 유지하고,
기존 구조를 우선하며, 코드 변경 후에는 가장 가까운 검증을 실행한다.

## 기술 스택

- 프레임워크: Spring Boot 3.2.0
- 언어: Java 21
- 빌드: Gradle
- 데이터 접근: Spring Data JPA
- 데이터베이스: MySQL(prod), H2(dev)
- 인증/인가: Spring Security, OAuth2 Client, JWT(JJWT)
- 토큰 저장: Spring Data Redis(non-dev), 메모리 저장소(dev)
- API 문서화: Swagger/OpenAPI, springdoc
- 공통 라이브러리: Lombok, Validation, AOP, AWS S3 SDK

## 아키텍처 규칙

- CRITICAL: Swagger/OpenAPI 어노테이션은 controller 구현체가 아니라 반드시 `*ControllerDocs` 인터페이스에 작성한다.
- CRITICAL: 공개 controller는 문서 인터페이스가 있거나 새로 추가되는 경우 대응하는 `*ControllerDocs`를 구현해야 한다.
- CRITICAL: `dev` 프로파일은 외부 DB 없이 실행 가능해야 한다.
- CRITICAL: 실제 secret은 `application*.yml`에 넣지 않는다. 환경변수 placeholder를 사용한다.
- CRITICAL: 모듈 경계, 프로파일 동작, 엔드포인트 문서화 규칙, 인증 흐름, 응답 계약, 검증 명령이 바뀌면 관련 `AGENTS.md` 또는 `docs/*.md`를 같은 변경에서 최신화한다.
- API 응답은 `ApiResponse`와 `global.apiPayload.code.status`의 상태 코드를 사용한다.
- controller는 요청 매핑, 입력 처리, 응답 wrapping에 집중하고 주요 판단은 service에 둔다.
- DTO와 entity는 분리한다. controller에서 JPA entity를 직접 반환하지 않는다.
- 기대 가능한 비즈니스 실패는 `GeneralException`과 `BaseCode` 구현체로 표현한다.
- 인증 사용자 조회는 가능하면 기존 `@AuthUser` resolver 패턴을 사용한다.
- token 저장, 조회, blacklist 동작은 `TokenStore` 경계를 통해 다룬다.
- `auth`, `domain`, `global`의 책임을 섞지 않는다.
- 디렉토리 구조, 패턴, 데이터 흐름, 상태 관리는 `docs/ARCHITECTURE.md`를 기준으로 판단한다.

## 모듈 규칙

- `auth`, `domain`, `global` 책임은 `docs/ARCHITECTURE.md`와 하위 `AGENTS.md`를 따른다.
- 각 모듈에서 작업하기 전 가장 가까운 하위 `AGENTS.md`를 읽는다.
- 하위 `AGENTS.md`가 루트보다 구체적인 규칙을 제공하면 하위 문서를 우선한다.
- `docs/*.md`는 보조 문서다. 모든 문서를 매번 읽지 말고, 작업 유형에 맞는 문서만 골라 읽는다.

## 설정 규칙

- `application.yml`은 공통 기본값만 둔다.
- profile별 설정은 `application-dev.yml`, `application-prod.yml`에 둔다.
- 기존 S3/profile/token-store 설정은 안정화된 인프라로 취급한다. 설정 변경 요청이 명시된 경우에만 건드린다.
- profile/config 변경 시 `dev`와 `prod` 동작이 섞이지 않았는지 확인한다.

## 검증 규칙

- 기본 검증 명령: `./gradlew test`.
- `./gradlew test`가 `gradle/wrapper/gradle-wrapper.jar` 누락으로 실패하면 애플리케이션 실패가 아니라 wrapper 환경 문제로 보고한다.
- Gradle 실행이 불가능하면 `git diff --check`와 대상 파일 정적 검토를 최소 검증으로 수행한다.
- controller 변경은 `@WebMvcTest` 또는 가까운 controller test를 우선한다.
- service 변경은 Spring context가 필요한 경우가 아니면 Mockito unit test를 우선한다.
- 검증하지 못한 부분은 최종 보고에 명시한다.

## 가이드 최신화 규칙

- 이 파일과 하위 `AGENTS.md`는 프로젝트가 진행되며 계속 최신 상태를 유지한다.
- 반복되는 결정이 프로젝트 규칙이 되거나, 새 모듈이 생기거나, 기존 규칙이 코드와 맞지 않으면 가이드를 수정한다.
- 긴 설명보다 짧은 운영 규칙을 선호한다.
- 아키텍처 흐름은 `docs/ARCHITECTURE.md`, 도메인 암묵지는 `docs/DOMAIN.md`, 금지 패턴은 `docs/ANTIPATTERNS.md`에 둔다.
- 반복 작업 절차는 `docs/WORKFLOWS.md`, profile/환경변수 계약은 `docs/CONFIGURATION.md`에 둔다.
- 루트/하위 `AGENTS.md`는 자동 로드되는 지침이므로 짧고 넓게 적용되는 규칙만 둔다. 긴 설명, 표, 예시는 `docs/`로 분리한다.
- 임시 구현 세부사항은 미래 작업자가 반드시 보존해야 하는 경우에만 문서화한다.
- feature/config 변경의 최종 보고에는 가이드 문서를 업데이트했는지, 또는 업데이트가 필요 없었던 이유를 포함한다.

## 참고 문서

- `docs/README.md`: 문서 인덱스와 읽는 순서.
- `docs/ARCHITECTURE.md`: 디렉토리 구조, 패턴, 데이터 흐름, 상태 관리.
- `docs/WORKFLOWS.md`: API 추가, 도메인 추가, 인증/설정 변경 체크리스트.
- `docs/CONFIGURATION.md`: profile별 설정과 환경변수 계약.
- `docs/DOMAIN.md`: 도메인 모델, 인증 의미, 응답 계약.
- `docs/ANTIPATTERNS.md`: 이 프로젝트에서 피해야 할 변경 방식.
- `src/main/java/com/myApp/auth/AGENTS.md`: 인증 모듈 작업 규칙.
- `src/main/java/com/myApp/domain/AGENTS.md`: 비즈니스 도메인 모듈 작업 규칙.
- `src/main/java/com/myApp/global/AGENTS.md`: 전역 설정/공통 모듈 작업 규칙.
