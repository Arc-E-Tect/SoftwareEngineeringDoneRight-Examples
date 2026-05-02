# End-to-End Testing (Containerized Stack)

## Purpose

`testE2E` validates the application in a production-like deployment model:

- Application runs in a container (`family-ties-app`)
- Database runs in a real PostgreSQL container (`postgres`)
- Tests run as external HTTPS clients against the running stack

This is intentionally different from system tests, which may use Testcontainers for convenience.

## What `./gradlew testE2E` Does

Running `./gradlew testE2E` performs the following automatically:

1. Validates E2E environment prerequisites (`validateE2EEnvironment`)
2. Starts Docker Compose infrastructure via Gradle dockerCompose plugin (`e2eComposeUp`)
3. Runs E2E scenarios
4. Stops and cleans up stack (`e2eComposeDown`), including containers and volumes

You usually do **not** need to call `e2eComposeUp` manually.

## Security Model for Secrets

Secrets are environment-only and must **not** be stored in `.env` files:

- `SSL_KEYSTORE_PASSWORD` → shell environment only
- `POSTGRES_PASSWORD` → shell environment only
- `H2_PASSWORD` → shell environment only

Non-secret values are stored in synchronized `.env` files:

- `family-ties/.env`
- `family-ties/app/src/testE2E/resources/.env`

These `.env` files contain values like:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `SERVER_PORT`
- `H2_USERNAME`

## From-Scratch Setup (Brand New Environment)

This section assumes:

- No keystores exist yet
- No `.env` files exist yet

### Human-Friendly Flow

```bash
cd family-ties

# Required environment-only secrets
export SSL_KEYSTORE_PASSWORD='your-strong-ssl-password'
export POSTGRES_PASSWORD='your-strong-postgres-password'
export H2_PASSWORD='your-strong-h2-password'

# Generates missing keystores and creates synchronized .env files
./setup-test-env.sh

# Run E2E
./gradlew testE2E --no-daemon --no-build-cache --info
```

What `setup-test-env.sh` does:

- Generates keystores if missing:
  - `app/src/testE2E/resources/ssl/keystore.p12`
  - `app/src/testSystem/resources/ssl/keystore.p12`
  - `app/src/main/resources/keystore.p12`
- Creates/synchronizes root and E2E `.env` files with non-secret values
- Keeps `SSL_KEYSTORE_PASSWORD` and `POSTGRES_PASSWORD` out of `.env` files

### Automation-Friendly Flow

Use `bootstrapProject.ps1` for scripts and agents — it is fully non-interactive and handles all env-var defaulting:

```powershell
$env:SSL_KEYSTORE_PASSWORD = 'your-strong-ssl-password'
$env:POSTGRES_PASSWORD = 'your-strong-postgres-password'
$env:H2_PASSWORD = 'your-strong-h2-password'

pwsh -NonInteractive -NoProfile -File ./bootstrapProject.ps1
```

After bootstrap succeeds:

```bash
./gradlew testE2E --no-daemon --no-build-cache --info
```

If you need a shell-only non-interactive flow (without PowerShell), pre-set all env vars to suppress prompts:

```bash
set -euo pipefail

cd family-ties

export SSL_KEYSTORE_PASSWORD='your-strong-ssl-password'
export POSTGRES_PASSWORD='your-strong-postgres-password'
export H2_PASSWORD='your-strong-h2-password'

# Provide all non-secret inputs through env to avoid prompts
H2_USERNAME='sa' \
POSTGRES_DB='familyties' \
POSTGRES_USER='familyties' \
SERVER_PORT='8443' \
./setup-test-env.sh

./gradlew testE2E --no-daemon --no-build-cache --info
```

## Manual Debugging (Optional)

Only needed when debugging container startup or runtime behavior.

```bash
cd family-ties

export SSL_KEYSTORE_PASSWORD='your-strong-ssl-password'
export POSTGRES_PASSWORD='your-strong-postgres-password'
export H2_PASSWORD='your-strong-h2-password'

./gradlew e2eComposeUp
./gradlew testE2E
./gradlew e2eComposeLogs
./gradlew e2eComposeDown
```

## CI/CD and Local Requirements

Minimum prerequisites:

- Docker + Docker Compose
- Java 21
- Gradle wrapper (`./gradlew`)

Required shell environment for E2E:

- `SSL_KEYSTORE_PASSWORD`
- `POSTGRES_PASSWORD`

## Troubleshooting

### `validateE2EEnvironment` fails

Common causes:

- `SSL_KEYSTORE_PASSWORD` not exported
- `POSTGRES_PASSWORD` not exported
- `H2_PASSWORD` not exported
- Root and E2E `.env` files missing
- Secrets found in `.env` (not allowed)

Fix:

```bash
cd family-ties
export SSL_KEYSTORE_PASSWORD='...'
export POSTGRES_PASSWORD='...'
export H2_PASSWORD='...'
./setup-test-env.sh
./gradlew testE2E --no-daemon --no-build-cache --info
```

### App container unhealthy / startup failures

Inspect logs:

```bash
cd family-ties
./gradlew e2eComposeLogs
```

### Keystore/password mismatch

Regenerate keystores with the current `SSL_KEYSTORE_PASSWORD`:

```bash
cd family-ties
export SSL_KEYSTORE_PASSWORD='...'
export POSTGRES_PASSWORD='...'
export H2_PASSWORD='...'
./setup-test-env.sh
./gradlew testE2E --no-daemon --no-build-cache --info
```

## Relationship to `qa`

- `qa` depends on `testE2E`
- Therefore, `./gradlew qa` also executes the same E2E Compose lifecycle automatically
