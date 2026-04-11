# Git Flow Workflow

This project follows the Git Flow branching model for better project management and release control.

## Branch Structure

### Main Branches

- **main** - Production-ready code. Only receives merges from release and hotfix branches.
- **develop** - Integration branch for features. This is the base for feature branches.

### Supporting Branches

- **feature/** - Feature branches for new functionality
  - Naming: `feature/feature-name`
  - Created from: `develop`
  - Merged back into: `develop`
  - Example: `feature/user-authentication`, `feature/appointment-booking`

- **release/** - Release preparation branches
  - Naming: `release/v1.0.0`
  - Created from: `develop`
  - Merged into: `main` and back to `develop`
  - Used for: Version bumps, release notes, bug fixes

- **hotfix/** - Emergency fixes for production issues
  - Naming: `hotfix/issue-description`
  - Created from: `main`
  - Merged into: `main` and `develop`
  - Example: `hotfix/login-crash-fix`

## Workflow

### Starting a New Feature

```bash
# Update develop branch
git checkout develop
git pull origin develop

# Create feature branch
git checkout -b feature/your-feature-name

# Work on your feature
# ... make commits ...

# Push to remote
git push origin feature/your-feature-name
```

### Finishing a Feature

```bash
# Ensure feature is up to date
git pull origin develop

# Switch to develop
git checkout develop

# Merge feature branch
git merge --no-ff feature/your-feature-name

# Push to remote
git push origin develop

# Delete feature branch
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

### Creating a Release

```bash
# Create release branch from develop
git checkout -b release/v1.0.0 develop

# Update version numbers, changelog, etc.
# ... make commits ...

# Merge into main
git checkout main
git merge --no-ff release/v1.0.0

# Tag the release
git tag -a v1.0.0 -m "Release version 1.0.0"

# Merge back into develop
git checkout develop
git merge --no-ff release/v1.0.0

# Push everything
git push origin main develop
git push origin v1.0.0

# Delete release branch
git branch -d release/v1.0.0
git push origin --delete release/v1.0.0
```

### Creating a Hotfix

```bash
# Create hotfix branch from main
git checkout -b hotfix/issue-fix main

# Fix the issue
# ... make commits ...

# Merge into main
git checkout main
git merge --no-ff hotfix/issue-fix

# Tag the hotfix
git tag -a v1.0.1 -m "Hotfix version 1.0.1"

# Merge into develop
git checkout develop
git merge --no-ff hotfix/issue-fix

# Push everything
git push origin main develop
git push origin v1.0.1

# Delete hotfix branch
git branch -d hotfix/issue-fix
git push origin --delete hotfix/issue-fix
```

## Commit Message Guidelines

Use clear, descriptive commit messages:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code refactoring without feature changes
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **chore**: Build process, dependencies, tooling

### Examples
```
feat(auth): add user login functionality
fix(appointments): resolve booking validation error
docs(readme): update installation instructions
chore(deps): update react to v18.2.0
```

## Best Practices

1. **Keep branches focused** - One feature per branch
2. **Use descriptive names** - Branch names should clearly indicate purpose
3. **Regular commits** - Commit frequently with meaningful messages
4. **Pull before push** - Always pull latest changes before pushing
5. **Code review** - Use pull requests for code review before merging
6. **Delete merged branches** - Clean up branches after merging
7. **Never force push** - Avoid `git push --force` on shared branches
8. **Sync regularly** - Keep feature branches updated with develop

## Current Status

- **main**: Production branch (protected)
- **develop**: Development integration branch (protected)

All feature work should branch from `develop` and be merged back via pull requests.
