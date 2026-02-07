# GitHub Actions Workflows

## Test Workflow (`test.yml`)

Runs unit tests on every pull request and push to `main` branch.

### What it does:
- Checks out the code
- Sets up JDK 21
- Runs `./gradlew test`
- Publishes test results (visible in PR checks)

### Making it required:
To enforce this check before merging PRs:
1. Go to repository Settings → Branches
2. Add a branch protection rule for `main`
3. Enable "Require status checks to pass before merging"
4. Select "test" workflow

This ensures all tests must pass before a PR can be merged.
