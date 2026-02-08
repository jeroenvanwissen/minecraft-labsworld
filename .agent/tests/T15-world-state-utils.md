# T15: WorldStateUtils Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/util/WorldStateUtils.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/util/WorldStateUtilsTest.kt`
**Current coverage:** 0/19 lines (0%)
**Target coverage:** ~95%+

## Why This Matters
19 uncovered lines. Pure utility with a mocked World — every branch is easily testable.

## Approach
- Mock `World` (relaxed)
- Call `setWorldState` with each state string and verify world mutations

## Tests to Write

### `@Nested class Day`
- `sets world time to 1000` — verify `world.time = 1000`

### `@Nested class Night`
- `sets world time to 13000`

### `@Nested class Clear`
- `disables storm and thunder` — verify `setStorm(false)`, `isThundering = false`
- `sets weatherDuration when durationTicks provided`
- `does not set weatherDuration when durationTicks is null`

### `@Nested class Rain`
- `enables storm, disables thunder` — verify `setStorm(true)`, `isThundering = false`
- `sets weatherDuration when provided`

### `@Nested class StormAndThunder`
- `enables storm and thunder` — verify both set to true
- `sets both weatherDuration and thunderDuration when provided`
- `"thunder" alias works same as "storm"`

### `@Nested class CaseInsensitivity`
- `"DAY" works same as "day"`
- `"Rain" works same as "rain"`

### `@Nested class UnknownState`
- `throws error for unknown state` — "foo" → IllegalStateException
