# Eclipse / STS 연동 가이드

Spring Boot Maven 프로젝트 `spring-boot-app`을 Eclipse·Spring Tools Suite(STS)에서 개발·실행하는 방법입니다.

## 필요 환경

| 항목 | 권장 |
|------|------|
| IDE | Eclipse 2023-12+ 또는 **Spring Tools 4** (STS) |
| JDK | **17** (Workspace 기본 JRE도 17) |
| Maven | 3.9+ (Eclipse 내장 m2e 사용 가능) |
| DB | MySQL 8.x, DB `spring_boot_app` |

## 1. 프로젝트 가져오기

### 방법 A — Maven 프로젝트 Import (권장)

1. **File → Import → Maven → Existing Maven Projects**
2. **Root Directory**: 이 폴더 선택  
   `spring-boot-app-fixed` (또는 `pom.xml`이 있는 루트)
3. **Projects**에 `spring-boot-app` 체크 → **Finish**
4. 우클릭 프로젝트 → **Maven → Update Project** (Alt+F5) → **Force Update** 체크 → OK

### 방법 B — 기존 Eclipse 메타데이터 사용

저장소에 `.project`, `.classpath`, `.settings`, `.launch`가 포함되어 있으면:

1. **`open-sts-workspace.bat`** 실행 → STS 워크스페이스 `new-workspace` 열기
2. **최초 1회**: `scripts/eclipse/import-maven-project.bat` 안내에 따라 Maven Import
3. **File → Open Projects from File System** (또는 Maven Import) → 프로젝트 루트 지정 → **Finish**
4. **Maven → Update Project** 실행

점검: `powershell -File scripts/eclipse/verify-eclipse-setup.ps1`

## 2. JDK 17 설정

1. **Window → Preferences → Java → Installed JREs**  
   - JDK 17 추가 후 **default**로 지정
2. **Java → Compiler** → Compiler compliance level: **17**
3. 프로젝트 우클릭 → **Properties → Java Build Path → Libraries**  
   - JRE가 **JavaSE-17**인지 확인

## 3. UTF-8 (한글 깨짐 방지)

1. **Window → Preferences → General → Workspace**  
   - Text file encoding: **UTF-8**
2. **Java → Installed JREs → [JDK 17] → Edit → Default VM arguments** (선택):

   ```
   -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8
   ```

## 4. 실행

### Run Configuration (저장됨)

**Run → Run Configurations** 또는 **Debug Configurations**에서:

| 이름 | 용도 |
|------|------|
| `spring-boot-app-java` | `SpringBootAppApplication` Java Application 실행 |
| `spring-boot-app-maven` | `mvn clean spring-boot:run` (MySQL 선행 확인 권장) |

`.launch` 폴더의 설정을 **Run Configurations → Java Application / Maven Build → Import launch configurations**로 가져올 수도 있습니다.

### STS Spring Boot Dashboard (STS만)

1. **Window → Show View → Other → Spring → Boot Dashboard**
2. 프로젝트 `spring-boot-app` → **(Re)start**

### Windows 배치 (Eclipse 밖)

```bat
run-server.bat
```

MySQL 기동·8081 포트 정리 후 `mvn spring-boot:run` 실행.

## 5. 접속 URL

- 앱: http://localhost:8081/
- 업체관리: http://localhost:8081/admin/companies

## 6. 자주 나는 문제

| 증상 | 조치 |
|------|------|
| Maven 의존성 오류 | 프로젝트 우클릭 → **Maven → Update Project** |
| `target` 없음 / 클래스 못 찾음 | **Run As → Maven build…** → goals: `compile` |
| Spring Boot nature 오류 | STS/Spring Tools 플러그인 설치, 또는 `.project`에서 spring nature 제거 후 Maven만 사용 |
| DB 연결 실패 | MySQL 기동, `application.properties` 계정·DB 확인, `application-local.properties` 참고 |
| 포트 8081 사용 중 | `run-server.bat` 2단계 또는 작업 관리자에서 해당 PID 종료 |

## 7. Git과 Eclipse 파일

`.project`, `.classpath`, `.settings`, `.launch`는 팀 import 편의를 위해 저장소에 포함할 수 있습니다.  
로컬만 쓰려면 `.gitignore`에 다시 추가해 커밋에서 제외하세요.
