# T07: VillagerNpcSwarmService Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSwarmService.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSwarmServiceTest.kt`
**Current coverage:** 0/43 lines (0%)
**Target coverage:** ~75%+

## Why This Matters
43 uncovered lines. Very similar structure to AttackService but simpler (no damage). Tests can follow the same pattern.

## Approach
Same as T06: mock `JavaPlugin`, `VillagerNpcLinkManager`, `BukkitScheduler`, `Player`, `Villager`.

## Tests to Write

### `@Nested class Validation`
- `fails when durationSeconds is 0` — Result.failure
- `fails when durationSeconds is negative` — Result.failure
- `returns success 0 when no NPCs found`

### `@Nested class SwarmExecution`
- `returns count of NPCs that were instructed to chase`
- `NPCs pathfind toward target` — verify `pathfinder.moveTo()` called
- `NPCs have AI enabled and removeWhenFarAway=false` — verify on each villager
- `stops when target goes offline`

### `@Nested class TaskManagement`
- `new swarm cancels previous running task`
- `stop() cancels task`
- `NPCs stop pathfinding after duration expires` — verify `pathfinder.stopPathfinding()` called
