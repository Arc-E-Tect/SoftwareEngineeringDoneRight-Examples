import fs from 'node:fs/promises';
import path from 'node:path';

const SERVICES = [
  'system-admin',
  'user-account',
  'auth-server',
  'relationship',
  'social-network',
];

const redNeedleRoot = process.cwd();

const removeRequestHeaders = new Set(['accept-encoding', 'User-Agent', 'X-Forwarded-For']);
// Content-Type is not a meaningful request matcher for bodyless methods
const removeRequestHeadersForBodylessMethods = new Set(['Content-Type']);
const bodylessMethods = new Set(['GET', 'DELETE', 'HEAD', 'OPTIONS']);
// WireMock diagnostic/transfer headers that should not be replayed to clients
const removeResponseHeaders = new Set(['Matched-Stub-Id', 'Transfer-Encoding']);

async function rmAndEnsureDir(dir) {
  await fs.rm(dir, { recursive: true, force: true });
  await fs.mkdir(dir, { recursive: true });
}

async function walk(dir) {
  const entries = await fs.readdir(dir, { withFileTypes: true });
  const files = [];

  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...(await walk(fullPath)));
    } else if (entry.isFile() && entry.name.endsWith('.json')) {
      files.push(fullPath);
    }
  }

  return files;
}

function sanitizeMapping(mapping) {
  const cloned = structuredClone(mapping);

  const requestHeaders = cloned?.request?.headers;
  if (requestHeaders && typeof requestHeaders === 'object') {
    const method = (cloned?.request?.method ?? '').toUpperCase();
    for (const key of Object.keys(requestHeaders)) {
      if (removeRequestHeaders.has(key)) {
        delete requestHeaders[key];
      } else if (bodylessMethods.has(method) && removeRequestHeadersForBodylessMethods.has(key)) {
        delete requestHeaders[key];
      }
    }
  }

  const responseHeaders = cloned?.response?.headers;
  if (responseHeaders && typeof responseHeaders === 'object') {
    for (const key of Object.keys(responseHeaders)) {
      if (removeResponseHeaders.has(key)) {
        delete responseHeaders[key];
      }
    }
  }

  return cloned;
}

async function prepareService(service) {
  const sourceRoot = path.resolve(redNeedleRoot, `../red-thread/${service}/build/wiremock/stubs`);
  const targetRoot = path.resolve(redNeedleRoot, `wiremock/${service}/mappings`);
  const targetFilesRoot = path.resolve(redNeedleRoot, `wiremock/${service}/__files`);

  try {
    await fs.access(sourceRoot);
  } catch {
    console.error(`[${service}] Generated stubs were not found.`);
    console.error(`Run this first:`);
    console.error(`  cd ../red-thread && ./gradlew :${service}:docGen --no-daemon --no-build-cache --info`);
    process.exit(1);
  }

  await rmAndEnsureDir(targetRoot);
  await fs.mkdir(targetFilesRoot, { recursive: true });

  const files = await walk(sourceRoot);
  let count = 0;

  for (const filePath of files) {
    const rel = path.relative(sourceRoot, filePath);
    const out = path.join(targetRoot, rel);

    const raw = await fs.readFile(filePath, 'utf8');
    const parsed = JSON.parse(raw);
    const sanitized = sanitizeMapping(parsed);

    await fs.mkdir(path.dirname(out), { recursive: true });
    await fs.writeFile(out, `${JSON.stringify(sanitized, null, 2)}\n`, 'utf8');
    count += 1;
  }

  console.log(`[${service}] Prepared ${count} mapping file(s) → ${targetRoot}`);
}

async function main() {
  for (const service of SERVICES) {
    await prepareService(service);
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
