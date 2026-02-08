# T18: PlayerActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/PlayerActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/PlayerActionHandlerTest.kt`
**Current coverage:** 0/12 lines (0%)
**Target coverage:** ~85%+

## Why This Matters
12 uncovered lines. Has action dispatch logic with specific action types.

## Approach
- Mock `TwitchContext`, `RedeemInvocation`
- Mock `Bukkit.getPlayerExact()` and `Bukkit.getOnlinePlayers()` via `mockkStatic`
- Mock `Player.world.spawn()` for TNT

## Tests to Write

### `@Nested class Handle`
- `returns early when no player found` — no online players, no crash
- `errors when action type is missing` — no "action" in params
- `errors for unknown action type` — "unknown_action" → error

### `@Nested class SpawnTnt`
- `spawns TNTPrimed at player location` — action="spawn_tnt", verify world.spawn called
- `sets fuse ticks to 80` — verify `tnt.fuseTicks = 80`
- `resolves target player from params` — target_player param set
- `falls back to first online player when no target specified`
