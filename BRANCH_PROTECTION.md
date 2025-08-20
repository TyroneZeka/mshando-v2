# Branch Protection Rules Configuration

This document outlines the recommended branch protection rules for the Mshando repository.

## Main Branch Protection

**Branch:** `main`

### Required Settings

- [x] **Require a pull request before merging**
  - [x] Require approvals: **2**
  - [x] Dismiss stale PR approvals when new commits are pushed
  - [x] Require review from code owners
  - [x] Restrict pushes that create new files

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - Required status checks:
    - `test`
    - `build`
    - `security-scan`
    - `code-coverage`

- [x] **Require conversation resolution before merging**

- [x] **Require signed commits**

- [x] **Require linear history**

- [x] **Include administrators**

- [x] **Restrict pushes that create new files**

- [x] **Allow force pushes: NO**

- [x] **Allow deletions: NO**

## Develop Branch Protection

**Branch:** `develop`

### Required Settings

- [x] **Require a pull request before merging**
  - [x] Require approvals: **1**
  - [x] Dismiss stale PR approvals when new commits are pushed
  - [x] Require review from code owners

- [x] **Require status checks to pass before merging**
  - [x] Require branches to be up to date before merging
  - Required status checks:
    - `test`
    - `build`
    - `security-scan`

- [x] **Require conversation resolution before merging**

- [x] **Include administrators**

- [x] **Allow force pushes: NO**

- [x] **Allow deletions: NO**

## Setup Instructions

### Via GitHub Web Interface

1. Go to Settings > Branches
2. Click "Add rule"
3. Configure each branch according to the settings above

### Via GitHub CLI

```bash
# Main branch protection
gh api repos/:owner/:repo/branches/main/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["test","build","security-scan","code-coverage"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":2,"dismiss_stale_reviews":true,"require_code_owner_reviews":true}' \
  --field restrictions=null \
  --field required_linear_history=true \
  --field allow_force_pushes=false \
  --field allow_deletions=false

# Develop branch protection
gh api repos/:owner/:repo/branches/develop/protection \
  --method PUT \
  --field required_status_checks='{"strict":true,"contexts":["test","build","security-scan"]}' \
  --field enforce_admins=true \
  --field required_pull_request_reviews='{"required_approving_review_count":1,"dismiss_stale_reviews":true,"require_code_owner_reviews":true}' \
  --field restrictions=null \
  --field allow_force_pushes=false \
  --field allow_deletions=false
```

## CODEOWNERS File

Create `.github/CODEOWNERS` to automatically request reviews:

```
# Global owners
* @TyroneZeka

# Backend code
/mshando-microservices/ @TyroneZeka @backend-team

# Security-related files
/mshando-microservices/**/SecurityConfig.java @TyroneZeka @security-team
/.github/workflows/ @TyroneZeka @devops-team

# Documentation
*.md @TyroneZeka @docs-team
/docs/ @TyroneZeka @docs-team
```

## Additional Security Measures

### Repository Settings

1. **General**
   - [ ] Allow merge commits
   - [x] Allow squash merging
   - [x] Allow rebase merging
   - [x] Automatically delete head branches

2. **Security**
   - [x] Enable vulnerability alerts
   - [x] Enable dependency graph
   - [x] Enable Dependabot alerts
   - [x] Enable Dependabot security updates

3. **Features**
   - [x] Wikis: Disabled
   - [x] Issues: Enabled
   - [x] Projects: Enabled
   - [x] Discussions: Enabled if needed

### Environment Protection Rules

For production deployments:

1. **Required reviewers**: Minimum 2 from admin team
2. **Wait timer**: 5 minutes
3. **Restrict to protected branches**: main only
4. **Environment secrets**: Production credentials
