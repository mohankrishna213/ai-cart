# ── Build Stage ───────────────────────────────────────────────────────────────
FROM maven:3.9.2-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy only the pom.xml first to leverage Docker cache for dependency download
COPY pom.xml ./

# Download dependencies without building
RUN mvn dependency:go-offline -B

# Copy source code and package the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Runtime Stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the fat JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]
