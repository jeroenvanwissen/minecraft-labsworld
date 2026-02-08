# T03: LootChestActionHandler Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/LootChestActionHandler.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/LootChestActionHandlerTest.kt`
**Current coverage:** 3/106 lines (2.8%)
**Target coverage:** ~60%+

## Why This Matters
106 lines with near-zero coverage. Contains significant private helper logic (loot parsing, weighted picking, chest placement) that drives real game behavior.

## Approach
- The `handle()` method requires mocking Bukkit world, block, chest state, and inventory
- Private helpers (`parseLootEntries`, `pickWeighted`, `findChestSpawnLocation`, `placeInRandomEmptySlot`) are tested through the public `handle()` method
- Mock `ActionUtils.resolveTargetPlayer()` to return a mock Player
- Mock `TwitchContext` with `LabsWorld` plugin, server, scheduler, twitchClient

## Tests to Write

### `@Nested class HandleBasicFlow`
- `places chest near player with loot` — full happy path: provide valid loot params, verify block type set to CHEST, inventory filled
- `errors when no loot defined and no debug bread` — params with no loot → expect error "No loot defined"
- `returns early when no target player found` — `resolveTargetPlayer` returns null, no crash
- `errors when no safe chest location found` — mock world to return non-solid floors, expect "Could not find a safe spot"

### `@Nested class LootParsing`
(Tested indirectly through `handle()` with crafted params)
- `parses valid loot entries with type, amount, weight` — verify items appear in chest
- `ignores entries with missing type` — malformed entry skipped
- `ignores entries with zero weight` — filtered out
- `respects min_amount and max_amount range`
- `falls back amount field to min/max when specified`

### `@Nested class ChestPlacement`
- `places chest on solid ground` — mock world `getHighestBlockYAt`, verify placement Y
- `skips hopper blocks` — floor is HOPPER, tries next location
- `skips non-air placement locations` — block above ground is not air

### `@Nested class DebugBread`
- `debug_force_bread_amount places bread in chest` — set param, verify BREAD in inventory
- `debug bread works even without loot entries`

### `@Nested class Announcement`
- `announces chest location when announce=true` — verify chat message sent
- `does not announce when announce=false` — verify no chat message

### `@Nested class AdditionalTypes`
- `additionalTypes contains "player.loot_chest"` — simple assertion on the field

## Notes
- `Material.matchMaterial()` is a static Bukkit method that needs `mockkStatic` — may need to be careful or skip those specific parsing paths.
- The `findChestSpawnLocation` uses `Random.nextInt` so outcomes aren't deterministic; either mock Random or just verify the chest is placed (any location).
- `setChestNameBestEffort` calls Paper's `Component.text()` which should work in tests.
