FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# Gradle cache reuse
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app

# Create non-root user
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=builder /workspace/build/libs/java-0.0.1-SNAPSHOT.jar app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
