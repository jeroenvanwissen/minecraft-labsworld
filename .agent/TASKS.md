# Refactoring Tasks

> Define and track systematic code refactoring tasks.
> Each task = 1 PR with clear acceptance criteria.

---

## Task Status Legend

| Status | Meaning                    |
| ------ | -------------------------- |
| `[ ]`  | not-started                |
| `[~]`  | in-progress                |
| `[x]`  | completed                  |
| `[!]`  | blocked (needs discussion) |

---

## Current Tasks

### Phase A — Example Phase

_Add your tasks here following the template below:_

#### Task A1: Example Task

| Field            | Value                   |
| ---------------- | ----------------------- |
| **ID**           | `A1`                    |
| **Status**       | `[ ]`                   |
| **Dependencies** | None                    |
| **Branch**       | `refactor/a1-task-name` |

**Goal:**
Brief description of what this task aims to achieve.

**Scope:**

```
src/main/kotlin/path/to/
├── FileToCreate.kt     # CREATE
├── FileToModify.kt     # MODIFY
└── FileToDelete.kt     # DELETE
```

**Implementation:**

1. Step one
2. Step two
3. Step three

**Acceptance Criteria:**

- [ ] Criterion one
- [ ] Criterion two
- [ ] Criterion three
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
# Add task-specific verification commands
```

---

## Verification Commands Reference

```bash
plugin/gradlew compileKotlin        # Compile check
plugin/gradlew shadowJar            # Build JAR
plugin/gradlew test                 # Run unit tests
plugin/gradlew runServer            # Run test server (manual)
plugin/gradlew build                # Full build
plugin/gradlew installPlugin        # Install to server
```

---

## Creating Dependency Graphs

When planning multiple tasks, create a visual dependency graph:

```
A1 ──> A2 ──> B1
       │
       └──> B2

C1, C2 ──> (independent, can run parallel)
```

This helps identify:
- Which tasks can run in parallel
- Which tasks block others
- The critical path through the work
- Potential merge conflict zones

---

## Guardrails

| Rule                           | Value                         | Action                  |
| ------------------------------ | ----------------------------- | ----------------------- |
| Max files changed per PR       | 8                             | Split into subtasks     |
| Max diff lines (added+removed) | 500                           | Split into subtasks     |
| Compile check                  | `plugin/gradlew compileKotlin`     | Must pass before PR     |
| Test suite                     | `plugin/gradlew test`              | Must pass before PR     |
| Code review required           | All PRs                       | Required before merge   |
| Ambiguity detected             | Missing info, unclear req     | **STOP and ask user**   |

---

## Notes

- Use the task template above for each new task
- Update status as work progresses (`[ ]` → `[~]` → `[x]`)
- Archive completed phases to keep file manageable
- See [README.md](README.md) for complete workflow guide
