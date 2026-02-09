# Task A1: Fix Concurrent Duel Conflicts

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `A1`         |
| **Status**       | `[x]`        |
| **Priority**     | Critical     |
| **Dependencies** | None         |
| **Branch**       | `fix/a1-duel-conflicts` |

**Goal:**
Prevent multiple duel requests from starting simultaneously. Currently, `VillagerNpcDuelService.duelTask` is a single instance that gets overwritten when a new duel starts while one is already running, leading to unpredictable behavior.

**Current Problem:**

```kotlin
// VillagerNpcDuelService.kt:48
// duelTask is a single BukkitTask — a new duel overwrites the reference,
// leaving the previous duel loop running without a way to cancel it.
private var duelTask: BukkitTask? = null
```

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcDuelService.kt           # MODIFY — add active duel check, reject/queue new duels
└── twitch/commands/lw/DuelSubcommand.kt    # MODIFY — handle rejection response to chat

plugin/src/test/kotlin/nl/jeroenlabs/labsWorld/
└── npc/VillagerNpcDuelServiceTest.kt       # CREATE — test concurrent duel rejection
```

**Implementation:**

1. Add an `isActive` property to `VillagerNpcDuelService` that checks if `duelTask` is non-null and not cancelled
2. In `startDuel()`, check `isActive` first and return `Result.failure()` with a descriptive message if a duel is already running
3. Update `DuelSubcommand` to handle the failure result and send a chat message (e.g., "A duel is already in progress!")
4. Add unit tests verifying that a second duel request is rejected while one is active

**Acceptance Criteria:**

- [x] Starting a duel while one is running returns a failure result
- [x] The chat user receives feedback that a duel is already in progress
- [x] The first duel continues uninterrupted when a second is requested
- [x] Build passes: `plugin/gradlew compileKotlin`
- [x] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
