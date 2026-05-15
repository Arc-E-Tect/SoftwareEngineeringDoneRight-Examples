# Family Ties

Spring Boot 4 microservice for managing family members and relationships following hexagonal architecture.

## Prerequisites

### Environment Variables (Required)

This project follows a **zero-credentials policy**. No secrets are stored in git, including test credentials and SSL keystores.

Before running tests, set up environment variables and generate keystores:

```bash
# Option 1: Use the setup script (recommended - generates keystores automatically)
./setup-test-env.sh

# Option 2: Use environment variables with the script
export SSL_KEYSTORE_PASSWORD="your-ssl-password"  # Keystores will be generated with this password
export H2_PASSWORD="your-strong-h2-password"
export POSTGRES_PASSWORD="your-strong-postgres-password"
./setup-test-env.sh

# Option 3: Export variables directly and generate keystores manually
export H2_USERNAME=sa
export H2_PASSWORD="your-strong-password"
export POSTGRES_DB=familyties
export POSTGRES_USER=familyties
export POSTGRES_PASSWORD="your-strong-password"
export SSL_KEYSTORE_PASSWORD="your-ssl-password"
# Then manually generate keystores (see ZERO_CREDENTIALS_POLICY.md)
```

The setup script will:
- Use environment variables if already set (especially `SSL_KEYSTORE_PASSWORD`)
- Prompt for values if not set (with secure password prompts)
- Generate strong random passwords for all credentials if you press Enter
- **Generate SSL keystores** (`keystore.p12`) with your chosen password
- Create synchronized non-secret `.env` files at project root and `app/src/testE2E/resources/.env`
- Keep `SSL_KEYSTORE_PASSWORD` environment-only (never persisted in `.env`)

**IMPORTANT**: All passwords can be freely chosen. The keystores are generated locally with your chosen `SSL_KEYSTORE_PASSWORD`.

The `.env` file and `keystore.p12` files are gitignored and will never be committed.

## Build & Test

```bash
# Fast feedback - runs unit, component, and architecture tests (~30 seconds)
./gradlew check

# Full build with integration tests
./gradlew build

# Complete QA pipeline - runs all tests including WireMock verification, contract, system, and E2E
# Note: QA task automatically disables configuration cache to ensure proper Docker container lifecycle
# For a completely clean QA run (recommended for final verification):
./gradlew clean QA --no-daemon  # ~55-60 seconds

# Or just run QA (uses incremental builds with daemon):
./gradlew QA  # ~50-55 seconds

# Run E2E tests only (independent from testContract/testSystem)
./gradlew testE2E
```

**Test Pipeline:**
- `check` runs fast tests only (unit + component + architecture)
- `QA` runs the complete verification pipeline: build → WireMock verification → contract tests → system tests → E2E tests
  - **Configuration cache is automatically disabled** for QA task to ensure proper Docker container lifecycle
  - Ensures sequential execution: `testContractWiremock` → `testSystemWiremock` → `composeDown` → `testContract` → `testSystem` → `testE2E`
  - Consistent performance: ~50-60 seconds regardless of clean/incremental builds
  - WireMock containers (port 8443) are stopped before E2E tests start their application container
- `testSystem` and `testContract` depend on `testComponent` (run in parallel)
- `testE2E` runs independently and validates E2E env consistency before startup
- Individual test tasks can be run independently - Docker Compose plugin automatically manages container cleanup

**Test Suites:**
- `test` - Unit tests (JUnit, fast feedback)
- `testComponent` - Component tests (Spring context, H2 in-memory, no SSL)
- `testContract` - API contract tests (HTTP, mocked dependencies, Spring REST Docs)
- `testContractWiremock` - Contract tests against WireMock in Docker
- `testSystem` - System integration tests (PostgreSQL via Testcontainers, SSL, embedded app)
- `testSystemWiremock` - System tests against WireMock stubs in Docker
- `testArchitecture` - Architecture compliance tests (ArchUnit)
- `testE2E` - End-to-end tests (full Docker Compose stack with containerized app)

Test suites are defined via `testing { suites { ... } }` in [app/build.gradle](app/build.gradle).

**Note**: The `testE2E` suite is **intentionally excluded** from the standard build to keep builds fast and infrastructure-independent. E2E tests require Docker Compose to be running.

📖 **For detailed testing documentation:**
- [SYSTEM_TESTING.md](SYSTEM_TESTING.md) - System integration tests with PostgreSQL and SSL
- [E2E_TESTING.md](E2E_TESTING.md) - End-to-end tests with Docker Compose

### Running E2E Tests

E2E tests validate the complete application in a production-like environment by:
1. Running the containerized Family Ties application (via Docker Compose)
2. Connecting external REST clients to test the HTTPS endpoints
3. Using a real PostgreSQL database (not mocked)

#### Quick Start (Recommended)

The testE2E task automatically manages the Docker Compose lifecycle:

```bash
./gradlew testE2E
```

This single command automatically:
- Validates E2E environment consistency (`validateE2EEnvironment`)
- Starts the Docker Compose stack (postgres + containerized app)
- Loads non-secret environment variables from `app/src/testE2E/resources/.env`
- Uses `SSL_KEYSTORE_PASSWORD` from your shell environment
- Runs all 26 E2E test scenarios
- Stops and cleans up the Docker Compose stack (even if tests fail)

The automatic lifecycle management is configured in [app/build.gradle](app/build.gradle) using:
- `dependsOn tasks.named('e2eComposeUp')` - starts stack before tests
- `finalizedBy tasks.named('e2eComposeDown')` - cleanup after tests complete

#### Manual Workflow

For debugging or step-by-step execution:

```bash
# 1. Start the containerized application stack (keeps it running)
./gradlew e2eComposeUp

# 2. View application logs
./gradlew e2eComposeLogs

# 3. Load environment variables and run tests
export SSL_KEYSTORE_PASSWORD="your-ssl-password"
export $(grep -v '^#' app/src/testE2E/resources/.env | xargs)
./gradlew testE2E --no-daemon

# 4. Stop the stack when done
./gradlew e2eComposeDown
```

#### What's Running?

The E2E environment includes:
- **postgres:18-alpine** - PostgreSQL database on port 5432
- **familyties/app:e2e** - Containerized Family Ties application on port 8443 (HTTPS)
- Both containers linked via Docker Compose network
- Tests run **outside** containers as REST clients (similar to production usage)

#### Environment Variables

Tests require these variables:
- From shell environment: `SSL_KEYSTORE_PASSWORD`
- From `app/src/testE2E/resources/.env`: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `SERVER_PORT`

Run `./setup-test-env.sh` to generate the `.env` file if it doesn't exist.

## Security expectations

- HTTPS is mandatory; the Cucumber security suites ([family-ties/app/src/testSystem/resources/features/security_policy.feature](family-ties/app/src/testSystem/resources/features/security_policy.feature) and [family-ties/app/src/testE2E/resources/features/security_policy.feature](family-ties/app/src/testE2E/resources/features/security_policy.feature)) assert that HTTP requests to person and relationship endpoints are rejected.

## Run locally

```bash
./gradlew bootRun
```

or with containers:

```bash
docker-compose up --build
```

Service listens on `https://localhost:8080` for versioned endpoints under `/v1`.

## API Contract

See [app/src/main/resources/openapi_familyties.yaml](app/src/main/resources/openapi_familyties.yaml).

## Docker

- `docker-compose.yml` starts app, PostgreSQL 18 Alpine, and WireMock (TLS-ready)
  - **PostgreSQL 18-alpine**: Smaller image size, full ARM64/Apple Silicon support
  - **WireMock 3.13.2-2**: Standard image (Alpine variant not available for ARM64)
- `SSL_KEYSTORE_PASSWORD` is reused for keystore and Postgres; set it via environment or a local `.env` (keep `.env` out of version control)
- WireMock mounts stubs from `app/src/main/resources/wiremock` with response templating enabled; start it alone with `docker-compose up wiremock` to serve the mocked API contract on ports 8089 (HTTP) and 8443 (HTTPS)
- Docker Compose plugin configuration ensures automatic container cleanup:
  - `stopContainers = true` - stops containers after tests complete
  - `removeContainers = true` - removes stopped containers
  - Individual test tasks (e.g., `testContractWiremock`, `testSystemWiremock`) automatically clean up their containers
- `docker-compose.prebuilt.yml` is provided for **offline or firewalled environments** where Docker cannot run Gradle during the build. Pre-build the jar locally first, then Docker skips the Gradle step entirely:
  ```bash
  # 1. Build the jar locally (requires JDK 21)
  ./gradlew :app:bootJar -x test --no-daemon

  # 2. Start the stack using the pre-built jar
  docker compose -f docker-compose.prebuilt.yml up --build
  ```
  Docker will fail immediately with a `COPY` error if the jar is missing — this is intentional (fail fast).
  > **Note**: The jar bundles whatever keystore is at `app/src/main/resources/keystore.p12` at build time. For E2E testing with SSL, ensure the testE2E keystore is in place before running `bootJar`.

## Run system/e2e suites against WireMock only

Use this flow to validate the WireMock expectations against the OpenAPI contract while the app is still under development.

For comprehensive system test documentation including real PostgreSQL integration, see [SYSTEM_TESTING.md](SYSTEM_TESTING.md).

1. Start only the WireMock container (TLS on 8443 uses the same `SSL_KEYSTORE_PASSWORD` you provide):
	```bash
	docker-compose up wiremock
	```
2. Point the Cucumber suites at WireMock by overriding the base URL (either env var or `-Dfamilyties.base-url`):
	- zsh/bash (macOS/Linux):
		```bash
		FAMILYTIES_BASE_URL=https://localhost:8443/v1/familyties ./gradlew testSystem --no-daemon --no-build-cache --info
		FAMILYTIES_BASE_URL=https://localhost:8443/v1/familyties ./gradlew testE2E --no-daemon --no-build-cache --info
		```
	- PowerShell (Windows/macOS):
		```powershell
		$env:FAMILYTIES_BASE_URL="https://localhost:8443/v1/familyties"; ./gradlew testSystem --no-daemon --no-build-cache --info
		$env:FAMILYTIES_BASE_URL="https://localhost:8443/v1/familyties"; ./gradlew testE2E --no-daemon --no-build-cache --info
		```
3. Expected result: both suites should pass just like they do against the real service. If they fail, either the WireMock stubs in [app/src/main/resources/wiremock](app/src/main/resources/wiremock) or the implementation behind [app/src/main/resources/openapi_familyties.yaml](app/src/main/resources/openapi_familyties.yaml) has drifted—fix whichever side is out of sync.

## Semantic Release

CI is configured to run semantic-release on `main` using conventional commits with `release.config.js`.
