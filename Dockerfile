# ── Build Stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Install Maven CLI
RUN apk add --no-cache maven

# Copy POM and fetch dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source and build the jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Runtime Stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Grab the fat JAR
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx256m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-XX:+UseStringDeduplication", \
    "-XX:+OptimizeStringConcat", \
    "-XX:MaxMetaspaceSize=128m", \
    "-XX:CompressedClassSpaceSize=64m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
