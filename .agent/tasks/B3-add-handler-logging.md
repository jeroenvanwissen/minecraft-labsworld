# Task B3: Add Logging to Redeem Handlers

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `B3`         |
| **Status**       | `[ ]`        |
| **Priority**     | Medium       |
| **Dependencies** | None         |
| **Branch**       | `chore/b3-handler-logging` |

**Goal:**
Add `plugin.logger.info()` calls to redeem handlers that currently execute silently, making it easier to debug why swarm/attack/aggro operations don't start.

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/
├── NpcAggroHandler.kt              # MODIFY — add info logging on execute
├── NpcAttackHandler.kt             # MODIFY — add info logging on execute
└── NpcSpawnHandler.kt              # MODIFY — add info logging on execute
```

**Implementation:**

1. Add `plugin.logger.info("Starting NPC aggro for user ${context.displayName}")` at the start of each handler's `handle()` method
2. Add result logging after the operation completes (success or failure)
3. Keep log messages concise and include the triggering user's display name

**Acceptance Criteria:**

- [ ] NpcAggroHandler logs when a swarm is triggered and its result
- [ ] NpcAttackHandler logs when an attack is triggered and its result
- [ ] NpcSpawnHandler logs when an NPC spawn is triggered and its result
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
