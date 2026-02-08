# T16: PlayerUtils Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/util/PlayerUtils.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/util/PlayerUtilsTest.kt`
**Current coverage:** 0/9 lines (0%)
**Target coverage:** ~95%+

## Why This Matters
9 uncovered lines. Small but used by many action handlers — high leverage.

## Approach
- Mock `Server` with `onlinePlayers` and `getPlayerExact()`
- Mock `Player` instances

## Tests to Write

### `@Nested class PickTargetPlayer`
- `returns exact player when preferred name matches` — `getPlayerExact` returns player
- `returns null when preferred set but not found and allowRandom=false`
- `falls back to random when preferred not found and allowRandom=true`
- `returns null when no players online`
- `returns the only player when exactly one online`
- `returns random player from multiple online` — multiple players, allowRandom=true
- `returns null when preferred is null and allowRandom=false`
- `returns null when preferred is blank and allowRandom=false and multiple players`
- `handles null preferred with allowRandom=true` — should pick random
