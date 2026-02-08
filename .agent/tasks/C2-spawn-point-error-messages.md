# Task C2: Improve Spawn Point Error Messages

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `C2`         |
| **Status**       | `[ ]`        |
| **Priority**     | Medium       |
| **Dependencies** | None         |
| **Branch**       | `fix/c2-spawn-error-messages` |

**Goal:**
Add specific error messages when NPC spawn operations fail due to missing spawn points, instead of returning generic null failures.

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
└── npc/VillagerNpcSpawnPointManager.kt     # MODIFY — return descriptive error in Result
```

**Implementation:**

1. When `pickSpawnLocation()` returns null because no spawn points exist, return a `Result.failure()` with a descriptive message
2. When spawn points exist but none are in a valid world, return a specific error message
3. Log the failure reason at warning level

**Acceptance Criteria:**

- [ ] "No spawn points configured" error when spawn point list is empty
- [ ] "No spawn points in valid worlds" error when worlds are unloaded
- [ ] Error messages are logged and propagated to calling code
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
