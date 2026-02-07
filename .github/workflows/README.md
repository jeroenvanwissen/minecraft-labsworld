# GitHub Actions Workflows

## Test Workflow (`test.yml`)

Runs unit tests on every pull request and push to `main` branch.

### What it does:
- Checks out the code
- Sets up JDK 21
- Runs `plugin/gradlew test` in the plugin directory
- Publishes test results (visible in PR checks)

### Permissions:
The workflow requires these permissions to publish test results:
- `contents: read` - Read repository contents
- `checks: write` - Create/update check runs with test results
- `pull-requests: write` - Comment on PRs with test summaries

### Making it required:
To enforce this check before merging PRs:

1. Go to your repository on GitHub
2. Click **Settings** (top menu)
3. Click **Branches** (left sidebar under "Code and automation")
4. Click **Add branch protection rule** (or edit existing rule for `main`)
5. In "Branch name pattern", enter: `main`
6. Enable these options:
   - ✅ **Require a pull request before merging**
   - ✅ **Require status checks to pass before merging**
7. In the search box that appears, type: `test`
8. Select **test** from the dropdown (this is the job name from the workflow)
9. Scroll down and click **Create** (or **Save changes**)

**Note:** The workflow must run at least once before the `test` check appears in the search results. If you don't see it, merge a PR or push to main first.

This ensures all tests must pass before a PR can be merged.
