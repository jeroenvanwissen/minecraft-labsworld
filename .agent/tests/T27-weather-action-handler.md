# T27: WeatherActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/WeatherActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/WeatherActionHandlerTest.kt`
**Current coverage:** 2/9 lines (22.2%)
**Target coverage:** ~90%+

## Why This Matters
7 uncovered lines. Delegates to WorldStateUtils.

## Approach
- Mock `ActionUtils.resolveTargetPlayer`, `ActionUtils.pickDefaultWorld`
- Mock or spy `WorldStateUtils.setWorldState`
- Mock `Player`, `World`

## Tests to Write

### `@Nested class Properties`
- `type is "world.weather"`

### `@Nested class Handle`
- `uses player world when player found` — verify setWorldState on player's world
- `falls back to default world when no player` — resolveTargetPlayer null, pickDefaultWorld returns world
- `returns early when no world available` — both null
- `passes state param to setWorldState` — state="rain"
- `defaults to "clear" when state param missing`
- `converts duration_seconds to ticks` — 60 seconds → 1200 ticks
- `coerces duration to at least 1 second`
