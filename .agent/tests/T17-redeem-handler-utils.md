# T17: RedeemHandlerUtils Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/RedeemHandlerUtils.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/handlers/RedeemHandlerUtilsTest.kt`
**Current coverage:** 0/6 lines (0%)
**Target coverage:** ~100%

## Why This Matters
6 uncovered lines. Pure function — easiest win in the codebase.

## Approach
- Create a mock `RedeemInvocation` (or construct a real one with mock event)
- Test `renderTemplate` directly

## Tests to Write

### `@Nested class RenderTemplate`
- `replaces {user} with userName`
- `replaces {userId} with userId`
- `replaces {reward} with rewardTitle`
- `replaces {rewardId} with rewardId`
- `replaces {input} with userInput`
- `replaces {input} with empty string when userInput is null`
- `replaces multiple placeholders in single template`
- `leaves template unchanged when no placeholders present`
