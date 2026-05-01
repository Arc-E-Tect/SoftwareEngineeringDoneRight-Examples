# Contributing to SoftwareEngineeringDoneRight-Examples

Thank you for your interest in contributing! This document provides guidelines to help ensure a smooth contribution process.

## Code of Conduct

Please be respectful and professional in all interactions. We are committed to maintaining a welcoming and inclusive community.

## How to Contribute

### Reporting Issues

Found a bug or security issue?
- **Security issues**: See [SECURITY.md](SECURITY.md) for responsible disclosure
- **Bugs**: Open an issue on GitHub with a clear description and reproduction steps

### Pull Request Process

1. **Fork** the repository
2. **Create a branch** for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes** following the code standards below

4. **Commit with Conventional Commits** format:
   ```
   feat(scope): short description
   
   Optional detailed explanation
   ```

   **Commit types**:
   - `feat`: New feature
   - `fix`: Bug fix
   - `perf`: Performance improvement
   - `refactor`: Code refactoring
   - `test`: Adding or updating tests
   - `ci`: CI/CD changes
   - `docs`: Documentation changes
   - `chore`: Other changes (dependencies, config, etc.)

5. **Push** to your fork and **create a Pull Request**

6. **Ensure all checks pass**:
   - ✅ Build and tests pass
   - ✅ Security scan passes
   - ✅ Code review approved
   - ✅ Pre-commit hooks pass

### Code Standards

- **Java**:
  - Follow Google Java Style Guide conventions
  - Use JDK 21+ features appropriately
  - Add meaningful comments for complex logic
  - Write unit tests for new functionality

- **All Languages**:
  - Keep code readable and maintainable
  - Add documentation for public APIs
  - Include tests for new features

### Security Best Practices

- **NO hardcoded secrets**: Use environment variables or `.env` files
- **NO test credentials**: Use `${VAR}` placeholders in configuration files
- **SECRET DETECTION**: Pre-commit hooks will catch and block secrets
- **WORKFLOWS**: Ensure GitHub Actions workflows use secrets, not hardcoded values
- See [ZERO_CREDENTIALS_POLICY.md](ZERO_CREDENTIALS_POLICY.md) for details

### Git Hooks

Before committing, ensure hooks are installed:
```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/pre-push
```

Hooks will:
- Detect hardcoded secrets
- Validate GitHub Actions workflows
- Check for keystore files
- Validate semantic-release config (on push to main)

### Testing

Before submitting a PR:

```bash
# Build project
gradle build

# Run all tests
gradle test

# Check test coverage
gradle jacocoTestReport

# Run security scan
gradle dependencyCheckAnalyze
```

## Development Setup

1. **Prerequisites**: Java 21+, Gradle 9.5.0+
2. **Clone**: `git clone https://github.com/Arc-E-Tect/SoftwareEngineeringDoneRight-Examples.git`
3. **Setup environment**:
   ```bash
   cp .env.example .env
   # Edit .env with your local values
   ```
4. **Build**: `gradle build`

## Branch Naming

Use descriptive branch names:
- `feature/add-new-feature` - New feature
- `fix/fix-specific-bug` - Bug fix
- `docs/update-readme` - Documentation updates
- `chore/upgrade-dependency` - Maintenance

## Getting Help

- **Questions**: Open a discussion or issue on GitHub
- **Documentation**: See README.md and policy documents
- **Security**: See [SECURITY.md](SECURITY.md)

## Merge Process

Your PR will be merged once:
1. ✅ All tests pass
2. ✅ Security scan passes (no vulnerabilities)
3. ✅ Code review approved
4. ✅ Branch is up-to-date with `main`

Main branch commits are automatically released using semantic versioning.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
