# T30: VillagerNpcLinkManager — Improve Existing Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcLinkManager.kt`
**Existing test:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcLinkManagerTest.kt`
**Current coverage:** 44/122 lines (36.1%)
**Target coverage:** ~70%+

## Why This Matters
78 uncovered lines in a 122-line file. The existing test covers storage config, user name resolution, and lookup. The gaps are in `ensureNpcAt` (the main method), `ensureNpcAtWithChunkLoad`, `applyNpcRuntimeSettings`, and the chunk-loading/stale-UUID paths.

## Gaps to Fill

### `@Nested class EnsureNpcAt`
The main method has multiple code paths:
- **Path 1: Loaded NPC found by userId** — `findLoadedNpcByUserId` returns villager → teleport, persist, return spawned=false
- **Path 2: Stored UUID exists and entity is loaded** — `server.getEntity(uuid)` returns villager → teleport
- **Path 3: Stored UUID exists but entity not loaded, chunk load succeeds** — `tryLoadAndFindNpcByUuid` finds it
- **Path 4: Stored UUID is stale** — entity not found anywhere → clear mapping, spawn new
- **Path 5: No stored UUID** — spawn new NPC

Tests to write:
- `teleports existing loaded NPC to new location` — path 1
- `returns spawned=false for teleported NPC`
- `finds NPC by stored UUID when globally loaded` — path 2
- `loads chunk and finds NPC at stored location` — path 3
- `spawns new NPC when stored UUID is stale` — path 4
- `spawns new NPC when no stored data exists` — path 5
- `persists user data after spawn or teleport` — verify YAML file contents
- `applies runtime settings (AI, profession)` — verify applyNpcRuntimeSettings

### `@Nested class EnsureNpcAtWithChunkLoad`
- `loads chunk before calling ensureNpcAt` — verify chunk.load(true)
- `returns failure when world is null`
- `returns "Spawned" message when NPC was newly spawned`
- `returns "Teleported" message when NPC was teleported`

### `@Nested class FindAllLinkedVillagerNpcs`
- `returns all linked villagers across worlds`
- `excludes non-linked villagers`
- `returns empty when no linked NPCs`

## Notes
- The existing test uses real temp dirs for YAML file I/O — follow that pattern.
- Mocking `server.getEntity(UUID)`, `world.getChunkAt()`, `chunk.load()`, `chunk.entities` is needed for the chunk-loading paths.
- `Entity.plainName()` is a private extension — it's tested implicitly through the return value of `EnsureResult.npcName`.
