# Git Workflow Guide

## Overview

This document outlines the Git workflow and version control practices for the Mshando project. We follow GitFlow with conventional commits and automated quality checks.

## Branch Strategy

### Main Branches

- **`main`** - Production-ready code
- **`develop`** - Integration branch for features

### Supporting Branches

- **`feature/*`** - New features
- **`hotfix/*`** - Production bug fixes
- **`release/*`** - Release preparation

## Conventional Commits

We use [Conventional Commits](https://www.conventionalcommits.org/) format:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation only changes
- **style**: Formatting, missing semicolons, etc.
- **refactor**: Code change that neither fixes a bug nor adds a feature
- **test**: Adding missing tests or correcting existing tests
- **chore**: Maintenance tasks
- **perf**: Performance improvements
- **ci**: CI/CD pipeline changes
- **build**: Build system or external dependency changes
- **revert**: Reverts a previous commit

### Examples

```bash
feat(auth): add JWT authentication
fix: resolve login redirect issue
docs: update API documentation
style: fix formatting in UserController
refactor(user): extract validation logic
test: add unit tests for AuthService
chore: update dependencies
```

## Git Hooks Setup

Install the provided Git hooks to ensure code quality:

```bash
# Set up Git hooks directory
git config core.hooksPath .githooks

# Make hooks executable (Linux/Mac)
chmod +x .githooks/*

# For Windows Git Bash
git update-index --chmod=+x .githooks/pre-commit
git update-index --chmod=+x .githooks/commit-msg
```

### Pre-commit Hook

Automatically runs before each commit:
- ✅ Compiles the code
- ✅ Runs unit tests
- ✅ Checks code style
- ✅ Runs security scans
- ✅ Validates commit message format

### Commit Message Hook

Validates commit message format according to conventional commits standard.

## Workflow Steps

### 1. Feature Development

```bash
# Create and checkout feature branch
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# Work on your feature
# ... make changes ...

# Commit changes (triggers pre-commit hook)
git add .
git commit -m "feat(auth): implement JWT authentication"

# Push feature branch
git push origin feature/user-authentication

# Create Pull Request to develop branch
```

### 2. Code Review Process

1. **Create Pull Request**
   - Target: `develop` branch
   - Include description of changes
   - Link related issues

2. **Review Checklist**
   - [ ] Code follows project standards
   - [ ] Tests are included and passing
   - [ ] Documentation is updated
   - [ ] No security vulnerabilities
   - [ ] Performance impact considered

3. **Merge Requirements**
   - ✅ All CI checks pass
   - ✅ At least one approval
   - ✅ No merge conflicts
   - ✅ Up-to-date with target branch

### 3. Release Process

```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# Finalize release (version bumps, changelog, etc.)
# ... make release preparations ...

# Merge to main and develop
git checkout main
git merge release/v1.2.0
git tag -a v1.2.0 -m "Release version 1.2.0"

git checkout develop
git merge release/v1.2.0

# Push everything
git push origin main
git push origin develop
git push origin v1.2.0

# Delete release branch
git branch -d release/v1.2.0
git push origin --delete release/v1.2.0
```

### 4. Hotfix Process

```bash
# Create hotfix branch from main
git checkout main
git pull origin main
git checkout -b hotfix/critical-security-fix

# Make the fix
# ... fix the issue ...

# Commit and push
git commit -m "fix: resolve critical security vulnerability"
git push origin hotfix/critical-security-fix

# Merge to main and develop
git checkout main
git merge hotfix/critical-security-fix
git tag -a v1.2.1 -m "Hotfix version 1.2.1"

git checkout develop
git merge hotfix/critical-security-fix

# Push everything
git push origin main
git push origin develop
git push origin v1.2.1

# Delete hotfix branch
git branch -d hotfix/critical-security-fix
git push origin --delete hotfix/critical-security-fix
```

## Best Practices

### Commit Guidelines

1. **Make atomic commits** - One logical change per commit
2. **Write clear messages** - Explain what and why, not how
3. **Keep commits small** - Easier to review and debug
4. **Test before committing** - Ensure code works

### Branch Guidelines

1. **Use descriptive names** - `feature/jwt-authentication` not `feature/auth`
2. **Keep branches short-lived** - Merge frequently to avoid conflicts
3. **Delete merged branches** - Keep repository clean
4. **Rebase vs Merge** - Use rebase for feature branches, merge for integration

### Pull Request Guidelines

1. **Clear title and description** - Help reviewers understand changes
2. **Small, focused PRs** - Easier to review thoroughly
3. **Include tests** - Maintain code coverage
4. **Update documentation** - Keep docs in sync with code

## CI/CD Integration

### GitHub Actions

Our CI/CD pipeline automatically:
- Runs on every push and pull request
- Executes comprehensive test suite
- Performs security scans
- Builds Docker images
- Deploys to staging/production

### Quality Gates

Code must pass all checks before merging:
- ✅ Unit and integration tests
- ✅ Code coverage > 80%
- ✅ Security scan (no high vulnerabilities)
- ✅ Code style compliance
- ✅ Documentation updates

## Common Commands

### Setup

```bash
# Clone repository
git clone https://github.com/TyroneZeka/mshando-v2.git
cd mshando-v2

# Install Git hooks
git config core.hooksPath .githooks
chmod +x .githooks/* # Linux/Mac only
```

### Daily Workflow

```bash
# Start new feature
git checkout develop
git pull origin develop
git checkout -b feature/new-feature

# Work and commit
git add .
git commit -m "feat: implement new feature"

# Keep up to date
git fetch origin
git rebase origin/develop

# Push when ready
git push origin feature/new-feature
```

### Troubleshooting

```bash
# Fix merge conflicts
git status
# Edit conflicted files
git add .
git commit

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Undo last commit (discard changes)
git reset --hard HEAD~1

# View commit history
git log --oneline --graph --decorate --all
```

## Tools and Extensions

### Recommended Tools

- **Git GUI**: GitKraken, Sourcetree, or VS Code Git integration
- **Commit Message Helper**: Conventional Commits VSCode extension
- **Code Review**: GitHub CLI for PR management

### VSCode Extensions

- GitLens
- Conventional Commits
- Git Graph
- Git History

## Support

For questions or issues with the Git workflow:
1. Check this documentation first
2. Search existing issues on GitHub
3. Ask in the team chat
4. Create an issue if needed

Remember: When in doubt, ask! It's better to clarify than to make mistakes that affect the team.
