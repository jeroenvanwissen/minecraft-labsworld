# T20: NpcSpawnHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/NpcSpawnHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/NpcSpawnHandlerTest.kt`
**Current coverage:** 0/9 lines (0%)
**Target coverage:** ~90%+

## Why This Matters
9 uncovered lines. Core NPC spawn redeem handler.

## Approach
- Mock `TwitchContext`, `LabsWorld`, `RedeemInvocation`
- Mock `say` extension function via `mockkStatic`

## Tests to Write

### `@Nested class Properties`
- `key is "npc.spawn"`
- `runOnMainThread is true`

### `@Nested class Handle`
- `errors when no spawn point placed` — `pickNpcSpawnPointSpawnLocation` returns null
- `calls ensureNpcAtSpawnPoint with correct user ID and name`
- `propagates failure from ensureNpcAtSpawnPoint`
- `sends template message when message param is present`
- `does not send message when message param is null`
- `does not send message when message param is blank`
