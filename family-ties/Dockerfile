# Multi-stage build for Family Ties application
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

# For E2E testing: Replace main keystore with testE2E keystore that uses SSL_KEYSTORE_PASSWORD
COPY app/src/testE2E/resources/ssl/keystore.p12 ./app/src/main/resources/keystore.p12

# Build the application (skip tests as they'll run separately)
# Dependencies are already cached from previous step
RUN ./gradlew :app:clean :app:bootJar -x test --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd -r familyties && useradd -r -g familyties familyties

# Copy the built jar from builder stage
COPY --from=builder /workspace/app/build/libs/familyTies-*.jar app.jar

# Change ownership
RUN chown -R familyties:familyties /app

# Switch to non-root user
USER familyties

# Expose port
EXPOSE 8080 8443

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f -k https://localhost:8443/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
