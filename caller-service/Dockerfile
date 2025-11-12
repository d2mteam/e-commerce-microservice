# Multi-stage build for caller-service
FROM gradle:8.7-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8020
ENTRYPOINT ["java","-jar","/app/app.jar"]
