# Global 모듈 가이드

범위: `src/main/java/com/myApp/global` 아래 모든 파일.

## 책임

- Spring 설정과 횡단 관심사.
- API 응답 래퍼와 상태 코드 추상화.
- 전역 예외 처리.
- 공통 DTO, base entity, 로깅, 선택적 인프라 연동.

## 경계

- `global`에는 비즈니스 도메인 유스케이스를 넣지 않는다.
- 보안 접근 규칙은 `SecurityConfig`에 두되, token 의미와 저장 정책은 `auth`에 둔다.
- API 응답/에러 코드 변경은 전체 controller/test에 영향이 있으므로 영향 범위를 확인한다.
- Swagger/OpenAPI 설정은 이 모듈에 둘 수 있지만, 엔드포인트별 어노테이션은 각 모듈의 `*ControllerDocs` 인터페이스에 둔다.

## 응답과 예외 계약

- 성공/실패 코드는 `global.apiPayload.code.status`에 추가한다.
- 기대 가능한 비즈니스 오류는 `GeneralException`으로 흘려보낸다.
- `ExceptionAdvice`는 exception을 `ApiResponse`로 변환하는 중심 위치다.
- 보안 필터 레벨 실패는 필요하면 `ApiResponse.Body`를 직접 작성할 수 있다.

## 설정 주의점

- `SecurityConfig` 변경은 Swagger, H2 console, dev auth endpoint, 보호 endpoint에 영향을 줄 수 있다.
- CORS 변경은 browser client와 OAuth redirect에 영향을 준다.

## 검증

- `SecurityConfig` 변경은 공개 endpoint와 인증 필요 endpoint를 대표로 검증한다.
- 응답/에러 변경은 JSON 구조와 HTTP status를 함께 확인한다.
- S3 변경은 가능하면 enabled/disabled profile 동작을 모두 확인한다.
