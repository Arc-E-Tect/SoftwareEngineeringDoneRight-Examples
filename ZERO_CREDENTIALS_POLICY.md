# Zero Credentials in Repository Policy

**Policy Level**: Mandatory - No Exceptions

## Policy Statement

**No credentials of any kind may be committed to version control, including test credentials.**

This zero-tolerance policy eliminates the entire class of vulnerabilities related to:
- Accidental production deployment of test configurations
- Credential leakage through configuration file copying
- Security risks from "safe" test passwords being similar to production patterns

## Rationale

Even test credentials introduce vulnerabilities when:
1. Configuration files are accidentally deployed to production
2. Test configurations are copied and reused in production contexts
3. Developers use similar patterns for both test and production credentials
4. Configuration files are shared across environments

**The only safe credential is one that doesn't exist in version control.**

## Implementation

### Configuration Files

All configuration files use environment variable placeholders:

```yaml
# ✅ CORRECT - Uses environment variables
database:
  password: ${DATABASE_PASSWORD}

# ❌ WRONG - Hardcoded test credentials (will be blocked by pre-commit hook)
database:
  password: testpassword
```

### Build Script (Gradle)

Test credentials are passed through from environment variables with **NO hardcoded defaults**:

```gradle
tasks.withType(Test).configureEach {
    // Environment variables - must be set via .env file or shell environment
    environment 'DATABASE_PASSWORD', System.getenv('DATABASE_PASSWORD')
    environment 'API_KEY', System.getenv('API_KEY')
}
```

**Why NO defaults:**
- Even test defaults in build files create security risks
- Forces explicit credential management from the start
- Prevents accidental use of weak test credentials in production-like environments
- CI/CD must explicitly configure credentials

### Pre-Commit Hook Enforcement

The pre-commit hook blocks ALL credentials in ALL files:

```bash
# Pattern matches any credential-like key-value pair
secret_pattern='(password|passwd|pwd|secret|api[-_]?key|...)[[:space:]]*[:=][[:space:]]*[^<$#][^$#]{2,}'

# Only documentation files are exempt (.example, .sample, .template)
# Test configuration files are NOT exempt
```

## Allowed Patterns

### Documentation Only

These patterns are allowed **ONLY** in:
- Git hooks (`.githooks/*`) - may contain pattern examples in comments
- Security documentation files (matching `*SECURITY*.md`, `*POLICY*.md`, `*SECRET*.md`, `*.adoc`)
- Template files (`.example`, `.sample`, `.template`)

```yaml
# ✅ Placeholder syntax
password: <your-password>

# ✅ Environment variable syntax  
password: ${PASSWORD}

# ✅ Comment documentation
password: # Set this via environment variable

# ✅ Clear example (security docs only)
password: EXAMPLE_PASSWORD
```

## Development Workflow

### Running Tests/Applications Locally

Tests require credentials from a `.env` file or environment variables:

```bash
# Option 1: Use .env file (recommended)
cp .env.example .env
# Edit .env with your credentials
./gradlew test

# Option 2: Export environment variables directly
export DATABASE_PASSWORD=your-password
export API_KEY=your-api-key
./gradlew test

# Option 3: Pass via Gradle CLI
./gradlew test \
  -DDATABASE_PASSWORD=your-password \
  -DAPI_KEY=your-api-key
```

### CI/CD Configuration

CI/CD systems should inject credentials as environment variables:

```yaml
# GitHub Actions example
- name: Run Tests
  env:
    DATABASE_PASSWORD: ${{ secrets.TEST_DB_PASSWORD }}
    API_KEY: ${{ secrets.TEST_API_KEY }}
  run: ./gradlew test
```

## Consequences of Violation

### Pre-Commit Hook

Commits with credentials will be **automatically blocked**:

```
Error: Potential secret detected in src/main/resources/application.yml
Matched lines:
5:    password: testpassword

Secrets must not be committed. Use environment variables or .env files instead.
```

### Manual Override

Bypassing the hook is **strongly discouraged**:

```bash
git commit --no-verify  # ⚠️ DO NOT DO THIS
```

If you believe you have a legitimate case, discuss with the team first.

## Exceptions

**There are no exceptions to this policy.**

- ❌ Not for test credentials
- ❌ Not for "safe" passwords
- ❌ Not for temporary configurations
- ❌ Not for development environments

The only allowed credentials in the repository are:
- ✅ Placeholders in template files (`.example`, `.sample`, `.template`)
- ✅ Environment variable references (e.g., `${PASSWORD}`)
- ✅ Examples in security documentation (`*SECURITY*.md`, `*POLICY*.md`, `*SECRET*.md`, `*.adoc`)
- ✅ Pattern examples in git hooks (`.githooks/*`)

## Benefits

1. **Zero production risk**: Test configurations cannot be accidentally deployed with credentials
2. **Simplified compliance**: Clear yes/no policy, no judgment calls required
3. **Better security hygiene**: Forces proper credential management from the start
4. **Easier auditing**: grep for credentials in repo should return zero results
5. **CI/CD ready**: Forces teams to use proper secret management systems

## Questions?

### "How do I run tests without setting up environment variables?"

Use the `.env` file approach: copy `.env.example` to `.env` and populate with your credentials.

### "What if I need different credentials for different scenarios?"

Override via environment variables:
```bash
DATABASE_PASSWORD=scenario1 ./gradlew test
```

### "Can I use any password values I want?"

**YES! All passwords can be freely chosen.**

Use whatever credentials work for your local development environment. They are stored in `.env` which is gitignored.

---

**Remember**: The safest credential is the one that never makes it into version control.
