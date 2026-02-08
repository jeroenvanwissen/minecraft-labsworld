# T23: WorldStateHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/WorldStateHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/WorldStateHandlerTest.kt`
**Current coverage:** 0/6 lines (0%)
**Target coverage:** ~100%

## Why This Matters
6 uncovered lines. Delegates to WorldStateUtils.

## Approach
- Mock `Bukkit.getWorlds()` via `mockkStatic`
- Mock `TwitchContext`, `RedeemInvocation`
- Mock or spy `WorldStateUtils.setWorldState`

## Tests to Write

### `@Nested class Properties`
- `key is "world.state"`
- `runOnMainThread is true`

### `@Nested class Handle`
- `calls setWorldState with correct state type` — params["type"] = "rain"
- `returns early when no worlds available` — `Bukkit.getWorlds()` returns empty list
- `errors when type param is missing`
- `uses first world from Bukkit.getWorlds()`
