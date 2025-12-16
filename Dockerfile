# Multi-stage build for groups-service

FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle
RUN ./gradlew --no-daemon dependencies > /dev/null

COPY src /app/src
RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:17-jre AS runner
WORKDIR /app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport"
EXPOSE 8080

COPY --from=builder /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
