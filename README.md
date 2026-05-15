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
