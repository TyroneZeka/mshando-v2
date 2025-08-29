# Backend Version Control Guidelines

## 🏷️ Tagging Strategy

### Semantic Versioning (SemVer)
- **MAJOR** (X.0.0): Breaking changes to API or database schema
- **MINOR** (0.X.0): New features, backward compatible
- **PATCH** (0.0.X): Bug fixes, backward compatible

### Tag Format
```
v[MAJOR].[MINOR].[PATCH]
```

Examples:
- `v1.0.0` - Initial stable release
- `v1.1.0` - New features added
- `v1.1.1` - Bug fixes

## 📝 Commit Message Format

### Structure
```
<type>(<scope>): <description>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Scopes (Microservices)
- `api-gateway`: API Gateway service
- `user-service`: User management service
- `task-service`: Task management service
- `bidding-service`: Bidding system service
- `payment-service`: Payment processing service
- `notification-service`: Notification system service

### Examples
```bash
feat(task-service): add task assignment functionality
fix(api-gateway): resolve CORS duplicate headers
docs(user-service): update API documentation
refactor(bidding-service): optimize bid calculation logic
```

## 🚀 Release Process

### 1. Feature Development
```bash
git checkout -b feature/new-feature
# Develop feature
git commit -m "feat(service): implement new feature"
git push origin feature/new-feature
# Create PR and merge to main
```

### 2. Bug Fixes
```bash
git checkout -b fix/issue-description
# Fix bug
git commit -m "fix(service): resolve specific issue"
git push origin fix/issue-description
# Create PR and merge to main
```

### 3. Release Preparation
```bash
git checkout main
git pull origin main
# Update version in pom.xml files
git add .
git commit -m "chore: bump version to v1.2.0"
git tag -a v1.2.0 -m "Release v1.2.0: Description"
git push origin main --tags
```

## 🔄 Branch Strategy

### Main Branches
- `main`: Production-ready code
- `develop`: Integration branch for features

### Supporting Branches
- `feature/*`: New features
- `fix/*`: Bug fixes
- `hotfix/*`: Critical production fixes
- `release/*`: Release preparation

## 📋 Pre-commit Checklist

- [ ] All tests pass
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] Version numbers updated if needed
- [ ] Commit message follows format
- [ ] No sensitive data in commit

## 🏗️ Build and Deploy

### Local Development
```bash
./build-all.sh      # Build all services
./start-all.sh      # Start all services
./stop-all.sh       # Stop all services
./status.sh         # Check service status
```

### Deployment
```bash
git checkout v1.2.0  # Checkout specific version
./deploy.sh          # Deploy to environment
```

## 📊 Version Tracking

### Check Current Version
```bash
git describe --tags --abbrev=0  # Latest tag
git log --oneline -10           # Recent commits
```

### View Changes Between Versions
```bash
git log v1.0.0..v1.1.0 --oneline
git diff v1.0.0..v1.1.0
```
