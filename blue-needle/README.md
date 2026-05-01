# Blue Needle

Blue Needle is an Angular micro-frontend client for IFF (Identification: Friend or Family).
It is structured around lazy-loaded routes — one per IFF microservice — and can run against
either generated WireMock stubs (no IFF build needed) or the full real IFF application stack.

## What it validates

- Generated stubs from `red-thread/<service>/build/wiremock/stubs`
- Docker Compose startup of WireMock with mappings loaded
- Client calls for all five IFF microservices (system-admin, user-account, auth-server, relationship, social-network):
  - Health information retrieval (`GET /actuator/health`)
  - Service version retrieval (`GET /`)

## Why mappings are normalized

Generated stubs contain strict header matching for transport/runtime headers such as `User-Agent`, `accept-encoding`, and `X-Forwarded-For`.
Additionally, `Content-Type` is stripped from request matchers for bodyless methods (`GET`, `DELETE`, `HEAD`, `OPTIONS`) because the docGen client sends it by default but browser clients do not.
Internal WireMock response headers (`Matched-Stub-Id`, `Transfer-Encoding`) are also removed.
`npm run stubs:prepare` applies all these normalizations and writes browser-compatible mappings to `wiremock/system-admin/mappings`.

## Service ports

Both modes use the same ports — `proxy.conf.json` requires no changes when switching:

| Service | HTTPS port |
|---|---|
| system-admin | 8443 |
| user-account | 8444 |
| auth-server | 8445 |
| relationship | 8446 |
| social-network | 8447 |

## Running Blue Needle

### Mode A — WireMock stubs (recommended for frontend development)

No SSL keystore setup is needed. IFF does not need to be built or running.

```bash
# 1. Install dependencies (first time only)
npm install

# 2. Generate stubs from Red Thread
#    Required before the first run and after any IFF API or implementation change.
cd ../red-thread
./gradlew :system-admin:docGen :user-account:docGen :auth-server:docGen :relationship:docGen :social-network:docGen --no-daemon
cd ../blue-needle

# 3. Start WireMock (prepares + starts containers)
npm run wiremock:up

# 4. Start Angular dev server
#    Detects and kills any existing process on port 4200 before starting.
./start-frontend.ps1   # PowerShell (Windows / macOS / Linux)
```

Open http://localhost:4200. Navigate to **System Admin** and use the POST / GET / DELETE buttons.

WireMock admin check:

```bash
curl -k -i https://localhost:8443/__admin/mappings
```

Stop WireMock:

```bash
npm run wiremock:down
```

### Mode B — Real IFF application

Requires a built IFF Docker image and SSL keystore. Use this mode to validate Blue Needle
against the actual running IFF services.

```bash
# 1. One-time setup in red-thread/ (generates keystores and .env files)
export SSL_KEYSTORE_PASSWORD='<your-local-secret>'
cd ../red-thread
./setup-test-env.sh
cd ../blue-needle

# 2. Start the full IFF stack
#    First run builds Docker images — this takes several minutes.
npm run iff:up

# 3. Start Angular dev server (identical command to Mode A)
#    Detects and kills any existing process on port 4200 before starting.
./start-frontend.ps1   # PowerShell (Windows / macOS / Linux)
```

Open http://localhost:4200. The Angular proxy routes to the real services on ports 8443–8447.

Stop the IFF stack:

```bash
npm run iff:down
```

## Automated BDD smoke test (Cucumber + Playwright)

The project contains an extendable smoke suite under `blue-needle-smoke/` using:

- Gherkin feature files (`.feature`)
- Cucumber step definitions
- Playwright API request context

### Mode A — smoke test against WireMock stubs

Stubs must be generated before the first run and after any IFF API or implementation change.

```bash
# Generate / refresh stubs (run from red-thread/)
cd ../red-thread && ./gradlew :system-admin:docGen :user-account:docGen :auth-server:docGen :relationship:docGen :social-network:docGen --no-daemon && cd ../blue-needle

# Start WireMock, run Cucumber, stop WireMock
npm run smoke:test
```

Run only the test execution against an already-running WireMock:

```bash
npm run smoke:test:run
```

### Mode B — smoke test against the real IFF stack

Requires the `iff/*:e2e` Docker images to be up to date.
Images are built automatically by `./gradlew :e2e:testE2E` in `red-thread/`.
For smoke tests only (no rebuild), the existing images from the last E2E run are used.

```bash
# Start IFF stack, run Cucumber, stop IFF stack
npm run smoke:test:iff
```

## Notes

- The Angular dev server uses `proxy.conf.json`, routing API calls to their respective service ports (8443–8447) in both WireMock and real-app mode.
- Per-service base URL overrides are available via environment variables: `IFF_SYSTEM_ADMIN_URL`, `IFF_USER_ACCOUNT_URL`, `IFF_AUTH_SERVER_URL`, `IFF_RELATIONSHIP_URL`, `IFF_SOCIAL_NETWORK_URL`.
- Only `wiremock/system-admin/` is listed in `.gitignore`; stub directories for the other four services are tracked in version control.
