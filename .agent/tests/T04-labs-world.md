# T04: LabsWorld Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/LabsWorld.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/LabsWorldTest.kt`
**Current coverage:** 0/82 lines (0%)
**Target coverage:** ~50%+ (some methods are thin facades)

## Why This Matters
82 uncovered lines. The main plugin class — its facade methods delegate to services. The `reloadTwitch()` method has important concurrency logic with `AtomicBoolean`.

## Approach
- `LabsWorld` extends `JavaPlugin` — mocking this is hard (Bukkit initialization)
- Focus on methods that can be tested with a mocked/spied `LabsWorld`:
  - Use `mockk<LabsWorld>(relaxed = true)` or `spyk` with relaxed parent
  - Use reflection to inject mocked subsystems (`twitchConfigManager`, `npcLinkManager`, etc.)
- Alternatively, test the `reloadTwitch` concurrency guard in isolation

## Tests to Write

### `@Nested class ReloadTwitch`
- `reloadTwitch succeeds on first call` — mock subsystems, verify config reloaded and client reconnected
- `reloadTwitch returns failure when already in progress` — set `twitchReloadInProgress` to true via reflection, verify Result.failure
- `reloadTwitch resets flag after success` — call reload, verify flag is false after
- `reloadTwitch resets flag after failure` — make `twitchConfigManager.reloadConfig()` throw, verify flag reset

### `@Nested class ReloadConfigOnly`
- `delegates to twitchConfigManager.reloadConfig` — verify called
- `returns failure when reloadConfig throws`

### `@Nested class TrySendTwitchMessage`
- `returns true on successful send` — mock getTwitchClient, mock chat.sendMessage
- `returns false when getTwitchClient throws` — twitchClient not initialized
- `returns false when sendMessage throws`

### `@Nested class TwitchStatusSnapshot`
- `returns correct snapshot with connected client` — verify all fields populated
- `returns snapshot with connected=false when not connected`

### `@Nested class FacadeMethods`
- `createNpcSpawnPointItem delegates to npcSpawnPointManager`
- `npcSpawnPointCount delegates to npcSpawnPointManager`
- `pickNpcSpawnPointSpawnLocation delegates to npcSpawnPointManager`
- `ensureNpcAtSpawnPoint delegates to npcLinkManager`
- `resolveLinkedUserIdByUserName delegates to npcLinkManager`

## Notes
- `onEnable()` and `initializeComponents()` create real subsystems and register Bukkit events — these are difficult to unit test. Skip or note as integration test territory.
- The `TwitchStatusSnapshot` data class can be tested directly.
