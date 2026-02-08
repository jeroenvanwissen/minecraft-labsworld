# T12: LwCommand Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/LwCommand.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/LwCommandTest.kt`
**Current coverage:** 0/28 lines (0%)
**Target coverage:** ~80%+

## Why This Matters
28 uncovered lines. The main `!lw` command router with subcommand dispatch and authorization.

## Approach
- Mock `TwitchContext` (with mocked `LabsWorld`, `TwitchClient`, `TwitchConfigManager`)
- Mock `CommandInvocation` with controlled `args` and `event`
- Mock `TwitchAuth.isAuthorized` via `mockkObject(TwitchAuth)`
- Mock scheduler for `runOnMainThread` dispatch

## Tests to Write

### `@Nested class NoSubcommand`
- `replies with usage when no args` — verify `replyMention("Usage: !lw help")`

### `@Nested class UnknownSubcommand`
- `replies with error for unknown subcommand` — args=["foo"], verify "Unknown command"

### `@Nested class SubcommandDispatch`
- `dispatches to help subcommand` — args=["help"], verify HelpSubcommand.handle called
- `dispatches to duel subcommand` — args=["duel", "@user"]
- `dispatches to reload subcommand` — args=["reload"]
- `dispatches to reloadtwitch alias` — args=["reloadtwitch"]

### `@Nested class Authorization`
- `denies unauthorized user` — `isAuthorized` returns false, verify "don't have permission"
- `allows authorized user` — `isAuthorized` returns true, subcommand executed

### `@Nested class ThreadScheduling`
- `runs on main thread when subcommand.runOnMainThread is true` — verify `scheduler.runTask` called
- `runs inline when runOnMainThread is false`

## Notes
- `LwSubcommands.all` is a real object — the subcommand index is a `lazy` property built from it. Test with real subcommands.
- The `subcommandIndex` flattens name + aliases — verify alias resolution (e.g., "reload" maps to ReloadSubcommand).
