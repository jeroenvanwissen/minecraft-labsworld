# T24: FireworksActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/FireworksActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/FireworksActionHandlerTest.kt`
**Current coverage:** 2/20 lines (10%)
**Target coverage:** ~75%+

## Why This Matters
18 uncovered lines. The most feature-rich action handler with multiple params.

## Approach
- Mock `ActionUtils.resolveTargetPlayer` via `mockkObject`
- Mock `Player`, `World`, `Firework`, `FireworkMeta`
- Mock `World.spawn(Location, Firework::class.java)` to return mock Firework

## Tests to Write

### `@Nested class Properties`
- `type is "player.fireworks"`

### `@Nested class Handle`
- `returns early when no target player` — resolveTargetPlayer returns null
- `spawns firework at player location` — verify world.spawn called
- `respects count param` — count=3, verify spawn called 3 times
- `coerces count to at least 1` — count=0 → 1 firework
- `respects power param` — power=2, verify meta.power = 2
- `coerces power to range 0-2`
- `uses specified shape` — shape="star", verify FireworkEffect.Type.STAR
- `defaults to ball shape` — no shape param
- `uses specified colors` — colors=["RED","BLUE"]
- `defaults to white when no colors`
- `applies random offset to spawn location`
