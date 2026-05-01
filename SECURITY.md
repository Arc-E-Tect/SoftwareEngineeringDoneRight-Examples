# Security Policy

## Reporting a Vulnerability

**DO NOT** open public issues for security vulnerabilities. Instead, please use GitHub's private vulnerability reporting feature or email the maintainers directly.

### How to Report

1. **Use GitHub Security Advisory**:
   - Go to the repository's **Security** tab
   - Click **Report a vulnerability**
   - Describe the vulnerability and provide reproduction steps
   - Submit privately to the maintainers

2. **Direct Contact** (if GitHub option unavailable):
   - Email the repository maintainers
   - Include: description, severity, affected versions, reproduction steps
   - Do not disclose publicly until a fix is released

### What to Include

- **Title**: Clear, concise vulnerability description
- **Severity**: Critical, High, Medium, Low
- **Affected versions**: Which project versions are affected
- **Description**: Detailed explanation of the vulnerability
- **Reproduction**: Steps to reproduce the issue
- **Impact**: What an attacker could do if exploited
- **Suggested fix**: If you have recommendations

## Responsible Disclosure Timeline

1. **Report submission**: Initial report received
2. **Acknowledgment** (within 48 hours): We confirm receipt and provide reference number
3. **Investigation** (within 1 week): We assess severity and scope
4. **Fix development**: We work on a patch
5. **Release** (typically 30 days): We release a fix in a new version
6. **Public disclosure**: Coordinated with you; details provided after fix is released

## Supported Versions

We provide security updates for:
- Current major version: Full support
- Previous major version: Security patches only
- Older versions: No support (please upgrade)

## Security Best Practices

### For This Repository

- **Zero-credentials policy**: No secrets are committed to version control
- **Secret detection**: Pre-commit hooks block hardcoded secrets
- **Dependency scanning**: NVD-based CVE scanning in CI/CD
- **Workflow validation**: GitHub Actions workflows are linted for security issues
- **Code review**: All changes require review before merge
- **Branch protection**: Main branch requires approval and passing checks

### For Users

- **Update regularly**: Keep dependencies updated to patch known vulnerabilities
- **Scan your use**: Run `gradle dependencyCheckAnalyze` to identify CVEs
- **Report issues**: Use responsible disclosure process above
- **Follow policies**: Adhere to security policies if using as a template

### Environment Variables

All secrets must be managed via environment variables:

```bash
# ✅ CORRECT
password: ${DATABASE_PASSWORD}
api_key: ${API_KEY}

# ❌ WRONG - will be blocked by pre-commit hook
password: hardcoded_password
api_key: hardcoded_api_key
```

## Vulnerability Scanning

This repository uses:

- **OWASP DependencyCheck**: NVD-based Java dependency scanning
- **actionlint**: GitHub Actions workflow validation
- **pre-commit hooks**: Secret detection and keystore protection

Run security checks locally:
```bash
gradle dependencyCheckUpdate   # Update NVD database
gradle dependencyCheckAnalyze  # Scan for vulnerabilities
```

## Incident Response

If a security issue is discovered:

1. **Investigation**: We determine impact and affected versions
2. **Patching**: We develop and test a fix
3. **Release**: We release a new version with patch
4. **Notification**: We notify users of the fix and encourage upgrades
5. **Disclosure**: We provide details after reasonable time for users to upgrade

## Security Policies

- [ZERO_CREDENTIALS_POLICY.md](ZERO_CREDENTIALS_POLICY.md) - No secrets in version control
- [SECRETS_POLICY.md](SECRETS_POLICY.md) - How to manage secrets safely
- [.githooks/README.md](.githooks/README.md) - Pre-commit hook protections

## Contact

For security inquiries:
- Primary: Report through GitHub Security Advisory (preferred)
- Alternative: Contact repository maintainers
- Do not disclose publicly until patch is released

## Acknowledgments

We appreciate responsible security researchers who report vulnerabilities through proper channels. We will acknowledge your contribution in our release notes if you wish.
