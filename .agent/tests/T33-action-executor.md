# T33: ActionExecutor — Improve Existing Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/ActionExecutor.kt`
**Current coverage:** 25/31 lines (80.6%)
**Target coverage:** ~95%+

## Why This Matters
6 uncovered lines. Already well-covered — just need to fill remaining gaps.

## Gaps to Fill

The existing test coverage (via RedeemDispatcherTest and CommandDispatcherTest) covers the happy path through `executeActions`. The gaps are likely:

### `@Nested class ErrorHandling`
- `logs warning when action handler throws` — provide action with type that throws, verify logger.warning
- `continues executing remaining actions after one fails` — 3 actions, middle one throws, verify all 3 attempted

### `@Nested class UnknownActionType`
- `throws error for unknown action type` — type="nonexistent", verify error("Unknown action type...")

### `@Nested class HandlerRegistry`
- `LootChestActionHandler registered under both "world.loot_chest" and "player.loot_chest"` — verify both types resolve
- `all expected handlers are registered` — verify each type string maps to a handler

## Notes
- `ActionExecutor` is an `object` — use `mockkObject` if needed to spy, but direct testing through `executeAction` is cleaner.
- The handler map is built in the `buildMap` block — test by calling `executeAction` with known types.
