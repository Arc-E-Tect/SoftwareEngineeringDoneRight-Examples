# SoftwareEngineeringDoneRight-Examples

A hardened, enterprise-grade example repository showcasing security best practices and development standards.

This repository demonstrates the security hardening patterns from [SoftwareEngineeringDoneRight-Gradle](https://github.com/Arc-E-Tect/SoftwareEngineeringDoneRight-Gradle), including:

- 🔐 **Git Hooks**: Pre-commit secret detection and pre-push PR validation
- 🛡️ **Security Policies**: Zero-credentials policy with environment variable management
- 🔍 **Vulnerability Scanning**: OWASP DependencyCheck for NVD-based CVE detection
- 🚀 **CI/CD**: GitHub Actions workflows for build, test, and automated releases
- 📋 **Code Quality**: JaCoCo test coverage reporting and semantic versioning

## Quick Start

### Prerequisites

- Java 21+
- Gradle 9.5.0 (or compatible) - can use system Gradle or wrapper
- Git

### Build & Test

```bash
# Build project
gradle build

# Run tests
gradle test

# Run security scan
gradle dependencyCheckAnalyze

# Generate test coverage report
gradle jacocoTestReport
```

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/Arc-E-Tect/SoftwareEngineeringDoneRight-Examples.git
   cd SoftwareEngineeringDoneRight-Examples
   ```

2. Set up environment variables:
   ```bash
   cp .env.example .env
   # Edit .env with your local values
   ```

3. Run the build:
   ```bash
   gradle build
   ```

## Security & Hardening

### Pre-Commit Hooks

Git hooks automatically validate commits for:
- **Secrets detection**: Blocks hardcoded passwords, API keys, tokens, etc.
- **Keystore protection**: Prevents committing SSL keystores
- **Workflow validation**: Runs `actionlint` on modified GitHub Actions workflows
- **Package lock consistency**: Ensures lock files match manifest files

Setup hooks locally:
```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/pre-push
```

See [.githooks/README.md](.githooks/README.md) for detailed information.

### Secrets Management

This repository enforces a **zero-credentials policy** - no secrets are stored in version control.

- **Configuration**: Use environment variable placeholders (e.g., `${DATABASE_PASSWORD}`)
- **Local development**: Copy `.env.example` to `.env` and add your credentials (`.env` is gitignored)
- **Production**: Use secret management services (AWS Secrets Manager, Azure Key Vault, etc.)

See [ZERO_CREDENTIALS_POLICY.md](ZERO_CREDENTIALS_POLICY.md) and [SECRETS_POLICY.md](SECRETS_POLICY.md) for details.

### Dependency Scanning

NVD-based vulnerability scanning runs automatically in CI/CD:
```bash
gradle dependencyCheckUpdate  # Update vulnerability database
gradle dependencyCheckAnalyze # Scan for CVEs
```

Reports are available in `build/reports/dependency-check-report.html`.

### Branch Protection

The `main` branch is protected with:
- ✅ Required pull request review (1 approval minimum)
- ✅ Required status checks (build, test, security scan)
- ✅ Dismissal of stale approvals on new commits
- ✅ Force push and deletion prevention

To configure manually:
1. Go to GitHub: **Settings** → **Branches** → **Branch protection rules**
2. Create rule for `main` branch with:
   - Require pull request reviews: 1 approval
   - Require status checks: `build-and-test`
   - Require branches to be up to date before merging
   - Restrict who can push (optional)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines, including:
- Conventional Commits format
- Pull request process
- Code standards
- Security reporting

## Security

For security issues, see [SECURITY.md](SECURITY.md) for responsible disclosure guidelines.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Related Projects

- [SoftwareEngineeringDoneRight-Gradle](https://github.com/Arc-E-Tect/SoftwareEngineeringDoneRight-Gradle) - Reference hardening implementation
