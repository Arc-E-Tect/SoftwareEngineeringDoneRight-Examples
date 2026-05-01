import { spawnSync } from 'node:child_process';
import https from 'node:https';

const IFF_MODE = process.env.IFF_MODE || 'wiremock';

const modeConfig = {
  wiremock: {
    upScript: 'wiremock:up',
    downScript: 'wiremock:down',
    readinessUrl: process.env.IFF_WIREMOCK_ADMIN_URL || 'https://localhost:8443/__admin/mappings',
    timeoutMs: 30_000
  },
  real: {
    upScript: 'iff:up',
    downScript: 'iff:down',
    readinessUrl: (process.env.IFF_BASE_URL || 'https://localhost:8443') + '/actuator/health',
    timeoutMs: 120_000
  }
}[IFF_MODE];

if (!modeConfig) {
  console.error(`Unknown IFF_MODE "${IFF_MODE}". Expected "wiremock" or "real".`);
  process.exit(1);
}

const { upScript, downScript, readinessUrl, timeoutMs } = modeConfig;

function run(command, args) {
  const result = spawnSync(command, args, {
    stdio: 'inherit',
    env: process.env
  });

  if (result.status !== 0) {
    throw new Error(`${command} ${args.join(' ')} failed with exit code ${result.status}`);
  }
}

async function waitForReady() {
  const start = Date.now();

  while (Date.now() - start < timeoutMs) {
    try {
      const ok = await checkEndpoint(readinessUrl);
      if (ok) {
        return;
      }
    } catch {
    }

    await new Promise((resolve) => setTimeout(resolve, 1000));
  }

  throw new Error(`Service did not become ready within ${timeoutMs}ms at ${readinessUrl}`);
}

async function checkEndpoint(url) {
  if (url.startsWith('https://')) {
    return new Promise((resolve) => {
      const request = https.request(url, { method: 'GET', rejectUnauthorized: false }, (response) => {
        resolve((response.statusCode ?? 0) >= 200 && (response.statusCode ?? 0) < 400);
      });

      request.on('error', () => resolve(false));
      request.end();
    });
  }

  const response = await fetch(url, { method: 'GET' });
  return response.ok;
}

let smokeError;

try {
  run('npm', ['run', upScript]);
  await waitForReady();
  run('npm', ['run', 'smoke:test:run']);
} catch (error) {
  smokeError = error;
} finally {
  try {
    run('npm', ['run', downScript]);
  } catch (downError) {
    if (!smokeError) {
      smokeError = downError;
    }
  }
}

if (smokeError) {
  console.error(smokeError.message);
  process.exit(1);
}

console.log('Smoke test finished successfully.');
