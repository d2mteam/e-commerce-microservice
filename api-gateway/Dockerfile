# syntax=docker/dockerfile:1

# Build stage: use JDK 21 and Gradle wrapper
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy wrapper and gradle files first for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .
# Ensure wrapper is executable and fix potential CRLF
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Copy rest of the source
COPY src src

# Build the boot jar (skip tests)
RUN ./gradlew --no-daemon clean bootJar -x test

# Runtime stage: JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
