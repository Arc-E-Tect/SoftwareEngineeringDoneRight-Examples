# System Testing with PostgreSQL and SSL

## Overview

System tests for Family Ties run the complete application with real infrastructure:
- **PostgreSQL database** (via Testcontainers) - not H2
- **HTTPS with SSL** - using self-signed certificates
- **WireMock stubs** for external service dependencies
- **Full Spring Boot application context**

System tests verify the application works correctly with production-like infrastructure while keeping external dependencies mocked.

## Test Pipeline Position

System tests are part of the quality gate pipeline:

1. ✅ `testComponent` - Unit tests with mocked dependencies
2. ✅ `testContract` + `testSystem` - Run in parallel after component tests
3. ✅ `testE2E` - Full containerized stack (runs only if system tests pass)

## Architecture

### Real System Tests (`testSystem`)
- **Database**: PostgreSQL 17.4 via Testcontainers
- **Transport**: HTTPS with self-signed SSL certificate
- **External Services**: WireMock stubs (optional, via Testcontainers)
- **Application**: Embedded Spring Boot on random port

### WireMock Variant Tests (`testSystemWiremock`)  
- Same as real system tests, but with WireMock running in Docker
- Tests external service integration contracts
- Useful for testing failover and error scenarios

## Prerequisites

### Required
- Docker running locally
- Java 21
- Gradle 9.3+
- Environment-only secrets exported in shell

### Environment Variables

System tests require:
- `SSL_KEYSTORE_PASSWORD` - Password for SSL keystore (required)
- `POSTGRES_PASSWORD` - Password for PostgreSQL (required)
- `H2_PASSWORD` - Password for H2 test profile use (required)
- `POSTGRES_DB` - Database name (default: `familyties`)
- `POSTGRES_USER` - Database user (default: `familyties`)

### Secret Handling Policy

Secrets are environment-only and must **not** be stored in `.env` files:

- `SSL_KEYSTORE_PASSWORD`
- `POSTGRES_PASSWORD`
- `H2_PASSWORD`

Non-secret values can be stored in synchronized `.env` files maintained by `setup-test-env.sh`.

### Setting Up Environment Variables

#### Option 1: Run setup script (Recommended)
```bash
export SSL_KEYSTORE_PASSWORD='your-secure-password'
export POSTGRES_PASSWORD='postgres-password'
export H2_PASSWORD='h2-password'

./setup-test-env.sh
```

This will:
- Prompt for non-secret values when needed
- Create/synchronize non-secret `.env` files
- Generate SSL keystores if they don't exist

#### Option 2: Manual setup
```bash
# Export environment variables
export SSL_KEYSTORE_PASSWORD='your-secure-password'
export POSTGRES_PASSWORD='postgres-password'
export H2_PASSWORD='h2-password'

# Generate keystores
./generate-test-keystore.sh
```

## Running System Tests

Before running, ensure required environment-only secrets are exported:

```bash
export SSL_KEYSTORE_PASSWORD='your-secure-password'
export POSTGRES_PASSWORD='postgres-password'
export H2_PASSWORD='h2-password'
```

### Environment Preflight

Use the preflight task to validate environment and policy before running system suites:

```bash
./gradlew :app:validateSystemEnvironment
```

The task verifies:
- Required environment-only secrets are present: `SSL_KEYSTORE_PASSWORD`, `POSTGRES_PASSWORD`, `H2_PASSWORD`
- Secrets are not persisted in root `.env`
- Required non-secret values are available: `POSTGRES_DB`, `POSTGRES_USER`, `SERVER_PORT`
- System keystore exists: `app/src/testSystem/resources/ssl/keystore.p12`

`testSystem`, `testSystemWiremock`, and `testContractWiremock` depend on this task automatically.

### Quick Start
```bash
# Run all system tests (with PostgreSQL and SSL)
./gradlew testSystem

# Run with verbose output
./gradlew testSystem --info

# Run specific test scenario
./gradlew testSystem -Dcucumber.filter.tags="@SunnyFlow"
```

### With WireMock Stubs
```bash
# Run system tests with WireMock container
./gradlew testSystemWiremock

# Both variants in parallel
./gradlew testSystem testSystemWiremock --parallel
```

### CI/CD Mode
```bash
# Same command used in GitHub Actions
./gradlew testSystem testContract --parallel --no-daemon
```

## SSL Keystore

### Automatic Generation
The system tests require an SSL keystore at:
```
app/src/testSystem/resources/ssl/keystore.p12
```

Generate it using:
```bash
./generate-test-keystore.sh
```

This creates a self-signed certificate valid for 10 years with:
- **Alias**: `familyties`
- **DN**: `CN=localhost, OU=Family Ties, O=Arc-E-Tect, L=Amsterdam, S=NH, C=NL`
- **SAN**: `dns:localhost,ip:127.0.0.1`
- **Key Algorithm**: RSA 2048-bit
- **Validity**: 3650 days

### Manual Generation
```bash
keytool -genkeypair \
  -alias familyties \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore app/src/testSystem/resources/ssl/keystore.p12 \
  -storepass "$SSL_KEYSTORE_PASSWORD" \
  -keypass "$SSL_KEYSTORE_PASSWORD" \
  -validity 3650 \
  -dname "CN=localhost, OU=Family Ties, O=Arc-E-Tect, L=Amsterdam, S=NH, C=NL" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
```

## Database Configuration

### Testcontainers JDBC URL
System tests use Testcontainers' special JDBC URL:
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:17.4-alpine:///familyties?TC_DAEMON=true
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
```

This automatically:
- Starts a PostgreSQL 17.4 Docker container
- Creates the `familyties` database
- Configures connection pooling
- Reuses containers across tests (TC_DAEMON=true)
- Cleans up after test suite completes

### Schema Management
- **DDL Mode**: `create-drop`
- **Dialect**: PostgreSQL
- Schema is created before each test suite and dropped after

## Test Output

### Reports Generated
- **Cucumber HTML**: `app/build/reports/cucumber/testSystem/`
- **Cucumber JSON**: `app/build/reports/cucumber/testSystem/cucumber.json`
- **JUnit XML**: `app/build/test-results/testSystem/`
- **Logs**: Console output with test progress

### Viewing Reports
```bash
# Open HTML report
open app/build/reports/cucumber/testSystem/cucumber-html-reports/overview-features.html

# View JUnit results
cat app/build/test-results/testSystem/TEST-*.xml
```

## Debugging

### View Testcontainers Logs
```bash
# Enable debug logging
./gradlew testSystem --debug

# Or set in application-system.yml:
logging:
  level:
    org.testcontainers: DEBUG
```

### Connect to PostgreSQL Container
While tests are running, find the container:
```bash
docker ps | grep postgres

# Connect with psql
docker exec -it <container-id> psql -U familyties -d familyties
```

### SSL Verification
Test HTTPS connectivity:
```bash
# Get the random port from test output, e.g., 54321
PORT=54321

curl -k https://localhost:$PORT/actuator/health

# Verify certificate
openssl s_client -connect localhost:$PORT -showcerts
```

### Common Issues

#### Issue: "SSL_KEYSTORE_PASSWORD not set"
```bash
export SSL_KEYSTORE_PASSWORD='your-password'
export POSTGRES_PASSWORD='postgres-password'
export H2_PASSWORD='h2-password'
./generate-test-keystore.sh
```

#### Issue: "Keystore was tampered with, or password was incorrect"
```bash
# Delete old keystore
rm app/src/testSystem/resources/ssl/keystore.p12

# Regenerate with correct password
export SSL_KEYSTORE_PASSWORD='correct-password'
./generate-test-keystore.sh
```

#### Issue: "Docker not running"
```bash
# Start Docker Desktop or Docker daemon
# Verify Docker is running:
docker ps
```

#### Issue: "Port already in use"
The tests use `RANDOM_PORT`, but if you see this error:
```bash
# Find and kill process using the port
lsof -ti:8080 | xargs kill -9
```

## Configuration Files

### Test Configuration
- `app/src/testSystem/resources/application-system.yml` - Spring Boot config
- `app/src/testSystem/resources/features/*.feature` - Cucumber scenarios
- `CucumberSpringConfiguration.java` - Test context setup
- `SystemStepDefinitions.java` - Step implementations

### SSL Configuration
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:ssl/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: familyties
```

## IDE Setup

### IntelliJ IDEA
1. Open Run/Debug Configurations
2. Add new "Cucumber Java" configuration
3. Set main class: `io.cucumber.core.cli.Main`
4. Set glue: `com.arc_e_tect.book.sedr.familyties.cucumber`
5. Set feature or folder path: `app/src/testSystem/resources/features`
6. Add environment variables:
   ```
   SSL_KEYSTORE_PASSWORD=your-password
   POSTGRES_PASSWORD=your-password
  H2_PASSWORD=your-password
   ```
7. Set working directory: `family-ties/app`

### VS Code
Add to `.vscode/launch.json`:
```json
{
  "type": "java",
  "name": "System Tests",
  "request": "launch",
  "mainClass": "io.cucumber.core.cli.Main",
  "args": [
    "--glue", "com.arc_e_tect.book.sedr.familyties.cucumber",
    "--plugin", "pretty",
    "--plugin", "html:build/reports/cucumber/testSystem",
    "src/testSystem/resources/features"
  ],
  "env": {
    "SSL_KEYSTORE_PASSWORD": "your-password",
    "POSTGRES_PASSWORD": "your-password",
    "H2_PASSWORD": "your-password",
    "SPRING_PROFILES_ACTIVE": "system"
  },
  "cwd": "${workspaceFolder}/family-ties/app"
}
```

## Differences from E2E Tests

| Aspect | System Tests | E2E Tests |
|--------|--------------|-----------|
| **Database** | PostgreSQL (Testcontainers) | PostgreSQL (Docker Compose) |
| **Application** | Embedded Spring Boot | Containerized JAR |
| **External Services** | WireMock stubs | Real or mocked services |
| **Network** | In-process HTTPS | Docker network |
| **Purpose** | Integration testing | Deployment verification |
| **Speed** | Fast (~30s) | Slower (~2min) |
| **Scope** | Single system | Full stack |

## Best Practices

1. **Always use SSL** - System tests must mirror production security
2. **Use PostgreSQL** - Never use H2 for system tests
3. **Generate keystores before first run** - Run `./generate-test-keystore.sh`
4. **Keep passwords secure** - Export secrets in environment variables; keep `.env` non-secret only
5. **Run in parallel with contract tests** - `./gradlew testSystem testContract --parallel`
6. **Check reports after failures** - HTML reports show exact failure points
7. **Use tags for selective testing** - `@SunnyFlow`, `@CloudyFlow`, `@StormyFlow`

## Security Notes

- **Keystore passwords** are never committed to Git
- **Self-signed certificates** are for testing only
- **`SSL_KEYSTORE_PASSWORD`, `POSTGRES_PASSWORD`, and `H2_PASSWORD` are environment-only**
- **`.env` files must contain non-secret values only**
- **Test keystores** are excluded from version control

## See Also

- [E2E Testing Documentation](E2E_TESTING.md) - Full containerized stack testing
- [GitHub Actions Workflow](.github/workflows/family-ties-build.yml) - CI/CD configuration
- [Setup Script](setup-test-env.sh) - Environment setup automation
