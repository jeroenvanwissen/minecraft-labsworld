# T06: VillagerNpcAttackService Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcAttackService.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcAttackServiceTest.kt`
**Current coverage:** 0/61 lines (0%)
**Target coverage:** ~70%+

## Why This Matters
61 uncovered lines. Contains validation, task scheduling, and damage application logic — all testable with mocked scheduler.

## Approach
- Mock `JavaPlugin`, `VillagerNpcLinkManager`, `BukkitScheduler`
- Mock `Player` target (with `isOnline`, `location`, `world`)
- Mock `Villager` NPCs (with `isValid`, `location`, `world`, `pathfinder`, `uniqueId`)
- Use scheduler mock that captures and runs the Runnable (existing pattern for `runTaskTimer`)

## Tests to Write

### `@Nested class Validation`
- `fails when durationSeconds is 0` — Result.failure
- `fails when durationSeconds is negative` — Result.failure
- `fails when damageHeartsPerHit is 0` — Result.failure
- `fails when damageHeartsPerHit is negative` — Result.failure
- `fails when hitCooldownMs is 0` — Result.failure
- `returns success 0 when no NPCs found` — `findAllLinkedVillagerNpcs` returns empty list

### `@Nested class AttackExecution`
- `returns count of NPCs` — 3 NPCs found, Result.success(3)
- `NPCs pathfind toward target` — verify `pathfinder.moveTo()` called
- `NPC in range deals damage to player` — mock locations close together, verify `target.damage()` called
- `NPC out of range does not deal damage` — mock locations far apart, verify no damage
- `hit cooldown prevents repeated damage` — two ticks, verify damage only once within cooldown
- `stops when target goes offline` — `isOnline` returns false, verify task cancelled

### `@Nested class TaskManagement`
- `new attack cancels previous running task` — start two attacks, verify first task cancelled
- `stop() cancels task and clears hit map` — call stop, verify
- `task is cancelled after duration expires` — verify `runTaskLater` schedules stop

## Notes
- `runTaskTimer` returns a mock `BukkitTask`. Capture the Runnable and invoke it manually to simulate ticks.
- `distanceSquared` on mocked locations needs careful setup — use `every { loc1.distanceSquared(loc2) } returns X`.
