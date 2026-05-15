# End-to-End Tests (Cucumber)

E2E tests run the application in a Docker container with PostgreSQL using TLS, exercising full end-to-end flows with production-like deployment.

**Architecture**: The entire application stack (containerized app + PostgreSQL) is deployed via Docker Compose. This simulates production deployment.

For comprehensive E2E test documentation, see [E2E_TESTING.md](../../E2E_TESTING.md).

## Quick Start

Prerequisites:
- Docker running locally
- SSL keystore generated (automatically created by `setup-test-env.sh`)
- Environment variable `SSL_KEYSTORE_PASSWORD` set

Run:
```bash
./gradlew testE2E --no-daemon --no-build-cache --info
```

The `testE2E` task automatically:
- Builds the application Docker image
- Starts the Docker Compose stack (app container + PostgreSQL)
- Runs all E2E test scenarios
- Stops and cleans up the Docker Compose stack (even if tests fail)

## Outputs
- Cucumber HTML/JSON: `app/build/reports/cucumber/testE2E/`
- JUnit XML: `app/build/test-results/testE2E/`

## Configuration

- **Docker Compose**: Uses `docker-compose.e2e.yml` with PostgreSQL 18-alpine
- **Application**: Runs in Docker container built from `Dockerfile`
- **SSL Keystores**: Located in `app/src/testE2E/resources/ssl/`
- **Environment Variables**: Loaded from `app/src/testE2E/resources/.env`

## Differences from System Tests

| Aspect | System Tests | E2E Tests |
|--------|-------------|-----------|
| Application | Embedded Spring Boot | Containerized via Docker |
| Database | PostgreSQL via Testcontainers | PostgreSQL via Docker Compose |
| Infrastructure | Managed by Testcontainers | Managed by Docker Compose |
| Purpose | Integration testing | Production deployment simulation |

See [SYSTEM_TESTING.md](../../SYSTEM_TESTING.md) for system test documentation.
