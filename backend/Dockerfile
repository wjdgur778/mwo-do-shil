# 1단계: 빌드 스테이지 (Gradle 환경)
FROM gradle:8.4-jdk17-alpine AS build
WORKDIR /app
# 빌드 효율을 위해 설정 파일만 먼저 복사 후 의존성 캐싱
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle clean build -x test --no-daemon

# 2단계: 실행 스테이지 (가벼운 JRE 환경)
FROM openjdk:17-jdk-slim
WORKDIR /app
# 빌드 단계에서 생성된 jar 파일만 쏙 가져오기
COPY --from=build /app/build/libs/*.jar app.jar

# 보안을 위해 Root 권한이 아닌 별도 유저 사용 (선택 사항이나 권장)
RUN useradd -m myuser
USER myuser

EXPOSE 8080
# 컨테이너 실행 시 메모리 최적화 옵션 추가
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]