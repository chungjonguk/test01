# =========================================================================
# 1단계: Maven 빌드 (Java 17)
# =========================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# 의존성 캐시 최적화: pom.xml 먼저 복사 후 의존성 미리 내려받기
# (m2e lifecycle-mapping 등으로 go-offline이 실패해도 빌드는 계속 — package에서 보완)
COPY pom.xml .
RUN mvn -B dependency:go-offline || true

# 소스 복사 후 패키징 (테스트 제외)
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# =========================================================================
# 2단계: 실행 (JRE only, 경량 이미지)
# =========================================================================
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# 타임존(KST)
ENV TZ=Asia/Seoul

# 빌드 산출물 jar 복사 (SNAPSHOT 등 버전 무관하게 매칭)
COPY --from=build /build/target/*.jar /app/app.jar

# 로그 / NAS 저장소 디렉터리 (compose에서 볼륨 마운트)
RUN mkdir -p /app/logs /data/nas-storage/printmall

EXPOSE 8081

# docker 프로필 + UTF-8/한국어 로케일
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Duser.language=ko -Duser.country=KR"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
