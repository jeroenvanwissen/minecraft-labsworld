# T19: NpcAttackHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/NpcAttackHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/NpcAttackHandlerTest.kt`
**Current coverage:** 0/9 lines (0%)
**Target coverage:** ~90%+

## Why This Matters
9 uncovered lines. Redeem handler that delegates to plugin.startAttackAllNpcs.

## Approach
- Mock `TwitchContext` with mocked `LabsWorld`
- Mock `RedeemInvocation`
- Mock `PlayerUtils.pickTargetPlayer` via `mockkObject`

## Tests to Write

### `@Nested class Properties`
- `key is "npc.attack"`
- `runOnMainThread is true`

### `@Nested class Handle`
- `calls startAttackAllNpcs with correct params` — seconds coerced, hearts coerced
- `errors when no online players` — `pickTargetPlayer` returns null → error "No online players"
- `coerces seconds to range 1-300`
- `coerces hearts_per_hit to range 0.5-10.0`
- `uses defaults when params missing` — 30 seconds, 1.0 hearts
- `propagates failure from startAttackAllNpcs` — `getOrThrow` rethrows
