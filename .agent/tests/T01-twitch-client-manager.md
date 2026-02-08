# T01: TwitchClientManager Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchClientManager.kt`
**Test file:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchClientManagerTest.kt`
**Status:** ✅ Done (PR #41)
**Coverage:** ~37% instruction coverage (all testable lines covered; remaining lines require real Twitch4J connections)

## Why This Matters
At 119 uncovered lines this is the single largest coverage gap. Many branches are around config validation and credential handling which are testable without a real Twitch connection.

## Approach
Much of this class creates real Twitch4J client instances and makes HTTP calls. Focus on the logic that can be tested via mocking rather than integration:

- Mock `JavaPlugin`, `TwitchConfigManager`, and `Bukkit.getScheduler()`
- Use MockK `mockkConstructor` or extract logic into testable units

## Tests to Write

### `@Nested class Init`
- `init returns false when hasRequiredConfig is false` — verify log message and false return
- `init returns false when clientId is empty` — config returns TwitchConfig with empty clientId
- `init returns false when clientSecret is empty`
- `init returns false when channelName is empty`

### `@Nested class IsConnected`
- `isConnected returns false before init` — new instance, `isConnected()` should be false
- `isConnected returns true after successful connect` — requires mocking TwitchClientBuilder (may be hard; document as integration test if so)

### `@Nested class Close`
- `close cancels token monitor task` — mock BukkitTask, verify `cancel()` called
- `close clears credential` — after close, `getCredential()` returns null
- `close is safe to call before init` — no crash

### `@Nested class IsCredentialValid`
- This is a private method, but its logic can be tested through `init()` behavior:
- `init with empty access token and no refresh token returns false`
- `init with short access token (<=10 chars) and no refresh token returns false`

## Notes
- `connect()` and `tokenMonitoring()` are private and deeply tied to Twitch4J. Where mocking the builder is impractical, document what needs integration testing.
- Use reflection to test `isCredentialValid` directly if needed (pattern already used in TwitchConfigManagerBindingsTest).
- The `getChannelId()` method makes a Helix API call — skip in unit tests, note for integration tests.
