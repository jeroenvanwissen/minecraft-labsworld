# T14: ReloadSubcommand Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/ReloadSubcommand.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/ReloadSubcommandTest.kt`
**Current coverage:** 0/11 lines (0%)
**Target coverage:** ~90%+

## Why This Matters
11 uncovered lines. Simple but important reload command.

## Approach
- Mock `TwitchContext`, `LabsWorld`, `CommandInvocation`
- Control `reloadTwitch()` return value and `trySendTwitchMessage()` behavior

## Tests to Write

### `@Nested class Properties`
- `name is "reloadtwitch"`
- `aliases contains "reload"`
- `permission is MODERATOR`

### `@Nested class Handle`
- `sends "Reloading..." reply` — verify `replyMention("Reloading Twitch config...")`
- `calls plugin.reloadTwitch()` — verify called
- `sends success via trySendTwitchMessage on success` — reload returns success, trySendTwitchMessage returns true
- `sends failure message on reload failure` — reload returns failure with exception
- `falls back to replyMention when trySendTwitchMessage fails` — trySendTwitchMessage returns false, verify replyMention called
