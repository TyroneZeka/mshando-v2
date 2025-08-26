# Git Commit Guidelines for Mshando Project

## Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

## Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **build**: Changes that affect the build system or external dependencies
- **ci**: Changes to our CI configuration files and scripts
- **chore**: Other changes that don't modify src or test files
- **revert**: Reverts a previous commit
- **security**: Security-related changes

## Scopes

Common scopes for this project:
- **user-service**: Changes to the user service
- **api-gateway**: Changes to the API gateway
- **eureka-server**: Changes to the service discovery server
- **auth**: Authentication-related changes
- **email**: Email service changes
- **config**: Configuration changes
- **docker**: Docker-related changes
- **tests**: Test-related changes

## Examples

### Good Examples

```bash
feat(user-service): add email verification system

Implement comprehensive email verification with:
- Token generation and validation
- Email templates for verification and welcome
- Development mode support without SMTP
- Resend verification functionality

Closes #123
```

```bash
fix(auth): resolve JWT token validation issue

The token validation was failing for verified users due to
incorrect user status checking in the isTokenValid method.

- Update JwtService to check user.isActive() properly
- Add null safety checks for user status
- Update tests to cover edge cases
```

```bash
security: remove hardcoded secrets from configuration

- Replace database passwords with environment variables
- Add JWT secret environment variable support
- Create secret generation scripts
- Update documentation with security setup instructions
```

### Bad Examples

```bash
# Too vague
fix: bug fix

# Missing type
updated user service

# No description
feat: 

# Too long subject line
feat(user-service): implement comprehensive email verification system with token generation validation email templates development mode support and resend functionality
```

## Rules

1. **Subject Line**:
   - Keep it under 50 characters
   - Use imperative mood ("add" not "added" or "adds")
   - Don't end with a period
   - Capitalize the first letter after the colon

2. **Body** (optional):
   - Wrap at 72 characters
   - Explain what and why, not how
   - Use bullet points for multiple changes
   - Reference issues/PRs when applicable

3. **Breaking Changes**:
   - Add `!` after the type/scope: `feat!: change API response format`
   - Describe the breaking change in the footer

4. **Footer** (optional):
   - Reference issues: `Closes #123`, `Fixes #456`
   - Note breaking changes: `BREAKING CHANGE: API response format changed`

## Branching Strategy

### Main Branches
- `master`: Production-ready code
- `develop`: Development integration branch

### Supporting Branches
- `feature/*`: New features
- `bugfix/*`: Bug fixes
- `hotfix/*`: Emergency fixes
- `release/*`: Release preparation

### Branch Naming
```bash
# Features
feature/user-profile-management
feature/email-verification-system
feature/password-reset-functionality

# Bug fixes
bugfix/jwt-token-validation
bugfix/email-sending-failure
bugfix/database-connection-timeout

# Hotfixes
hotfix/security-vulnerability-fix
hotfix/critical-auth-bug
```

## Workflow

### Feature Development
```bash
# Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/email-verification-system

# Make changes and commit regularly
git add .
git commit -m "feat(user-service): add email verification service"

# Push and create PR
git push origin feature/email-verification-system
```

### Regular Commits
Make frequent, small commits that represent logical units of work:

```bash
feat(user-service): add email verification model and repository
feat(user-service): implement email verification service logic
feat(user-service): add email verification controller endpoints
test(user-service): add email verification integration tests
docs(user-service): update API documentation for email verification
```

### Before Committing
1. Run tests: `./mshando-microservices/user-service/quick-test.sh`
2. Check code style and formatting
3. Review your changes: `git diff --cached`
4. Ensure no sensitive data is included
5. Write a clear, descriptive commit message

## Release Process

### Version Tagging
```bash
# Tag releases with semantic versioning
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

### Release Notes
Include in tag message or release notes:
- New features
- Bug fixes
- Breaking changes
- Migration instructions
- Security updates

## Security Considerations

### Never Commit
- Passwords, API keys, or secrets
- Database connection strings with credentials
- Private keys or certificates
- Personal information
- Environment-specific configuration

### Always Check
```bash
# Review staged changes before committing
git diff --cached

# Check for sensitive data
git log --grep="password\|secret\|key" --all
```

### If You Accidentally Commit Secrets
1. Immediately rotate/change the compromised secrets
2. Remove from Git history (if recent): `git reset --soft HEAD~1`
3. For older commits, use `git filter-branch` or BFG Repo-Cleaner
4. Force push (only if safe): `git push --force-with-lease`

## Tools and Automation

### Git Hooks (Optional)
Create `.git/hooks/pre-commit`:
```bash
#!/bin/sh
# Run tests before committing
./mshando-microservices/user-service/quick-test.sh
```

### Commit Message Template
```bash
# Set up commit message template
git config commit.template .gitmessage.txt
```

## Review Checklist

Before submitting a pull request:

- [ ] Code follows project conventions
- [ ] Tests pass locally
- [ ] Documentation updated if needed
- [ ] No sensitive data committed
- [ ] Commit messages follow guidelines
- [ ] Branch is up to date with target branch
- [ ] Changes are focused and coherent
- [ ] Breaking changes are documented

## Examples of Good Commit History

```
feat(user-service): implement comprehensive authentication system
security: enhance JWT token validation with user status checks
feat(email): add development mode support for email verification
fix(auth): resolve token expiration edge case
docs: update API documentation with authentication examples
test(user-service): add comprehensive integration tests
chore(deps): update Spring Boot to 3.1.5
refactor(user-service): extract email service into separate component
```

This approach ensures a clean, readable Git history that helps with:
- Understanding what changed and why
- Debugging issues
- Code reviews
- Release planning
- Automated changelog generation
