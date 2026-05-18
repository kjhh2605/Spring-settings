# 설정과 환경변수

이 문서는 profile별 실행 계약과 환경변수 사용 기준을 정리한다.

## Profile 개요

| Profile | 목적 | DB | Token 저장 | S3 |
| --- | --- | --- | --- | --- |
| `dev` | 로컬 개발 기본값 | H2 in-memory | 메모리 `TokenStore` | 비활성 |
| `prod` | 운영/운영 유사 환경 | MySQL | Redis 기반 `TokenStore` | 활성 |

기본 profile은 `SPRING_ACTIVE_PROFILE`이 없을 때 `dev`다.

## 공통 설정

| 변수 | 사용 위치 | 기본값 | 비고 |
| --- | --- | --- | --- |
| `SPRING_APP_NAME` | `spring.application.name` | `myApp` | 애플리케이션 이름 |
| `SPRING_ACTIVE_PROFILE` | `spring.profiles.active` | `dev` | 기본 로컬 실행은 `dev` |
| `DB_URL` | 공통 datasource URL | `jdbc:mysql://localhost:3306/myapp` | `dev`에서는 H2 설정이 덮어씀 |
| `DB_USER` | 공통 datasource username | `root` | `dev`에서는 `sa` |
| `DB_PASSWORD` | 공통 datasource password | `root1234!@` | `dev`에서는 빈 값 |
| `AWS_REGION` | S3 region | 없음 | S3 활성 profile에서 필요 |
| `AWS_S3_BUCKET` | S3 bucket | 없음 | S3 활성 profile에서 필요 |

## Dev profile

`application-dev.yml`은 외부 DB 없이 실행 가능해야 한다.

| 항목 | 값 |
| --- | --- |
| DB URL | `jdbc:h2:mem:myapp;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |
| DB driver | `org.h2.Driver` |
| H2 console | `/h2-console` |
| JPA database | `h2` |
| Hibernate DDL | `update` |
| S3 | `spring.cloud.aws.s3.enabled=false` |
| Redis repository | 비활성 |
| JWT access token | 180초 |
| JWT refresh token | 14일 |

Dev OAuth client 값은 placeholder 기본값을 가진다. 실제 OAuth 로그인을 테스트하려면 아래 환경변수를 주입한다.

| 변수 | 기본값 |
| --- | --- |
| `GOOGLE_CLIENT_ID` | `dev-google-client-id` |
| `GOOGLE_CLIENT_SECRET` | `dev-google-client-secret` |
| `KAKAO_CLIENT_ID` | `dev-kakao-client-id` |
| `KAKAO_CLIENT_SECRET` | `dev-kakao-client-secret` |
| `NAVER_CLIENT_ID` | `dev-naver-client-id` |
| `NAVER_CLIENT_SECRET` | `dev-naver-client-secret` |

## Prod profile

`application-prod.yml`은 외부 인프라가 준비되어 있다는 전제로 동작한다.

| 항목 | 값 |
| --- | --- |
| DB | 공통 datasource 환경변수 사용 |
| JPA DDL | `validate` |
| S3 | `spring.cloud.aws.s3.enabled=true` |
| Redis | `REDIS_HOST`, `REDIS_PORT` 사용 |
| JWT access token | 1800초 |
| JWT refresh token | 14일 |
| OAuth redirect URL | `http://localhost:3000/oauth2/redirect` |

Prod에서 명시적으로 관리해야 하는 환경변수:

| 변수 | 필수 여부 | 비고 |
| --- | --- | --- |
| `SPRING_ACTIVE_PROFILE=prod` | 필수 | prod profile 활성화 |
| `DB_URL` | 필수 | MySQL 접속 URL |
| `DB_USER` | 필수 | DB 사용자 |
| `DB_PASSWORD` | 필수 | DB 비밀번호 |
| `JWT_SECRET` | 필수 | 충분히 긴 base64 secret 사용 |
| `REDIS_HOST` | 필수 | 기본값은 `localhost`지만 운영에서는 명시 권장 |
| `REDIS_PORT` | 필수 | 기본값은 `6379` |
| `AWS_REGION` | 필수 | S3 활성 시 필요 |
| `AWS_S3_BUCKET` | 필수 | S3 활성 시 필요 |
| `GOOGLE_CLIENT_ID` | OAuth 사용 시 필수 | Google login |
| `GOOGLE_CLIENT_SECRET` | OAuth 사용 시 필수 | Google login |
| `KAKAO_CLIENT_ID` | OAuth 사용 시 필수 | Kakao login |
| `KAKAO_CLIENT_SECRET` | OAuth 사용 시 필수 | Kakao login |
| `NAVER_CLIENT_ID` | OAuth 사용 시 필수 | Naver login |
| `NAVER_CLIENT_SECRET` | OAuth 사용 시 필수 | Naver login |

`application-prod.yml`에 일부 dev 편의용 fallback이 남아 있더라도 prod 실행에서는 반드시 환경변수로 override한다.

## 변경 규칙

- 새 환경변수를 추가하면 이 문서와 관련 `application-*.yml`을 함께 갱신한다.
- profile별 의미가 바뀌면 루트 `AGENTS.md`의 CRITICAL 규칙과 충돌하지 않는지 확인한다.
- secret 기본값은 dev 편의용일 때만 허용한다. prod secret은 환경변수로만 주입한다.
- `dev`는 외부 DB 없이 실행 가능해야 한다.
