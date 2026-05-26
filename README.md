# Spring Boot Application

새로운 Spring Boot 프로젝트입니다.

## 실행 방법

```bash
mvn spring-boot:run
```

또는

```bash
mvn clean package
java -jar target/spring-boot-app-0.0.1-SNAPSHOT.jar
```

## STS (Spring Tool Suite) 워크스페이스

| 항목 | 경로 |
|------|------|
| **STS 워크스페이스** | `D:\sts-workspace` (`scripts/eclipse/sts-workspace.local.txt`) |
| **소스 폴더** | `spring-boot-app-fixed` (Cursor·STS 동일) |
| **프로젝트** | `spring-boot-app-fixed` → Eclipse 이름 `spring-boot-app` |
| **JDK** | Java 17 (`C:\Users\chung\.local\dev\jdk-17` 권장) |
| **포트** | 8081 (`application.properties`) |

### STS에서 열기

1. **`open-sts-workspace.bat`** 또는 **File → Switch Workspace** → `D:\sts-workspace`
2. **File → Import → Maven → Existing Maven Projects**
3. **Root Directory** → `spring-boot-app-fixed` 선택 후 Finish
4. **Window → Preferences → Java → Installed JREs** 에 JDK 17 등록
5. 프로젝트 우클릭 → **Maven → Update Project** (Force Update 체크)

### 실행 구성 (`.launch/`)

- `spring-boot-app-spring-boot.launch` — Spring Boot Dashboard 실행 (권장)
- `spring-boot-app-java.launch` — Java Application
- `spring-boot-app-maven.launch` — `spring-boot:run` (Maven)

`open-sts-workspace.bat` — STS 5.1.1 + `D:\sts-workspace` (경로는 `scripts/eclipse/sts-path.local.txt` 등)

**워크스페이스 연동 1회:** `sync-eclipse-workspace.bat` (JDK·UTF-8·자동 새로고침·점검)

> MySQL(`localhost:3306`, DB `spring_boot_app`)이 실행 중이어야 앱이 기동됩니다.

### Cursor ↔ STS 소스 연동

두 IDE는 **같은 폴더** `spring-boot-app-fixed` 를 열어야 합니다. 별도 복사본을 만들지 마세요.

| 도구 | 열 경로 |
|------|---------|
| **Cursor** | `...\new-workspace\spring-boot-app-fixed` |
| **STS** | 워크스페이스 `D:\sts-workspace` + 프로젝트 `spring-boot-app-fixed` |

**STS 워크스페이스 1회 설정** (`new-workspace-sts.prefs` 참고)

- **Preferences → General → Workspace**
  - Refresh using native hooks or polling ✓
  - Refresh on access ✓
  - Build automatically ✓

**동작**

1. 한쪽에서 저장 → 디스크에 같은 파일이 갱신됨
2. STS는 자동 새로고침·빌드로 `target/classes` 반영
3. 실행 중 서버는 **DevTools**가 `src/main/java`, `src/main/resources` 변경을 감지해 재시작 (약 2초)

**권장**

- 서버는 STS 또는 `run-server.bat` 중 **한 곳에서만** 실행
- `pom.xml` 수정 후 STS: **Maven → Update Project**

## 프로젝트 구조

```
spring-boot-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/springbootapp/
│   │   │       └── SpringBootAppApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/springbootapp/
│               └── SpringBootAppApplicationTests.java
└── pom.xml
```

## 기술 스택

- Spring Boot 3.2.3
- Java 17
- Maven
