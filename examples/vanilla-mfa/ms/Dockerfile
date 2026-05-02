# Multi-stage build for Vanilla MS application
# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy gradle wrapper and build files first (these change rarely)
COPY gradlew ./
COPY gradle ./gradle/
COPY settings.gradle ./
COPY gradle.properties ./
COPY versions.properties ./

# Copy only dependency-related files to leverage Docker cache for dependencies
COPY app/build.gradle ./app/
COPY app/lombok.config ./app/

# Download and cache dependencies in a separate layer
# This layer will be cached unless build files change
RUN chmod +x ./gradlew && ./gradlew :app:dependencies --no-daemon > /dev/null 2>&1 || true

# Now copy the source code (changes frequently)
COPY app/src ./app/src/

# Build the application (skip tests as they'll run separately)
# Dependencies are already cached from previous step
RUN ./gradlew :app:clean :app:bootJar -x test --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd -r vanilla-ms && useradd -r -g vanilla-ms vanilla-ms

# Copy the built jar from builder stage
COPY --from=builder /workspace/app/build/libs/familyTies-*.jar app.jar

# Change ownership
RUN chown -R vanilla-ms:vanilla-ms /app

# Switch to non-root user
USER vanilla-ms

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
