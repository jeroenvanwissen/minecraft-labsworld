# T26: SpawnMobActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/SpawnMobActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/SpawnMobActionHandlerTest.kt`
**Current coverage:** 2/13 lines (15.4%)
**Target coverage:** ~80%+

## Why This Matters
11 uncovered lines. Mob spawning with validation.

## Approach
- Mock `ActionUtils` (resolveTargetPlayer, parseEntityType)
- Mock `Player`, `World`, `EntityType`

## Tests to Write

### `@Nested class Properties`
- `type is "player.spawn_mob"`

### `@Nested class Handle`
- `returns early when no target player`
- `errors when mob param missing` — "Missing mob type"
- `errors when mob type unrecognized` — parseEntityType returns null → "Unknown mob type"
- `skips non-spawnable entity types` — `isSpawnable = false`, no spawn call
- `skips non-alive entity types` — `isAlive = false`, no spawn call
- `spawns correct count of mobs` — count=5, verify spawnEntity called 5 times
- `coerces count to at least 1`
- `applies random offset based on radius param`
- `uses default radius of 2.0 when not specified`
