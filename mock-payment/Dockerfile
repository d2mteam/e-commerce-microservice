# Stage 1: Cache Dependencies
FROM eclipse-temurin:17-jdk-alpine AS cache
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew ./
# Chỉ tải thư viện về, không build code
RUN ./gradlew dependencies --no-daemon

# Stage 2: Build Code
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY --from=cache /root/.gradle /root/.gradle
COPY . .
RUN ./gradlew clean build -x test --no-daemon

# Stage 3: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]