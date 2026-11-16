# syntax=docker/dockerfile:1

# ---------- 1단계: 빌드 (JDK + Gradle) ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# 의존성 정의 파일만 먼저 복사 → 소스만 바뀌면 의존성 다운로드 레이어를 재사용한다
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon > /dev/null

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar -x test --no-daemon \
    && cp build/libs/*-SNAPSHOT.jar app.jar

# ---------- 2단계: 실행 (JRE 만 포함해 이미지 크기 최소화) ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# root 가 아닌 전용 사용자로 실행 (컨테이너 탈취 시 피해 최소화)
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
