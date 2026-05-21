---
title: Spring Boot App (Falcon UI) — 포트폴리오
author: redcroxx
date: 2026-05-21
---

# Spring Boot App (Falcon UI)

Falcon 템플릿 기반 풀스택 웹 애플리케이션 — 개발환경·화면 구성 요약

| 항목 | 내용 |
|------|------|
| Repository | https://github.com/chungjonguk/test01 |
| 로컬 URL | http://localhost:8081 |
| Thymeleaf 화면 | 243+ 페이지 |

---

## 1. 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.2.3 |
| Language | Java 17 |
| Build | Maven 3.9 |
| View | Thymeleaf |
| UI | Falcon + Bootstrap 5.2.1 |
| Database | MySQL 8 + MyBatis 3 |
| DevTools | Spring DevTools (자동 재시작) |

---

## 2. 개발환경

| 항목 | 설정 |
|------|------|
| IDE | Cursor + STS (동일 프로젝트 폴더) |
| JDK | C:\Users\chung\.local\dev\jdk-17 |
| Maven | apache-maven-3.9.15 |
| 서버 포트 | 8081 |
| Database | localhost:3306 / DB `spring_boot_app` |
| 인코딩 | UTF-8 (서블릿·Thymeleaf·JVM) |
| 로컬 비밀 설정 | `application-local.properties` (git 제외) |
| 실행 | `mvn spring-boot:run` 또는 `run-server.bat` |

### 2.1 로컬 개발 모드

- Thymeleaf·정적 리소스: `src/main/resources` 직접 로드, 캐시 비활성화
- Java 변경: DevTools가 `src/main/java`, `src/main/resources` 감지 후 재시작
- `application-local.properties` / `pom.xml` 변경 시: 서버 완전 재시작 권장

### 2.2 외부 API (로컬)

| 기능 | 설정 키 | 비고 |
|------|---------|------|
| 카카오 주소 검색 | `kakao.client-id` (REST API 키) | `/api/kakao/local/...` |
| 카카오맵 | `kakao.javascript-key` | REST 키와 별도 |
| 카카오 로그인 | `kakao.redirect-uri` | `http://localhost:8081/auth/kakao/callback` |
| 네이버 로그인 | `naver.client-id` | OAuth2 |

카카오 콘솔: JavaScript SDK 도메인 `http://localhost:8081`, 카카오맵 사용 ON

---

## 3. 아키텍처

```
Browser (Falcon UI / Bootstrap 5)
    ↓
Thymeleaf Templates + Static (/assets, /vendors)
    ↓
Controller (page/*) ── MVC 화면
Controller (*Api*) ── REST JSON (/api/*)
    ↓
Service (업무, 카카오 로컬, 인증)
    ↓
MyBatis Mapper + resources/mapper/*.xml
    ↓
MySQL (spring_boot_app)
```

| 계층 | 역할 |
|------|------|
| `controller.page.*` | Falcon 화면 라우팅 |
| `controller.*Api*` | REST API |
| `service` | 비즈니스·외부 API 연동 |
| `mapper` + XML | DB 접근 |
| `web.PageViewAdvice` | 화면별 JS/CSS 플래그 |
| `web.ScreenAccessInterceptor` | screen_list 접근 제어 |

---

## 4. 화면 구성 요약

사이드바·`screen_list` 기준. 총 **243+** Thymeleaf 템플릿.

| 구분 | 규모 | URL 패턴 | 대표 화면 |
|------|------|----------|-----------|
| Dashboard | ~6 | `/dashboard/*` | analytics, crm, e-commerce, lms, project-management, saas |
| App | ~35 | `/app/*` | calendar, chat, email, e-commerce, e-learning, kanban, social |
| Pages | ~40 | `/pages/*` | authentication (simple/card/split/wizard), user, faq, errors |
| Modules | ~120 | `/modules/*` | components, forms, charts (ECharts/Chart.js), maps, utilities |
| Admin·기타 | ~15 | `/admin/*` 등 | 메뉴관리, 공통코드, documentation, demo |

### 4.1 대표 URL

| 화면 | URL |
|------|-----|
| 홈 | http://localhost:8081/ |
| 대시보드 (Analytics) | http://localhost:8081/dashboard/analytics |
| 인증 마법사 (주소·지도) | http://localhost:8081/pages/authentication/wizard |
| 주문 목록 | http://localhost:8081/app/e-commerce/orders/order-list |
| 메뉴 관리 | http://localhost:8081/admin/menus |
| 공통코드 관리 | http://localhost:8081/admin/codes |

---

## 5. 커스텀·연동 기능

템플릿 데모 외에 Spring Boot·DB·외부 API와 연동한 영역.

### 5.1 인증·세션

- 카카오 / 네이버 OAuth2 로그인
- HTTP 세션 타임아웃 10분

### 5.2 마법사 — 주소·카카오맵 (`/pages/authentication/wizard`)

- 카카오 로컬 API 주소 검색 (`wizard-kakao-address.js`)
- 수령인 → 연락처 → 주소정보 UI
- 우편번호·시도·구·기본주소: 검색으로만 입력 (readonly)
- 상세주소만 직접 입력
- 주소 선택 시 카카오맵 표시 (JavaScript 키)
- 국가 KR 고정

### 5.3 관리 기능

- **메뉴관리** (`/admin/menus`): `screen_list` CRUD, 사이드바 연동
- **공통코드** (`/admin/codes`): 코드 그룹·상세 관리, 콤보 로더

### 5.4 REST API 예시

| Method | Path | 설명 |
|--------|------|------|
| GET | `/api/kakao/local/search/address` | 카카오 주소 검색 |
| GET | `/api/screens` | 화면 목록 |
| GET | `/api/codes/...` | 공통코드 옵션 |

---

## 6. 프로젝트 구조 (요약)

```
spring-boot-app-fixed/
├── src/main/java/com/example/springbootapp/
│   ├── controller/       # MVC + REST + Auth
│   ├── service/          # 업무 로직
│   ├── mapper/           # MyBatis
│   ├── domain/           # 엔티티
│   ├── config/           # 시드, WebMvc
│   └── web/              # Advice, Interceptor
├── src/main/resources/
│   ├── templates/        # Thymeleaf (Falcon)
│   ├── static/           # assets, vendors (Bootstrap)
│   ├── mapper/           # SQL XML
│   └── application*.properties
├── scripts/              # MySQL 초기화 SQL
├── run-server.bat        # 로컬 서버 재시작
└── pom.xml               # Maven
```

---

## 7. Git

- Branch: `main`
- 최근 기능 커밋: `feat: 마법사 카카오 주소검색·지도 및 JavaScript 키 분리` (c735049)
- 비밀키 파일은 `.gitignore` 처리 (`application-local.properties`)

---

*문서 생성: spring-boot-app-fixed 프로젝트 기준*
