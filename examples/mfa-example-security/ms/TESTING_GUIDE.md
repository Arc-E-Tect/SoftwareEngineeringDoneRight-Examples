# Family Ties Testing Guide

This guide provides a comprehensive overview of the Family Ties testing strategy and how to run the different test suites.

**For detailed documentation:**
- [SYSTEM_TESTING.md](SYSTEM_TESTING.md) - Comprehensive guide for system integration tests (PostgreSQL + SSL via Testcontainers)
- [E2E_TESTING.md](E2E_TESTING.md) - End-to-end testing with Docker Compose

## Environment Bootstrap

System and E2E tests require SSL keystores and `.env` files to be in place before they can run.

### Using bootstrapProject.ps1 (Recommended for Automation)

`bootstrapProject.ps1` is the preferred automation entry point. It sets up all keystores and `.env` files non-interactively by pre-setting all required environment variables and delegating to `setup-test-env.sh`.

```powershell
# Requires: SSL_KEYSTORE_PASSWORD set in the process environment
$env:SSL_KEYSTORE_PASSWORD = '<your-password>'
pwsh -NonInteractive -NoProfile -File ./bootstrapProject.ps1
```

The script:
1. Removes any stale `.env` and `keystore.p12` files (always writes fresh)
2. Pre-sets non-secret defaults (`H2_USERNAME=sa`, `POSTGRES_DB=familyties`, `POSTGRES_USER=familyties`, `SERVER_PORT=8443`) and random throwaway passwords for `H2_PASSWORD` and `POSTGRES_PASSWORD` when not supplied
3. Calls `setup-test-env.sh` non-interactively (prompts are suppressed by pre-set env vars)
4. Exits 0 on success, non-zero on failure

The Tools-repo dependency updater calls `bootstrapProject.ps1` automatically — no manual action needed during dependency update runs.

### Using setup-test-env.sh Directly

For local human setup, run `setup-test-env.sh` directly and respond to the prompts:

```bash
export SSL_KEYSTORE_PASSWORD='<choose-a-local-secret>'
./setup-test-env.sh
```

For non-interactive usage (CI pipelines, scripts), pre-set all env vars to suppress prompts:

```bash
SSL_KEYSTORE_PASSWORD='...' \
POSTGRES_PASSWORD='...' \
H2_PASSWORD='...' \
H2_USERNAME='sa' \
POSTGRES_DB='familyties' \
POSTGRES_USER='familyties' \
SERVER_PORT='8443' \
./setup-test-env.sh
```

## Test Suites Overview

Family Ties uses Gradle's testing suites DSL to organize tests into multiple categories:

| Suite | Purpose | Infrastructure | Included in `./gradlew check` |
|-------|---------|----------------|-------------------------------|
| **test** | Unit tests | None | ✅ Yes |
| **testComponent** | Component tests | H2 in-memory database | ✅ Yes |
| **testArchitecture** | Architecture rules | None (ArchUnit) | ✅ Yes |
| **testContract** | API contract tests | Mocked dependencies | ❌ No - Depends on testComponent |
| **testContractWiremock** | Contract tests against WireMock | WireMock in Docker | ❌ No - Depends on testComponent |
| **testSystem** | System integration tests | PostgreSQL (Testcontainers) + SSL | ❌ No - Depends on testComponent |
| **testSystemWiremock** | System tests against WireMock | WireMock in Docker | ❌ No - Depends on testComponent |
| **testE2E** | End-to-end tests | PostgreSQL + Containerized app | ❌ No - Independent |

**Test Dependency Chain:**
1. `check` runs fast tests: `test`, `testComponent`, `testArchitecture` (~30 seconds)
2. `testContract` and `testSystem` run in parallel (both depend on testComponent)
3. `testE2E` runs independently and validates E2E env consistency before startup

This pipeline ensures:
- ⚡ Fast developer feedback from `check` task
- 🚫 System tests don't run if component tests fail
- ✅ E2E failures isolate Docker/infrastructure issues without being blocked by other suites

## Running Tests

### Fast Feedback Loop (Recommended)

```bash
# Runs unit, component, and architecture tests only (~30 seconds)
./gradlew check
```

The `check` task is optimized for fast developer feedback and includes:
- Unit tests (`test`)
- Component tests (`testComponent`) 
- Architecture validation (`testArchitecture`)

### Complete QA Pipeline

```bash
# Full QA verification pipeline - runs all tests in correct order
# For a completely clean QA run (recommended for final verification):
./gradlew clean QA

# Or just run QA (uses incremental builds, faster):
./gradlew QA
```

The `QA` task runs the complete verification pipeline in this order:
1. **Build** - Compiles all code and runs fast tests (`check`)
2. **testContractWiremock** - Validates API contracts against WireMock stubs
3. **testSystemWiremock** - Verifies WireMock behavior matches real application
4. **composeDown** - Ensures WireMock containers are stopped (automatic via `finalizedBy`)
5. **testContract** - Contract tests against real application
6. **testSystem** - System tests against real application
7. **testE2E** - End-to-end tests with Docker Compose

**Why use `./gradlew clean QA`?**
- Ensures a completely fresh build state
- Verifies everything works from scratch (no cached artifacts)
- Recommended for final verification before releases or merge requests
- The clean is separate from QA to avoid conflicts with running tasks

**When to use `./gradlew QA` (without clean)?**
- During development for faster feedback
- When you know the build is already up-to-date
- For incremental verification after small changes

### Full Test Pipeline (Individual Tasks)

```bash
# Run integration tests (contract + system run in parallel)
./gradlew testSystem testContract --parallel

# Run E2E tests only (independent)
./gradlew testE2E

# Or run everything including E2E in one command
./gradlew check testE2E
```

### WireMock Stub Verification

```bash
# Verify WireMock stubs match real application behavior
# Start WireMock via docker-compose
docker-compose up -d wiremock

# Run testSystem against WireMock instead of real app
FAMILYTIES_BASE_URL=https://localhost:8443/v1/familyties ./gradlew testSystem

# Stop WireMock
docker-compose down
```

**Why verify WireMock stubs?**
Client teams can develop against WireMock stubs while Family Ties is still being built. By running the same system tests against both WireMock and the real application, you ensure the stubs accurately represent real behavior. If tests pass against both, clients can trust the stubs.

**How it works:**
- `SystemStepDefinitions` checks for `FAMILYTIES_BASE_URL` environment variable
- When set, tests connect to that URL instead of the locally-started application
- Same test scenarios verify both WireMock stubs and real implementation
- This is consumer-driven contract testing in action!

### Individual Test Suites

```bash
# Unit tests only
./gradlew test

# Component tests only
./gradlew testComponent

# Contract tests only
./gradlew testContract

# Architecture tests only
./gradlew testArchitecture

# System tests only
./gradlew testSystem
```

### End-to-End Tests (Requires Docker)

E2E tests are **intentionally excluded** from the standard build because they require Docker Compose infrastructure.

#### Prerequisites

1. Ensure `.env` file exists with credentials:
   ```bash
   ./setup-test-env.sh
   ```

2. Docker Desktop or Docker Engine must be running

#### Running E2E Tests

The `testE2E` task **automatically manages the Docker Compose lifecycle**:

```bash
./gradlew testE2E
```

This single command automatically:
- Starts the Docker Compose stack (postgres + containerized app)
- Loads environment variables from `app/src/testE2E/resources/.env`
- Runs all 26 E2E test scenarios
- Stops and cleans up the Docker Compose stack (even if tests fail)

#### Manual Docker Compose Control (For Debugging)

If you need to manage the Docker Compose stack manually:

```bash
# Start the stack (keeps it running)
./gradlew e2eComposeUp

# View application logs
./gradlew e2eComposeLogs

# Run tests against running stack
export $(grep -v '^#' app/src/testE2E/resources/.env | xargs)
./gradlew testE2E --no-daemon

# Stop and cleanup stack when done
./gradlew e2eComposeDown
```

#### E2E Infrastructure

- **PostgreSQL**: Database container (`postgres:16-alpine`)
- **WireMock**: External API mocking (`wiremock/wiremock:4.0.0-beta-24`)
- **SSL/TLS**: Keystores from `app/src/testE2E/resources/ssl/`
- **Credentials**: From `.env` file or environment variables

## Test Configuration

### Cucumber BDD Tests

Both `testSystem` and `testE2E` use Cucumber for BDD scenarios:

- **Feature files**: Located in `src/testSystem/resources/features/` and `src/testE2E/resources/features/`
- **Step definitions**: `SystemStepDefinitions.java` and `E2EStepDefinitions.java`
- **Suite configuration**: Uses `@SelectPackages("features")` to discover feature files

### SSL/TLS Configuration

System and E2E tests use HTTPS with self-signed certificates:

- **System tests**: Keystores in `app/src/testSystem/resources/ssl/`
- **E2E tests**: Keystores in `app/src/testE2E/resources/ssl/`
- **Main application**: Keystore in `app/src/main/resources/`
- **Password**: Set via `SSL_KEYSTORE_PASSWORD` environment variable
- **Generation**: Use `./generate-test-keystore.sh` or `./setup-test-env.sh`

Component and contract tests use HTTP (no SSL) for faster execution.

For detailed SSL configuration, see [SYSTEM_TESTING.md](SYSTEM_TESTING.md#ssl-keystore).

### Database Configuration

- **Unit tests**: No database (pure Java tests)
- **Component/Contract/Architecture tests**: H2 in-memory database with defaults (no SSL)
- **System tests**: PostgreSQL 17.4-alpine via Testcontainers with SSL (production-like)
- **E2E tests**: PostgreSQL 18-alpine container via Docker Compose (production deployment simulation)

For detailed system test configuration, see [SYSTEM_TESTING.md](SYSTEM_TESTING.md).
For E2E test setup and infrastructure, see [E2E_TESTING.md](E2E_TESTING.md).

## Environment Variables

Tests use environment variables for credentials (no defaults in code):

```bash
# Required for system and E2E tests
SSL_KEYSTORE_PASSWORD=<your-password>

# Required for E2E tests only
POSTGRES_DB=familyties
POSTGRES_USER=familyties
POSTGRES_PASSWORD=<your-password>
```

**Note**: Component and contract tests use H2 with default in-memory configuration and don't require database credentials.

Use `./setup-test-env.sh` to generate these automatically.

## Zero Credentials Policy

**No secrets are stored in git**, including test credentials and SSL keystores.

- `.env` files: Gitignored, generated locally
- `keystore.p12` files: Gitignored, generated locally
- Pre-commit hooks: Block any credential commits

See [ZERO_CREDENTIALS_POLICY.md](ZERO_CREDENTIALS_POLICY.md) for complete policy details.

## Test Reports

After running tests, reports are available at:

```
build/reports/tests/test/              # Unit test report
build/reports/tests/testComponent/     # Component test report
build/reports/tests/testContract/      # Contract test report
build/reports/tests/testArchitecture/  # Architecture test report
build/reports/tests/testSystem/        # System test report
build/reports/tests/testE2E/           # E2E test report (if run)
build/reports/cucumber/testSystem/     # Cucumber HTML report (system)
build/reports/cucumber/testE2E/        # Cucumber HTML report (E2E, if run)
build/reports/jacoco/test/             # Code coverage report
```

## Troubleshooting

### Environment Variables Not Found

**Symptom**: Tests fail with `null` values for database credentials.

**Solution**: 
```bash
./setup-test-env.sh
source .env  # If needed
./gradlew clean build
```

### SSL Keystore Not Found

**Symptom**: Tests fail with "keystore.p12 not found" error.

**Solution**: Keystores are generated by `setup-test-env.sh`:
```bash
./setup-test-env.sh
./gradlew clean build
```

### Docker Not Running

**Symptom**: E2E tests fail with "Cannot connect to the Docker daemon".

**Solution**: Start Docker Desktop or Docker Engine:
```bash
# On macOS with Docker Desktop
open -a Docker

# Then run E2E tests (Docker Compose managed automatically)
./gradlew testE2E
```

### Persistent Environment Variable Conflicts

**Symptom**: `setup-test-env.sh` detects `SSL_KEYSTORE_PASSWORD` but keystore doesn't work.

**Solution**: Either unset the environment variable or regenerate keystores:
```bash
# Option 1: Use .env file only
unset SSL_KEYSTORE_PASSWORD
./setup-test-env.sh

# Option 2: Regenerate keystores with your persistent password
# (setup script will detect and use it)
./setup-test-env.sh
```

## CI/CD Considerations

For GitHub Actions or other CI/CD:

1. Set secrets in CI/CD environment (GitHub Secrets, etc.)
2. Generate keystores during CI build:
   ```bash
   export SSL_KEYSTORE_PASSWORD="${{ secrets.SSL_KEYSTORE_PASSWORD }}"
   export H2_PASSWORD="${{ secrets.H2_PASSWORD }}"
   export POSTGRES_PASSWORD="${{ secrets.POSTGRES_PASSWORD }}"
   ./setup-test-env.sh
   ./gradlew clean build  # Runs all tests except E2E
   ```

3. For E2E tests in CI:
   ```bash
   # testE2E automatically manages Docker Compose lifecycle
   ./gradlew testE2E
   ```

## Summary

✅ **Fast feedback**: `./gradlew check` runs fast tests in ~30 seconds
✅ **Complete QA pipeline**: `./gradlew clean QA` runs all verification tests in correct order
✅ **Comprehensive testing**: 6 test suites covering unit, component, contract, architecture, system, and E2E
✅ **WireMock verification**: Validates consumer-driven contracts against stubs
✅ **Zero credentials**: All secrets externalized, nothing in git
✅ **Flexible execution**: Run individual suites or full QA pipeline
✅ **Production-like E2E**: Docker Compose with PostgreSQL and automatic container cleanup
