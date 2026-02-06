# How to Run the Agent Task Workflow

> Guide for executing the refactoring tasks with GitHub Copilot in VS Code.

**See also:**

- [TASKS.md](TASKS.md) — All refactoring tasks with acceptance criteria
- [README.md](README.md) — Docker setup overview

---

## Quick Start (VS Code + GitHub Copilot)

You're already set up! GitHub Copilot is running in VS Code right now.

```
1. Open .agent/TASKS.md in VS Code
2. Create a branch: git checkout -b refactor/a1-npc-keys
3. In Copilot Chat, type: "Execute task A1 from this file"
4. Review the changes Copilot makes
5. Run: ./.agent/scripts/verify.sh
6. Commit and create PR
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
# Check tools
git --version          # Should be 2.30+
java -version          # Should be 21+
./gradlew --version    # Should work
gh --version           # Should be 2.0+
gh auth status         # Should be authenticated
```

---

## Workflow: Running a Single Task

### Step 1: Create Branch

```bash
git checkout main
git pull origin main
git checkout -b refactor/a1-npc-keys
```

### Step 2: Open Task File

Open `.agent/TASKS.md` in VS Code.

### Step 3: Ask Copilot to Execute

In Copilot Chat (Cmd+Shift+I or click the Copilot icon), say:

> Execute task A1 from .agent/TASKS.md. Follow the implementation steps exactly.
> Create the files and make the changes as specified in the scope.

Copilot will:

- Read the task details
- Create/modify the files listed in scope
- Apply the implementation steps

### Step 4: Review Changes

```bash
# See what changed
git diff
git status

# Check diff size (guardrails)
git diff --stat | tail -1
```

### Step 5: Verify Build

```bash
# Quick verification
./gradlew compileKotlin
./gradlew shadowJar

# Or use the script
./.agent/scripts/verify.sh
```

### Step 6: Run Task-Specific Checks

Each task has verification commands. For example, task A1:

```bash
# Should return 0 (no duplicates)
grep -r "NamespacedKey.*npc_" src/main/kotlin --include="*.kt" | grep -v "VillagerNpcKeys.kt" | wc -l
```

### Step 7: Commit and Create PR

```bash
git add -A
git commit -m "refactor(npc): centralise NamespacedKeys in VillagerNpcKeys

Task A1 from .agent/TASKS.md
- Created VillagerNpcKeys object with all NPC key definitions
- Removed duplicate key definitions from 3 files
- Added helper functions for PDC lookups"

git push -u origin refactor/a1-npc-keys
gh pr create --title "refactor(npc): centralise NamespacedKeys" --body "Completes task A1"
```

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

### Split a Large Task

```
Task D1 affects too many files. Split it into subtasks D1a, D1b, D1c following the note in the task.
```

### Check Acceptance Criteria

```
Review task A1's acceptance criteria. Are all items satisfied?
```

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

Work directly but always on branches:

```bash
git checkout main
git checkout -b refactor/a1-npc-keys
# ... work ...
# If it goes wrong:
git checkout main
git branch -D refactor/a1-npc-keys
```

### Option 3: Docker (for isolated builds)

Use Docker for verification without affecting your local setup:

```bash
cd .agent
cp .env.example .env
# Edit .env with GITHUB_TOKEN

docker-compose build
docker-compose run --rm agent

# Inside container
./gradlew compileKotlin
./gradlew shadowJar
```

---

## Test Server

```bash
# Run isolated Minecraft server with plugin
./gradlew runServer

# Server runs in run/ directory (gitignored)
# Plugin JAR auto-installed
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
git branch -D refactor/a1-npc-keys
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

### Wrong Files Modified

```bash
# See what changed
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

## Task Dependency Order

Run tasks respecting dependencies:

**Phase A (Foundation):**

- A0 first (rename Npc* → VillagerNpc*)
- A1, A3 (both depend on A0)
- A2 (depends on A1)

**Phase B (NPC Layer):**

- B1 (depends on A1) → B2, B3 (depend on B1)

**Phase C (Deduplication):**

- C1, C2, C3 (all independent)

**Phase D (Twitch Layer):**

- D1 → D2 → D3
- D1 → D4

**Phase E (Action System):**

- D1 → E1 → E2 → E3 → E4 → E5

**Phase F (Polish):**

- F1 (depends on B1)
- F2 (independent)

---

## Quick Reference

| Action        | Command                                          |
| ------------- | ------------------------------------------------ |
| Create branch | `git checkout -b refactor/<task>`                |
| Ask Copilot   | _"Execute task X from .agent/TASKS.md"_          |
| Verify build  | `./gradlew compileKotlin && ./gradlew shadowJar` |
| Check diff    | `git diff --stat`                                |
| Create PR     | `gh pr create --fill`                            |
| Reset changes | `git checkout .`                                 |
| Delete branch | `git checkout main && git branch -D <branch>`    |

---

## Security Notes

1. **Never commit secrets** — Keep `twitch.config.yml` credentials safe
2. **Review before push** — Always `git diff` before committing
3. **Use branches** — Never commit directly to main
4. **Test on dev server** — Use `./gradlew runServer`, not production
5. **Small PRs** — Easier to review and revert
