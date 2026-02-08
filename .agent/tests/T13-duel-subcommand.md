# T13: DuelSubcommand Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/DuelSubcommand.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/DuelSubcommandTest.kt`
**Current coverage:** 0/19 lines (0%)
**Target coverage:** ~85%+

## Why This Matters
19 uncovered lines. Validation-heavy subcommand with clear error paths.

## Approach
- Mock `TwitchContext` with mocked `LabsWorld` plugin
- Mock `CommandInvocation` with controlled args, userId, userName

## Tests to Write

### `@Nested class Validation`
- `rejects user without NPC` — `getStoredLinkedUserName(userId)` returns null → "don't have an NPC yet"
- `rejects missing target` — args has no index 1 → "Usage: !lw duel @TwitchUser"
- `rejects blank target`
- `rejects self-duel` — target name equals invoker name → "Invalid target"
- `rejects target without NPC` — `resolveLinkedUserIdByUserName` returns null → "No NPC found"

### `@Nested class DuelStart`
- `starts duel with correct user IDs and names` — verify `startNpcDuel` called with correct params
- `uses stored name for target` — `getStoredLinkedUserName` returns "StoredName", verify passed
- `replies with failure message on duel error` — `startNpcDuel` returns failure

### `@Nested class NameSanitization`
- `strips @ prefix from target name` — "@user" → "user"
- `strips non-alphanumeric/underscore chars` — handled by `sanitizeTwitchName`
