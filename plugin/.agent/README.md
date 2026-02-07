# Agent Environment for Refactoring

> Complete guide for executing systematic refactoring tasks with GitHub Copilot in VS Code.

---

## Contents

| File                 | Purpose                                     |
| -------------------- | ------------------------------------------- |
| [TASKS.md](TASKS.md) | Task template and current refactoring tasks |
| [scripts/](scripts/) | Helper scripts for verification             |

---

## Quick Start

```
1. Open .agent/TASKS.md in VS Code
2. Create a branch: git checkout -b refactor/<task-id>
3. In Copilot Chat: "Execute task <ID> from .agent/TASKS.md"
4. Review the changes Copilot makes
5. Run: ./gradlew compileKotlin && ./gradlew test
6. Commit: git add -A && git commit -m "descriptive message"
7. Push: git push -u origin <branch-name>
8. Create PR: gh pr create --title "..." --body "..."
9. Mark task as completed in TASKS.md
```

---

## Prerequisites

### Required Tools

| Tool               | Version   | Purpose                   | Install                                                |
| ------------------ | --------- | ------------------------- | ------------------------------------------------------ |
| **Git**            | 2.30+     | Version control           | `brew install git`                                     |
| **JDK**            | 21        | Kotlin/Gradle compilation | `brew install openjdk@21`                              |
| **Gradle**         | (wrapper) | Build system              | Included via `./gradlew`                               |
| **VS Code**        | Latest    | Editor with Copilot       | [code.visualstudio.com](https://code.visualstudio.com) |
| **GitHub Copilot** | Extension | AI assistant              | VS Code marketplace                                    |
| **GitHub CLI**     | 2.0+      | Create PRs from terminal  | `brew install gh`                                      |

### Verify Setup

```bash
git --version          # Should be 2.30+
java -version          # Should be 21+
./gradlew --version    # Should work
gh --version           # Should be 2.0+
gh auth status         # Should be authenticated
```

---

## Complete Workflow

### Step 1: Create Branch

```bash
git checkout main
git pull origin main
git checkout -b refactor/<task-id>-<short-name>
```

**Branch naming examples:**
- `refactor/a1-npc-keys`
- `refactor/b2-duel-service`
- `refactor/e4-action-registry`

### Step 2: Open Task File

Open `.agent/TASKS.md` in VS Code and find your task.

### Step 3: Ask Copilot to Execute

In Copilot Chat (Cmd+Shift+I or click the Copilot icon):

> Execute task A1 from .agent/TASKS.md. Follow the implementation steps exactly.
> Create the files and make the changes as specified in the scope.

Copilot will:
- Read the task details
- Create/modify the files listed in scope
- Apply the implementation steps

### Step 4: Review Changes

```bash
git diff
git status
git diff --stat | tail -1  # Check diff size (guardrails)
```

### Step 5: Verify Build

```bash
./gradlew compileKotlin
./gradlew shadowJar
./gradlew test
```

Or use the verification script:

```bash
./.agent/scripts/verify.sh
```

### Step 6: Run Task-Specific Checks

Each task may have verification commands. Example:

```bash
grep -r "NamespacedKey.*npc_" src/main/kotlin --include="*.kt" | grep -v "VillagerNpcKeys.kt" | wc -l
# Should return 0 (no duplicates)
```

### Step 7: Commit Changes

Use this commit message format:

```
<type>(<scope>): <subject>

<body>

Task <ID> from .agent/TASKS.md
- Bullet point summary of changes
- One line per major change
- Focus on "what" and "why"
```

**Commit types:**
- `refactor` — Code restructuring
- `feat` — New feature
- `fix` — Bug fix
- `test` — Adding tests
- `docs` — Documentation

**Example commit:**

```bash
git add -A
git commit -m "refactor(npc): centralize NamespacedKeys in VillagerNpcKeys

Task A1 from .agent/TASKS.md
- Created VillagerNpcKeys object with all NPC key definitions
- Removed duplicate key definitions from 3 files
- Added helper functions for PDC lookups"
```

### Step 8: Push and Create PR

```bash
git push -u origin refactor/<task-id>-<short-name>

gh pr create \
  --title "refactor(scope): brief description" \
  --body "Completes task A1 from .agent/TASKS.md

## Changes
- List of changes made
- Why these changes were necessary

## Verification
- [x] ./gradlew compileKotlin passes
- [x] ./gradlew test passes
- [x] All acceptance criteria met"
```

### Step 9: Wait for Code Review

All PRs require code review before merging.

### Step 10: Update Task Status

After PR is merged, mark the task as `[x]` completed in TASKS.md.

---

## Copilot Chat Prompts

### Execute a Task

```
Execute task A1 from .agent/TASKS.md. Follow the implementation steps exactly.
```

### Fix a Build Error

```
The build failed with this error: [paste error]. Fix it while staying within the task scope.
```

### Check Acceptance Criteria

```
Review task A1's acceptance criteria. Are all items satisfied?
```

### Split a Large Task

```
Task D1 affects too many files. Split it into subtasks D1a, D1b, D1c.
```

---

## Sandbox Options

### Option 1: Git Worktrees (Recommended)

Keep your main branch clean:

```bash
cd /Users/jeroen/Projects/MineCraft/minecraft-labsworld/plugin
git checkout main
git pull origin main

# Create isolated worktree
git worktree add ../plugin-agent-work main
cd ../plugin-agent-work

# Open in new VS Code window
code .
```

To reset:

```bash
git worktree remove ../plugin-agent-work --force
git worktree add ../plugin-agent-work main
```

### Option 2: Branch-Only (Simplest)

Work directly on feature branches:

```bash
git checkout main
git checkout -b refactor/a1-task-name
# ... work ...
# If it goes wrong:
git checkout main
git branch -D refactor/a1-task-name
```

---

## Test Server

Run an isolated Minecraft server with your plugin:

```bash
./gradlew runServer

# Server runs in run/ directory (gitignored)
# Plugin JAR auto-installed
# Press Ctrl+C to stop
```

---

## Troubleshooting

### Copilot Made Too Many Changes

```bash
git diff --stat
# If > 8 files or > 500 lines:
git checkout .
# Ask Copilot to focus on specific files
```

### Compile Error

```bash
./gradlew compileKotlin 2>&1 | head -30
# Paste error to Copilot Chat and ask for fix
```

### Test Failures

```bash
./gradlew test --tests "*FailingTest"
# Investigate and fix, don't skip tests
```

### Wrong Files Modified

```bash
git diff --name-only
# Reset specific file
git checkout -- src/main/kotlin/path/to/File.kt
# Or reset all
git checkout .
```

### Merge Conflicts

```bash
git fetch origin
git rebase origin/main
# Resolve conflicts
git add -A
git rebase --continue
```

---

## Rollback Procedures

### Undo Last Commit (Before Push)

```bash
git reset --soft HEAD~1  # Keep changes staged
git reset --hard HEAD~1  # Discard changes
```

### Discard All Changes

```bash
git checkout .
git clean -fd  # Remove untracked files
```

### Abandon a Branch

```bash
git checkout main
git branch -D refactor/task-id
```

---

## Guardrails

Follow these constraints to maintain quality:

| Rule                           | Value                         | Action on Violation       |
| ------------------------------ | ----------------------------- | ------------------------- |
| Max files changed per PR       | 8                             | Split into subtasks       |
| Max diff lines (added+removed) | 500                           | Split into subtasks       |
| Compile check                  | `./gradlew compileKotlin`     | Must pass before PR       |
| Test suite                     | `./gradlew test`              | Must pass before PR       |
| Plugin load test               | `./gradlew runServer`         | Recommended               |
| Code review required           | All PRs                       | Required before merge     |
| Ambiguity detected             | Missing info, unclear req     | **STOP and ask user**     |

### Stop-and-Ask Triggers

The agent MUST stop and ask the user when:

- A file referenced in scope does not exist
- A method/class to modify cannot be found
- The change would affect more files than the scope specifies
- A dependency task is marked "blocked" or "needs-discussion"
- Test failures occur that aren't obviously related to the change
- The implementation approach has multiple valid options with trade-offs

---

## Best Practices

**Do:**
- ✅ Keep PRs small and focused (≤8 files, ≤500 lines)
- ✅ Run verification before every commit
- ✅ Write clear commit messages
- ✅ Create descriptive PR descriptions
- ✅ Update task status immediately
- ✅ Use feature branches, never commit to main
- ✅ Get code review before merging

**Don't:**
- ❌ Create "mega PRs" touching 20+ files
- ❌ Mix unrelated changes in one PR
- ❌ Skip verification steps
- ❌ Merge with failing tests
- ❌ Commit secrets or credentials

**Task Definition:**
- Keep tasks small and focused (1 task = 1 PR)
- Define clear acceptance criteria
- List all files that will be modified
- Specify dependencies between tasks
- Include verification commands

**Execution:**
- Follow strict guardrails (max 8 files, 500 lines per PR)
- Run verification before every commit
- Create descriptive PR titles and descriptions
- Get code review before merging
- Update task status immediately after completion

**Quality:**
- Write tests for new functionality
- Maintain existing test coverage
- Document non-obvious changes
- Keep commits focused and atomic
- Use meaningful commit messages

---

## Helper Scripts

Located in `.agent/scripts/`:

```bash
# Pre-flight check (clean state, build works)
./.agent/scripts/preflight.sh

# Verify build + guardrails after changes
./.agent/scripts/verify.sh
```

---

## Security

1. **Never commit secrets** — Keep `twitch.config.yml` credentials safe
2. **Review before push** — Always `git diff` before committing
3. **Use branches** — Never commit directly to main
4. **Test on dev server** — Use `./gradlew runServer`, not production
5. **Small PRs** — Easier to review and revert
6. **Feature branches** — All work uses feature branches with PR reviews

---

## Getting Started

1. **Review [TASKS.md](TASKS.md)** to see the task template
2. **Define your first task** in TASKS.md following the example
3. **Create a branch** with `git checkout -b refactor/<task-id>`
4. **Ask Copilot** to execute the task
5. **Run verification** with `./gradlew compileKotlin && ./gradlew test`
6. **Commit and push** following the commit message format
7. **Create a PR** with `gh pr create`
8. **Mark task as completed** when PR is merged
9. **Repeat** for each improvement

---

## Quick Reference

| Action              | Command                                          |
| ------------------- | ------------------------------------------------ |
| Create branch       | `git checkout -b refactor/<task>`                |
| Ask Copilot         | _"Execute task X from .agent/TASKS.md"_          |
| Verify build        | `./gradlew compileKotlin && ./gradlew test`      |
| Commit              | `git add -A && git commit -m "message"`          |
| Push                | `git push -u origin <branch-name>`               |
| Create PR           | `gh pr create --title "..." --body "..."`        |
| Reset changes       | `git checkout .`                                 |
| Delete branch       | `git checkout main && git branch -D <branch>`    |
| Compile check       | `./gradlew compileKotlin`                        |
| Build JAR           | `./gradlew shadowJar`                            |
| Run unit tests      | `./gradlew test`                                 |
| Run test server     | `./gradlew runServer`                            |
| Full build          | `./gradlew build`                                |
| Install to server   | `./gradlew installPlugin`                        |

---

## Tips

- Start with small, focused tasks to build confidence
- Use dependency graphs when planning multiple tasks
- Keep task scope tight (≤8 files, ≤500 lines)
- Run verification often to catch issues early
- Archive completed phases to keep TASKS.md manageable
- Reference git commit history for implementation details
- Use git worktrees to keep your main branch clean
- Test on `./gradlew runServer` before creating PR
