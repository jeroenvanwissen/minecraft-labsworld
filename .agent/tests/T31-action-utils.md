# T31: ActionUtils — Improve Existing Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/ActionUtils.kt`
**Existing test:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/ActionUtilsParsingTest.kt`
**Current coverage:** 15/36 lines (41.7%)
**Target coverage:** ~75%+

## Why This Matters
21 uncovered lines. Existing tests cover `parseFireworkType`, `parseEntityType`, `parseItemStacks` (partially), and `pickDefaultWorld`. Gaps are in `resolveTargetPlayer`, `randomOffset`, and `parseColors`.

## Gaps to Fill

### `@Nested class ResolveTargetPlayer`
- `returns player by exact name from params["target_player"]` — mock Bukkit.getPlayerExact
- `resolves "redeemer" alias to invocation.userName`
- `resolves "invoker" alias`
- `resolves "self" alias`
- `resolves "{user}" alias`
- `falls back to invocation.userName when target_player is null`
- `falls back to random player when no match and allowRandom=true`
- `returns null when no match and allowRandom=false`
- `returns null when no online players`

### `@Nested class RandomOffset`
- `returns zero vector when radius is 0`
- `returns zero vector when radius is negative`
- `returns vector within radius bounds` — may need statistical test or just verify magnitude

### `@Nested class ParseColors`
- `parses valid dye color names` — ["RED", "BLUE"] → [Color.RED, Color.BLUE]
- `ignores invalid color names` — ["INVALID"] → empty
- `handles mixed valid and invalid` — ["RED", "BOGUS", "GREEN"] → [Color.RED, Color.GREEN]
- `is case insensitive` — ["red"] → uppercase conversion happens

## Notes
- `resolveTargetPlayer` calls `Bukkit.getPlayerExact()` and `Bukkit.getServer()` — use `mockkStatic(Bukkit::class)`.
- `parseColors` uses `DyeColor.valueOf()` which is a real enum — no mocking needed.
- Existing test already has `@AfterEach unmockkAll()` — follow pattern.
