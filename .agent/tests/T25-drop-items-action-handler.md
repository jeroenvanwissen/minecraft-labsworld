# T25: DropItemsActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/DropItemsActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/DropItemsActionHandlerTest.kt`
**Current coverage:** 2/15 lines (13.3%)
**Target coverage:** ~80%+

## Why This Matters
13 uncovered lines. Item dropping with stack splitting logic.

## Approach
- Mock `ActionUtils` (resolveTargetPlayer, parseItemStacks)
- Mock `Player`, `World`

## Tests to Write

### `@Nested class Properties`
- `type is "player.drop_items"`

### `@Nested class Handle`
- `returns early when no target player`
- `errors when no items defined` — empty items list → "No items defined"
- `drops single item stack` — one item, verify `dropItemNaturally` called
- `splits large stacks by maxStackSize` — item amount > maxStackSize, verify multiple drops
- `handles multiple item types` — 3 different items, verify 3+ drops
