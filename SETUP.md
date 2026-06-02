# 프로젝트 설정 방법 (PrintMall Spring Boot)

Spring Boot 3.2.3 + Java 17 + MyBatis + Thymeleaf 기반 웹 애플리케이션입니다.
데이터베이스는 **PostgreSQL 16** 을 사용합니다(과거 MySQL → PostgreSQL 마이그레이션 완료).

- 애플리케이션 포트: **8081**
- DB: PostgreSQL 16, 기본 DB명 `spring_boot_app`
- 기동 시 `schema/*.sql` 스크립트가 자동 적용됩니다(`app.schema.auto-apply=true`).

---

## 1. 사전 준비물

| 항목 | 버전 | 비고 |
| --- | --- | --- |
| JDK | 17 | `java -version` |
| Maven | 3.9+ | 프로젝트에 wrapper 미포함, 시스템 mvn 사용 |
| PostgreSQL | 16 | 호스트 설치 또는 Docker 중 택1 |
| (선택) Docker Desktop | 최신 | Docker로 실행 시. Windows는 WSL2 필요 |

---

## 2. 실행 방법 A — Docker (권장, 가장 간단)

`docker-compose.yml` 에 PostgreSQL과 앱이 함께 정의되어 있습니다.

```bash
# 프로젝트 루트에서
docker compose up -d --build
```

- `postgres` : PostgreSQL 16 컨테이너 (호스트 **5433** → 컨테이너 5432)
- `app`      : 앱 컨테이너 (호스트 **8081**), `postgres`가 healthy 된 후 기동

접속: http://localhost:8081/

기본 계정/DB (환경변수로 변경 가능):

| 항목 | 기본값 |
| --- | --- |
| POSTGRES_DB | `spring_boot_app` |
| POSTGRES_USER | `appuser` |
| POSTGRES_PASSWORD | `apppass` |

상태 확인 / 로그 / 종료:

```bash
docker compose ps
docker compose logs -f app
docker compose down        # 컨테이너 중지(데이터 볼륨 유지)
docker compose down -v     # DB 볼륨까지 삭제(초기화)
```

> Windows에서 Docker 엔진이 안 뜨면 WSL2가 필요합니다. 관리자 PowerShell에서 `wsl --install` 후 재부팅하세요.

---

## 3. 실행 방법 B — 호스트 PostgreSQL + Maven

### 3-1. PostgreSQL 준비

**일반(서비스) 설치 시**: PostgreSQL 16 설치 후 DB 생성

```sql
CREATE DATABASE spring_boot_app;
```

앱 기본 접속 정보(`application.properties`):

```
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_boot_app
spring.datasource.username=postgres
spring.datasource.password=postgres
```

**관리자 권한 없이(비서비스) 사용 시**: 동봉된 스크립트로 사용자 영역에 클러스터를 띄울 수 있습니다.
(이 PC처럼 5432 바인딩이 막힌 환경에서는 **5433** 포트를 사용합니다.)

```powershell
# PostgreSQL 16 바이너리가 C:\Program Files\PostgreSQL\16 에 설치되어 있어야 함
.\scripts\pg-host-start.ps1   # D:\pgdata-springboot 클러스터 initdb + 5433 기동 + DB 생성
.\scripts\pg-host-stop.ps1    # 중지
```

### 3-2. 앱 실행

```powershell
# 기본 5432 외 포트(예: 5433)를 쓸 경우 환경변수로 오버라이드
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5433/spring_boot_app'
$env:SPRING_DATASOURCE_USERNAME='postgres'
$env:SPRING_DATASOURCE_PASSWORD='postgres'

mvn spring-boot:run
```

빌드만:

```bash
mvn -DskipTests clean package
java -jar target/spring-boot-app-0.0.1-SNAPSHOT.jar
```

접속: http://localhost:8081/

---

## 4. Eclipse / STS 연동

이 프로젝트는 이미 Eclipse(m2e) 프로젝트로 구성되어 있습니다(`.project`, `.classpath`, `.settings`).

1. **File → Import → Existing Maven Projects** 로 프로젝트 폴더 선택
2. import 후 또는 pom 변경 후: 프로젝트 우클릭 → **Maven → Update Project (Alt+F5)**
   - "Force Update of Snapshots/Releases" 체크 → OK
3. 실행: `SpringBootAppApplication` 우클릭 → Run As → Spring Boot App
   - DB 접속 정보는 Run Configuration의 환경변수(또는 `application.properties`)로 지정

> 클래스패스는 m2e가 `pom.xml`에서 자동 동기화하므로 별도 jar 추가가 필요 없습니다. (JRE: JavaSE-17)

---

## 5. 주요 설정 / 환경변수

| 환경변수 | 설명 | 기본값 |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/spring_boot_app` |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 | `postgres` (docker: `appuser`) |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | `postgres` (docker: `apppass`) |
| `SPRING_PROFILES_ACTIVE` | 프로파일 | (호스트) 없음 / (docker) `docker` |
| `APP_STORAGE_NAS_BASE_PATH` | 파일 저장 경로 | 호스트: `D:\nas-storage\printmall\uploads` |
| `APP_PUBLIC_PATH_SECRET` | 암호화 경로 시크릿 | 운영 시 반드시 변경 |
| `RRNO_CRYPTO_SECRET_KEY` | 주민번호 AES 키(32자) | 운영 시 반드시 변경 |

- 스키마 자동 적용: `app.schema.auto-apply=true` (기동 시 `SchemaScriptCatalog` 순서대로 적용, 개별 스크립트 실패는 건너뜀)
- 기동 후 DB 접속 재확인: `app.datasource.startup-check=true`

---

## 6. 포트 참고

| 포트 | 용도 |
| --- | --- |
| 8081 | 애플리케이션(HTTP) |
| 5432 | PostgreSQL 표준(호스트 일반 설치) |
| 5433 | PostgreSQL (Docker 매핑 / 비서비스 호스트 클러스터) |

> Docker와 호스트 PostgreSQL은 둘 다 호스트 **5433** 을 사용하므로 **동시에 실행할 수 없습니다.**
> Docker로 검증할 때 호스트 PG가 떠 있으면 `scripts\pg-host-stop.ps1` 로 먼저 중지하거나 compose 포트를 `5434:5432`로 변경하세요.

---

## 7. 데이터베이스 스키마

- DDL/시드 스크립트: `src/main/resources/schema/*.sql` (PostgreSQL 16 문법)
- 기동 시 적용 순서: `src/main/java/.../config/SchemaScriptCatalog.java`
- 수동 일괄 적용용: `schema/00_create_all_tables.sql`, `schema/01_seed_all.sql` (DBeaver/psql에서 직접 실행)
- 정상 기동 시 public 스키마에 약 **42개 테이블**이 생성됩니다.

---

## 8. 트러블슈팅

- **`Permission denied` (5432 바인딩 실패)**: 다른 포트(5433 등)로 기동하고 `SPRING_DATASOURCE_URL`을 맞춰주세요.
- **`role "..." does not exist`**: 접속 사용자/비밀번호가 DB 계정과 일치하는지 확인(Docker는 `appuser`).
- **한글 깨짐**: DB 인코딩은 UTF8이며 데이터는 정상입니다. 콘솔 출력만 깨져 보이면 `chcp 65001` 후 확인하세요.
- **Docker 엔진 미기동(Windows)**: `wsl --install`(관리자) + 재부팅 후 Docker Desktop 실행.
