# System Tests (Cucumber)

System tests run the Spring Boot application with **PostgreSQL via Testcontainers** and **SSL enabled** to verify integration in a production-like environment.

## Quick Start

```bash
# Ensure environment variables are set
export SSL_KEYSTORE_PASSWORD='your-password'
export POSTGRES_PASSWORD='your-password'

# Run system tests
./gradlew testSystem
```

## Architecture

- **Database**: PostgreSQL 17.4-alpine via Testcontainers (automatically managed)
- **Transport**: HTTPS with self-signed SSL certificate
- **Application**: Embedded Spring Boot on random port
- **External Services**: WireMock stubs (via Testcontainers, when needed)

## Prerequisites

1. **Docker** running locally (required for Testcontainers)
2. **SSL keystore** at `app/src/testSystem/resources/ssl/keystore.p12`
   - Generated automatically by `./setup-test-env.sh`
   - Or manually via `./generate-test-keystore.sh`
3. **Environment variables**:
   - `SSL_KEYSTORE_PASSWORD` - Password for SSL keystore (required)
   - `POSTGRES_PASSWORD` - Password for PostgreSQL (required)

## Run Commands

```bash
# Standard run (recommended)
./gradlew testSystem

# With detailed output
./gradlew testSystem --info

# Clean run
./gradlew clean testSystem --no-daemon
```

## Test Outputs

- **Cucumber HTML**: `app/build/reports/cucumber/testSystem/cucumber-html-reports/`
- **Cucumber JSON**: `app/build/reports/cucumber/testSystem/cucumber.json`
- **JUnit XML**: `app/build/test-results/testSystem/`
- **Test Report**: `app/build/reports/tests/testSystem/index.html`

## WireMock Variant

Run system tests against WireMock stubs instead of the real application:

```bash
./gradlew testSystemWiremock
```

This variant:
- Starts WireMock in a Docker container
- Tests external service integration contracts
- Useful for testing error scenarios and failover

## Configuration

System tests use the `test` Spring profile defined in:
- `app/src/testSystem/resources/application-system.yml`

Key configuration:
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:17.4-alpine:///familyties?TC_DAEMON=true
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
server:
  ssl:
    enabled: true
    key-store: classpath:ssl/keystore.p12
```

## Important Notes

- **Testcontainers automatically manages PostgreSQL**: No manual container setup required
- **SSL is mandatory**: Tests verify HTTPS enforcement (HTTP requests are rejected)
- **Database schema is created/dropped**: Each test run starts with a clean database
- **Thread-safe execution**: Uses `@DirtiesContext` to isolate test scenarios
- **No WireMock by default**: System tests run against the embedded application

📖 **For comprehensive documentation**, see:
- [SYSTEM_TESTING.md](../../SYSTEM_TESTING.md) - Complete system testing guide
- [E2E_TESTING.md](../../E2E_TESTING.md) - End-to-end testing with Docker Compose

