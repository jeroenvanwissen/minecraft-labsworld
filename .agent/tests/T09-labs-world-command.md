# T09: LabsWorldCommand Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/commands/LabsWorldCommand.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/commands/LabsWorldCommandTest.kt`
**Current coverage:** 0/44 lines (0%) + 0/5 LabsWorldPaperCommand
**Target coverage:** ~80%+

## Why This Matters
44 uncovered lines. Pure command routing with clear branches — ideal for unit testing.

## Approach
- Mock `LabsWorld` plugin (relaxed)
- Mock `CommandSender` and `Player` (for spawnpoint subcommand)
- Test each subcommand branch independently

## Tests to Write

### `@Nested class NoArgs`
- `sends usage message when no args provided`

### `@Nested class ReloadTwitch`
- `sends "Reloading..." message` — verify `sender.sendMessage`
- `runs reload async` — verify `scheduler.runTaskAsynchronously` called
- `sends success message on reload success` — mock plugin.reloadTwitch returns success
- `sends failure message on reload failure` — mock returns failure

### `@Nested class ReloadConfig`
- `calls plugin.reloadConfigOnly`
- `sends success message on success`
- `sends failure message on failure`

### `@Nested class Status`
- `displays all status fields` — verify sendMessage called with "connected:", "channel:", "has_required_config:", "env:", "npc_spawn_points:"

### `@Nested class Spawnpoint`
- `rejects non-player sender` — CommandSender (not Player), sends "only be used in-game"
- `gives spawn point item to player` — mock Player.inventory.addItem returns empty, sends "Given: NPC Spawn Point"
- `drops item when inventory full` — addItem returns leftover, sends "Inventory full"

### `@Nested class UnknownSubcommand`
- `sends unknown subcommand message` — args=["foo"], verify message contains "Unknown subcommand"

### `@Nested class LabsWorldPaperCommand`
- `delegates to LabsWorldCommand.handle` — verify handle called with correct args
- `permission returns labsworld.admin`

## Notes
- All subcommands return `true` — verify return value.
- The `reloadtwitch` subcommand uses nested scheduler calls (async then sync). Mock both `runTaskAsynchronously` and `runTask` to capture and run Runnables.
