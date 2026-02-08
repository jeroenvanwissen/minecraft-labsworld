# T28: HealActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/HealActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/HealActionHandlerTest.kt`
**Current coverage:** 2/9 lines (22.2%)
**Target coverage:** ~90%+

## Why This Matters
7 uncovered lines. Health calculation with hearts-to-HP conversion.

## Approach
- Mock `ActionUtils.resolveTargetPlayer`
- Mock `Player` with `health`, `getAttribute(Attribute.MAX_HEALTH)`

## Tests to Write

### `@Nested class Properties`
- `type is "player.heal"`

### `@Nested class Handle`
- `returns early when no target player`
- `heals using hearts param (hearts * 2)` — hearts=3, verify health += 6.0
- `heals using health param when hearts not specified` — health=4.0, verify health += 4.0
- `defaults to 4.0 health when neither param specified`
- `does not heal when healthPoints <= 0` — hearts=0
- `caps health at maxHealth` — player.health=18, heal 10, maxHealth=20 → health=20
- `uses hearts param over health param when both present` — hearts takes precedence when >= 0
