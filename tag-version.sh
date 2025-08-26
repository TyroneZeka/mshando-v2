#!/bin/bash

# Version tagging and release script for Mshando project
# Usage: ./tag-version.sh [major|minor|patch|version]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    print_error "Not in a git repository"
    exit 1
fi

# Check if we're on main or develop branch
current_branch=$(git branch --show-current)
if [[ "$current_branch" != "main" && "$current_branch" != "develop" ]]; then
    print_error "Must be on main or develop branch to create a tag"
    exit 1
fi

# Check if working directory is clean
if ! git diff-index --quiet HEAD --; then
    print_error "Working directory is not clean. Please commit or stash your changes."
    exit 1
fi

# Get the latest tag
latest_tag=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")
print_info "Latest tag: $latest_tag"

# Extract version numbers
if [[ $latest_tag =~ v([0-9]+)\.([0-9]+)\.([0-9]+) ]]; then
    major=${BASH_REMATCH[1]}
    minor=${BASH_REMATCH[2]}
    patch=${BASH_REMATCH[3]}
else
    major=0
    minor=0
    patch=0
fi

# Determine the increment type
increment_type=${1:-patch}

case $increment_type in
    major)
        new_major=$((major + 1))
        new_minor=0
        new_patch=0
        ;;
    minor)
        new_major=$major
        new_minor=$((minor + 1))
        new_patch=0
        ;;
    patch)
        new_major=$major
        new_minor=$minor
        new_patch=$((patch + 1))
        ;;
    v*)
        # Custom version provided
        if [[ $increment_type =~ v([0-9]+)\.([0-9]+)\.([0-9]+) ]]; then
            new_major=${BASH_REMATCH[1]}
            new_minor=${BASH_REMATCH[2]}
            new_patch=${BASH_REMATCH[3]}
        else
            print_error "Invalid version format. Use vX.Y.Z"
            exit 1
        fi
        ;;
    *)
        print_error "Invalid increment type. Use: major, minor, patch, or vX.Y.Z"
        exit 1
        ;;
esac

new_version="v${new_major}.${new_minor}.${new_patch}"

print_info "Current version: $latest_tag"
print_info "New version: $new_version"

# Confirm with user
read -p "Create tag $new_version? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Aborted"
    exit 0
fi

# Generate changelog since last tag
print_info "Generating changelog since $latest_tag..."
changelog=$(git log --pretty=format:"- %s" $latest_tag..HEAD 2>/dev/null || git log --pretty=format:"- %s")

# Create tag with changelog
tag_message="Release $new_version

Changes since $latest_tag:
$changelog"

print_info "Creating tag..."
git tag -a "$new_version" -m "$tag_message"

print_success "Tag $new_version created successfully!"

# Ask if user wants to push
read -p "Push tag to origin? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Pushing tag to origin..."
    git push origin "$new_version"
    print_success "Tag pushed to origin!"
else
    print_warning "Tag created locally but not pushed to origin."
    print_info "To push later, run: git push origin $new_version"
fi

print_success "Version tagging completed!"
print_info "Next steps:"
echo "  1. Create a release on GitHub if needed"
echo "  2. Update version in application files if needed"
echo "  3. Deploy to appropriate environments"
