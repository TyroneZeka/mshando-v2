# Git Configuration for Mshando Project

This document contains recommended Git configuration for the Mshando project.

## Global Git Configuration

Run these commands to set up your Git configuration:

```bash
# User information
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Core settings
git config --global core.autocrlf true  # Windows
git config --global core.autocrlf input # Linux/Mac
git config --global core.editor "code --wait"  # VS Code
git config --global init.defaultBranch main

# Security
git config --global user.signingkey YOUR_GPG_KEY_ID
git config --global commit.gpgsign true
git config --global tag.gpgsign true

# Diff and merge tools
git config --global merge.tool vscode
git config --global mergetool.vscode.cmd 'code --wait $MERGED'
git config --global diff.tool vscode
git config --global difftool.vscode.cmd 'code --wait --diff $LOCAL $REMOTE'

# Push settings
git config --global push.default simple
git config --global push.autoSetupRemote true

# Pull settings
git config --global pull.rebase true

# Credential helper (Windows)
git config --global credential.helper manager-core
```

## Useful Git Aliases

Add these aliases to your Git configuration:

```bash
# Status and log aliases
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.unstage 'reset HEAD --'
git config --global alias.last 'log -1 HEAD'

# Enhanced log views
git config --global alias.lg "log --color --graph --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit"
git config --global alias.tree "log --graph --pretty=format:'%Cred%h%Creset %Cgreen(%cr)%Creset %C(bold blue)<%an>%Creset%C(yellow)%d%Creset %s' --abbrev-commit --all"

# Workflow aliases
git config --global alias.sync '!git fetch origin && git rebase origin/$(git branch --show-current)'
git config --global alias.publish '!git push -u origin $(git branch --show-current)'
git config --global alias.track '!git branch --set-upstream-to=origin/$(git branch --show-current)'

# Cleanup aliases
git config --global alias.cleanup '!git branch --merged | grep -v "\*\|main\|develop" | xargs -n 1 git branch -d'
git config --global alias.prune-all '!git remote prune origin && git gc && git clean -df && git stash clear'

# Feature branch workflow
git config --global alias.feature '!f() { git checkout develop && git pull origin develop && git checkout -b feature/$1; }; f'
git config --global alias.hotfix '!f() { git checkout main && git pull origin main && git checkout -b hotfix/$1; }; f'
git config --global alias.release '!f() { git checkout develop && git pull origin develop && git checkout -b release/$1; }; f'

# Commit aliases
git config --global alias.cmt 'commit -m'
git config --global alias.amend 'commit --amend --no-edit'
git config --global alias.fix 'commit --fixup'
git config --global alias.squash 'rebase -i --autosquash'

# Stash aliases
git config --global alias.save 'stash push -m'
git config --global alias.pop 'stash pop'
git config --global alias.apply 'stash apply'
git config --global alias.list 'stash list'

# Conventional commit aliases
git config --global alias.feat '!f() { git commit -m "feat: $1"; }; f'
git config --global alias.fix '!f() { git commit -m "fix: $1"; }; f'
git config --global alias.docs '!f() { git commit -m "docs: $1"; }; f'
git config --global alias.style '!f() { git commit -m "style: $1"; }; f'
git config --global alias.refactor '!f() { git commit -m "refactor: $1"; }; f'
git config --global alias.test '!f() { git commit -m "test: $1"; }; f'
git config --global alias.chore '!f() { git commit -m "chore: $1"; }; f'
```

## Project-Specific Configuration

For this project, also run:

```bash
# Set up Git hooks
git config core.hooksPath .githooks

# Set up merge strategy
git config merge.ours.driver true

# Set up line ending handling
git config core.autocrlf true

# Set up default pull strategy
git config pull.rebase true
```

## VS Code Integration

Add these settings to your VS Code settings.json:

```json
{
  "git.enableCommitSigning": true,
  "git.alwaysShowStagedChangesResourceGroup": true,
  "git.confirmSync": false,
  "git.enableSmartCommit": true,
  "git.fetchOnPull": true,
  "git.pruneOnFetch": true,
  "git.rebaseWhenSync": true,
  "gitlens.advanced.messages": {
    "suppressCommitHasNoPreviousCommitWarning": false,
    "suppressCommitNotFoundWarning": false,
    "suppressFileNotUnderSourceControlWarning": false,
    "suppressGitVersionWarning": false,
    "suppressLineUncommittedWarning": false,
    "suppressNoRepositoryWarning": false
  }
}
```

## SSH Key Setup

For secure Git operations, set up SSH keys:

```bash
# Generate SSH key
ssh-keygen -t ed25519 -C "your.email@example.com"

# Add to SSH agent
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519

# Add public key to GitHub
cat ~/.ssh/id_ed25519.pub
# Copy output and add to GitHub SSH keys
```

## GPG Signing Setup

For signed commits:

```bash
# Generate GPG key
gpg --full-generate-key

# List GPG keys
gpg --list-secret-keys --keyid-format LONG

# Export public key
gpg --armor --export YOUR_KEY_ID

# Configure Git
git config --global user.signingkey YOUR_KEY_ID
git config --global commit.gpgsign true
```

## Troubleshooting

### Common Issues

1. **Line ending issues**
   ```bash
   git config core.autocrlf true  # Windows
   git config core.autocrlf input # Linux/Mac
   ```

2. **Authentication issues**
   ```bash
   git config --global credential.helper manager-core
   ```

3. **Merge conflicts**
   ```bash
   git config merge.tool vscode
   git mergetool
   ```

4. **Hook execution issues**
   ```bash
   chmod +x .githooks/*
   git config core.hooksPath .githooks
   ```

### Useful Commands

```bash
# Check configuration
git config --list

# Reset configuration
git config --unset-all alias.ALIASNAME

# Global ignore patterns
git config --global core.excludesfile ~/.gitignore_global
```
