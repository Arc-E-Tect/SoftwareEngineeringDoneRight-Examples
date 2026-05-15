# Multi-stage build for Vanilla MFA application
# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy gradle wrapper and build files first (these change rarely)
COPY gradlew ./
COPY gradle/ ./gradle/
COPY settings.gradle ./
COPY build.gradle ./

# Download and cache dependencies in a separate layer
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

# Now copy the source code (changes frequently)
COPY src ./src/

# Build the application (skip tests as they run separately)
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd -r vanilla-mfa && useradd -r -g vanilla-mfa vanilla-mfa

# Copy the built jar from builder stage
COPY --from=builder /workspace/build/libs/vanilla-mfa-*.jar app.jar

# Change ownership
RUN chown -R vanilla-mfa:vanilla-mfa /app

# Switch to non-root user
USER vanilla-mfa

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application with the vanilla profile
ENTRYPOINT ["java", "-Dspring.profiles.active=vanilla", "-jar", "/app/app.jar"]
