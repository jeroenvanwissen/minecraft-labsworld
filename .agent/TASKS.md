# Project Tasks

> LabsWorld — Minecraft Paper Plugin with Twitch chat integration for NPC villagers.
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

### Wave A — Critical Fixes

_Fix correctness and reliability issues that can cause data loss or unpredictable behavior._

| Status | ID   | Task                                                                  | Priority | Dependencies |
| ------ | ---- | --------------------------------------------------------------------- | -------- | ------------ |
| `[x]`  | `A1` | [Fix Concurrent Duel Conflicts](tasks/A1-fix-duel-conflicts.md)       | Critical | None         |
| `[x]`  | `A2` | [Add Error Handling for Chunk Loading](tasks/A2-fix-chunk-loading.md)  | High     | None         |

### Wave B — Validation & Robustness

_Add missing validation, cooldowns, and logging to prevent abuse and improve debuggability._

| Status | ID   | Task                                                                      | Priority | Dependencies |
| ------ | ---- | ------------------------------------------------------------------------- | -------- | ------------ |
| `[x]`  | `B1` | [Add Cooldown for Swarm/Attack Services](tasks/B1-add-swarm-cooldown.md)  | Medium   | None         |
| `[x]`  | `B2` | [Make Duel Parameters Configurable](tasks/B2-configurable-duel-params.md) | Medium   | A1           |
| `[ ]`  | `B3` | [Add Logging to Redeem Handlers](tasks/B3-add-handler-logging.md)         | Medium   | None         |

### Wave C — Hardening

_Tighten permissions and improve error reporting for edge cases._

| Status | ID   | Task                                                                          | Priority | Dependencies |
| ------ | ---- | ----------------------------------------------------------------------------- | -------- | ------------ |
| `[x]`  | `C1` | [Add Permission Check on Duel Command](tasks/C1-duel-permission-check.md)     | High     | A1           |
| `[x]`  | `C2` | [Improve Spawn Point Error Messages](tasks/C2-spawn-point-error-messages.md)  | Medium   | None         |
| `[x]`  | `C3` | [Add Duel Accept/Timeout Flow](tasks/C3-duel-accept-timer.md)                 | Medium   | A1           |

---

## Dependency Graph

```
Wave A (all independent — start in parallel):
A1, A2

Wave B:
A1 ──> B2
B1, B3 (independent)

Wave C:
A1 ──> C1
A1 ──> C3
C2 (independent)
```

---

## Verification Commands Reference

```bash
plugin/gradlew compileKotlin        # Compile check
plugin/gradlew shadowJar            # Build JAR
plugin/gradlew test                 # Run unit tests
plugin/gradlew jacocoTestReport     # Generate coverage report
plugin/gradlew build                # Full build (test + coverage verification)
plugin/gradlew runServer            # Run test server (manual)
plugin/gradlew installPlugin        # Install to server
```

---

## Guardrails

| Rule                           | Value                              | Action                |
| ------------------------------ | ---------------------------------- | --------------------- |
| Max files changed per PR       | 8                                  | Split into subtasks   |
| Max diff lines (added+removed) | 500                                | Split into subtasks   |
| Compile check                  | `plugin/gradlew compileKotlin`     | Must pass before PR   |
| Test suite                     | `plugin/gradlew test`              | Must pass before PR   |
| Code review required           | All PRs                            | Required before merge |
| Ambiguity detected             | Missing info                       | **STOP and ask user** |

---

## Notes

- **Start with Wave A** — these are independent and can all be worked on in parallel
- Each wave unlocks the next; don't skip ahead unless dependencies are met
- Update status as work progresses (`[ ]` → `[~]` → `[x]`)
- Each task should be completed on its own feature branch and merged via PR
- See [README.md](README.md) for complete workflow guide
