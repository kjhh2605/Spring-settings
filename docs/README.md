# 문서 인덱스

AI 작업자는 루트 `AGENTS.md`를 먼저 읽고, 작업 성격에 따라 아래 문서를 추가로 확인한다.
`AGENTS.md`는 자동 로드되는 작업 지침이고, `docs/*.md`는 필요한 경우에만 읽는 보조 문서다.

## 읽는 순서

1. `AGENTS.md`: 전체 기술 스택, CRITICAL 규칙, 검증 기준.
2. `docs/ARCHITECTURE.md`: 디렉토리 구조, 레이어 패턴, 데이터 흐름, 상태 관리.
3. `docs/WORKFLOWS.md`: 작업 유형별 체크리스트.
4. `docs/CONFIGURATION.md`: profile별 설정과 환경변수 계약.
5. `docs/DOMAIN.md`: Member/인증 의미, token 저장 경계, API 응답 계약.
6. `docs/ANTIPATTERNS.md`: 변경 전 피해야 할 패턴.
7. 변경 위치의 하위 `AGENTS.md`: 모듈별 구체 규칙.

## 하위 가이드

- `src/main/java/com/myApp/auth/AGENTS.md`: 인증/인가 모듈.
- `src/main/java/com/myApp/domain/AGENTS.md`: 비즈니스 도메인 모듈.
- `src/main/java/com/myApp/global/AGENTS.md`: 전역 설정/공통 모듈.

## 유지보수 원칙

- 프로젝트 규칙이 바뀌면 해당 문서를 같은 변경에서 갱신한다.
- 문서가 서로 중복될 때는 `AGENTS.md`에는 규칙, `ARCHITECTURE.md`에는 흐름, `WORKFLOWS.md`에는 절차, `CONFIGURATION.md`에는 설정 계약, `DOMAIN.md`에는 의미, `ANTIPATTERNS.md`에는 금지 패턴을 둔다.
- 루트/하위 `AGENTS.md`에는 자주 필요한 규칙만 둔다. 표, 긴 예시, 환경변수 목록은 `docs/`에 둔다.
- 임시 구현 설명은 남기지 않는다. 반복해서 지켜야 할 결정만 기록한다.
