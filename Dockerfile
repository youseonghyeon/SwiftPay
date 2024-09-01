# 1. 빌드 단계
# JDK 21을 기반으로 하는 빌드 이미지 설정
FROM openjdk:21-jdk-slim AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper를 복사하고 실행 권한 부여
COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew

# Gradle 캐시를 활용하기 위해 먼저 build.gradle.kts와 settings.gradle.kts 복사
COPY build.gradle.kts settings.gradle.kts ./

# 종속성 다운로드 (gradle의 캐시를 활용)
RUN ./gradlew build -x test --no-daemon || return 0

# 모든 소스 파일 복사
COPY . .

# 프로젝트 빌드
RUN ./gradlew build -x test --no-daemon

# 2. 실행 단계
# 실행을 위한 경량 이미지 설정
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 빌드 단계에서 복사
COPY --from=build /app/build/libs/swift-pay-0.0.1-SNAPSHOT.jar /app/pay.jar

# 애플리케이션 실행 명령어
CMD ["java", "-jar", "/app/pay.jar"]

# 포트 설정
EXPOSE 8080
