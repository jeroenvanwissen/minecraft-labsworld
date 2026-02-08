# T05: VillagerNpcSpawnPointManager Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSpawnPointManager.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSpawnPointManagerTest.kt`
**Current coverage:** 0/71 lines (0%) + 0/6 SpawnPointKey + 0/7 SpawnPointKey.Companion
**Target coverage:** ~70%+

## Why This Matters
71+ lines with 0% coverage. Manages spawn point persistence, item creation, and location picking — all critical game mechanics.

## Approach
- Mock `JavaPlugin` with a real temp directory for `dataFolder`
- `SpawnPointKey` and `SpawnPointKey.fromString()` are pure data — test directly
- Item creation requires `Material` and `ItemStack` which need Bukkit statics — mock or test indirectly
- File I/O (`load`/`save`) uses real YAML files — use temp dirs (existing pattern)

## Tests to Write

### `@Nested class SpawnPointKey`
- `toString produces correct format` — "uuid:x:y:z"
- `fromString parses valid key` — round-trip with toString
- `fromString returns null for wrong part count` — "uuid:x:y" → null
- `fromString returns null for invalid UUID` — "notauuid:1:2:3" → null
- `fromString returns null for non-integer coords` — "uuid:a:2:3" → null

### `@Nested class InitAndPersistence`
- `init creates empty file when none exists` — verify file created with empty spawn_points
- `init loads existing spawn points from file` — pre-write YAML, verify points loaded
- `save persists spawn points sorted` — add multiple points, verify YAML content

### `@Nested class SpawnPointLocations`
- `getSpawnPointLocations returns empty when no points`
- `getSpawnPointLocations resolves world UUIDs to locations` — mock `Bukkit.getWorld()`
- `getSpawnPointLocations skips unloaded worlds` — `Bukkit.getWorld()` returns null

### `@Nested class PickSpawnLocation`
- `returns null when no spawn points exist`
- `returns location on top of marker block` — verify +0.5x, +1y, +0.5z offset
- `picks deterministically (sorted)` — add multiple, verify first in sort order chosen

### `@Nested class CanUseSpawnPoints`
- `returns true for player with labsworld.admin permission`
- `returns true for player with labsworld.npcspawnpoint permission`
- `returns false for player without permissions`

### `@Nested class ReconcileStoredSpawnPoints`
- `removes spawn points where block is no longer a spawn point` — mock world/block, `isSpawnPointBlock` returns false
- `keeps valid spawn points`

## Notes
- `createSpawnPointItem` requires `ItemStack` constructor and `Material.PLAYER_HEAD` — these are Bukkit statics. If they can't be mocked, note for integration tests.
- `markPlacedBlock` requires `TileState` — mock `Block.getState()` returning a mock `TileState`.
- `isSpawnPointBlock` has a TileState path and a fallback path — test both.
