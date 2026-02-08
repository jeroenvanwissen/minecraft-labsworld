# T21: NpcSwarmHandler (NpcAggroHandler) Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/NpcAggroHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/NpcSwarmHandlerTest.kt`
**Current coverage:** 0/8 lines (0%)
**Target coverage:** ~90%+

## Why This Matters
8 uncovered lines. Swarm redeem handler.

## Approach
- Mock `TwitchContext`, `LabsWorld`, `RedeemInvocation`, `PlayerUtils`

## Tests to Write

### `@Nested class Properties`
- `key is "npc.swarm"`
- `runOnMainThread is true`

### `@Nested class Handle`
- `calls startSwarmAllNpcs with correct params` — seconds from params
- `errors when no online players` — `pickTargetPlayer` returns null
- `coerces seconds to range 1-300`
- `uses default 30 seconds when param missing`
- `propagates failure from startSwarmAllNpcs`
