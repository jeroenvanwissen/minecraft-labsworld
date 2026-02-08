# T02: VillagerNpcDuelService Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcDuelService.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcDuelServiceTest.kt`
**Current coverage:** 0/117 lines (0%)
**Target coverage:** ~65%+ of testable lines

## Why This Matters
117 uncovered lines — the second largest gap. The duel logic has many branches (hit/miss, winner/loser, respawn) that are ideal for unit testing.

## Approach
- Mock `JavaPlugin`, `VillagerNpcLinkManager`, `VillagerNpcSpawnPointManager`
- Mock `BukkitScheduler` to execute runnables synchronously (existing pattern)
- Mock `Villager` entities with controllable `isValid`, `location`, `uniqueId`
- Control `Random` outcomes by mocking or seeding

## Tests to Write

### `@Nested class StartDuelValidation`
- `fails when both user IDs are the same` — Result.failure with "Cannot duel same user"
- `fails when no spawn points are placed` — `pickSpawnLocation` returns null
- `fails when NPC A cannot be found after ensure` — `findLoadedNpcByUserId` returns null for A
- `fails when NPC B cannot be found after ensure` — `findLoadedNpcByUserId` returns null for B

### `@Nested class DuelExecution`
- `cancels previous duel when starting a new one` — start two duels, verify first task cancelled
- `NPCs are made non-invulnerable during duel` — verify `isInvulnerable = false` on both NPCs
- `NPCs pathfind toward each other` — verify `pathfinder.moveTo()` called
- `duel ends when NPC A HP reaches 0` — mock Random to always let B hit A, verify announce with B as winner
- `duel ends when NPC B HP reaches 0` — mock Random to always let A hit B, verify announce with A as winner
- `loser NPC is removed` — verify `loserNpc.remove()` called
- `winner NPC is restored to invulnerable` — verify `isInvulnerable = true`

### `@Nested class RespawnAfterDuel`
- `loser NPC respawns after delay` — verify `ensureNpcAt` called for loser via runTaskLater
- `winner NPC is teleported back to spawn` — verify teleport called on winner

### `@Nested class AnnounceCallback`
- `announces duel start` — verify announce called with "vs ... duel begins!"
- `announces winner` — verify announce called with "wins!"
- `announces respawn` — verify announce called with "respawned"

## Notes
- The `runTaskTimer` and `runTaskLater` need careful mocking. Use `answers` blocks that capture and immediately invoke the Runnable.
- For hit/miss testing, `mockkObject(Random)` or use `mockkStatic` for `kotlin.random.Random`.
