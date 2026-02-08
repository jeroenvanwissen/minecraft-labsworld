# Task A2: Add Error Handling for Chunk Loading

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `A2`         |
| **Status**       | `[ ]`        |
| **Priority**     | High         |
| **Dependencies** | None         |
| **Branch**       | `fix/a2-chunk-loading` |

**Goal:**
Add proper error handling around chunk loading in `VillagerNpcLinkManager` to prevent silent failures when a world is unloaded or a chunk cannot be loaded.

**Current Problem:**

```kotlin
// VillagerNpcLinkManager.kt:248-251
// chunk.load(true) may fail silently if the world is unloaded
chunk.load(true)
```

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
└── npc/VillagerNpcLinkManager.kt           # MODIFY — wrap chunk loading with error handling
```

**Implementation:**

1. Validate that the world exists (is loaded) before attempting to load a chunk
2. Wrap `chunk.load()` in a `runCatching` block
3. Log a warning if chunk loading fails, including the world name and coordinates
4. Return `Result.failure()` if the chunk cannot be loaded instead of proceeding with invalid state

**Acceptance Criteria:**

- [ ] Chunk loading failures are logged with world name and coordinates
- [ ] NPC spawn gracefully fails instead of proceeding with invalid state
- [ ] World existence is validated before chunk operations
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
