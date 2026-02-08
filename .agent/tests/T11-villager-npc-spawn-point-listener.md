# T11: VillagerNpcSpawnPointListener Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSpawnPointListener.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSpawnPointListenerTest.kt`
**Current coverage:** 0/30 lines (0%)
**Target coverage:** ~80%+

## Why This Matters
30 uncovered lines. Event listener with clear permission checks and block management — very testable.

## Approach
- Mock `VillagerNpcSpawnPointManager`
- Mock Bukkit events: `BlockPlaceEvent`, `BlockBreakEvent`, `EntityExplodeEvent`, `BlockExplodeEvent`
- Mock `Player`, `Block`, `ItemStack`, `World`, `Location`

## Tests to Write

### `@Nested class OnPlace`
- `does nothing when item is not a spawn point` — `isSpawnPointItem` returns false
- `cancels event when player lacks permission` — verify `event.isCancelled = true`, player gets message
- `marks block when player has permission` — verify `markPlacedBlock` called
- `cancels event when markPlacedBlock throws` — exception from manager, verify cancelled + error message

### `@Nested class OnBreak`
- `does nothing when block is not a spawn point` — `isSpawnPointBlock` returns false
- `cancels event when player lacks permission` — verify cancelled + message
- `unmarks block and drops item when player has permission` — verify:
  - `event.isDropItems = false`
  - `event.expToDrop = 0`
  - `unmarkBrokenBlock` called
  - `world.dropItemNaturally` called with spawn point item

### `@Nested class ExplosionProtection`
- `onEntityExplode removes spawn point blocks from blast list` — add spawn point block to `blockList()`, verify removed
- `onEntityExplode keeps non-spawn-point blocks` — verify non-spawn blocks remain
- `onBlockExplode removes spawn point blocks from blast list`
