# T32: TwitchConfigManager — Improve Existing Tests

**Source:** `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchConfigManager.kt`
**Existing test:** `src/test/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchConfigManagerBindingsTest.kt`
**Current coverage:** 68/112 lines (60.7%)
**Target coverage:** ~80%+

## Why This Matters
44 uncovered lines. Existing tests cover binding parsing thoroughly. Gaps are in `init()`, `getConfig()`, `hasRequiredConfig()`, `getTwitchEnvPresence()`, `isRedeemsEnabled()`, `shouldLogUnmatchedRedeems()`, `saveConfig()`, and `parsePermission()`.

## Gaps to Fill

### `@Nested class Init`
- `creates config file from resource when none exists` — mock plugin.saveResource
- `loads existing config file` — pre-write YAML file
- `increments reloadVersion` — verify getReloadVersion() returns 1 after init
- `logs warning for legacy auth keys` — config with client_id key, verify logger.warning

### `@Nested class ReloadConfig`
- `reloads YAML from disk` — modify file after init, reload, verify new values
- `increments reloadVersion` — verify version bumps on each reload

### `@Nested class GetConfig`
- `reads auth from environment variables` — mock System.getenv (tricky; may use reflection on `env()` method)
- `reads channelName from YAML first` — configYaml has channel_name
- `falls back to TWITCH_CHANNEL_NAME env var when YAML is empty`

### `@Nested class HasRequiredConfig`
- `returns true when all required config present` — clientId, clientSecret, channelName, refreshToken all set
- `returns false when clientId is empty`
- `returns false when channelName is empty`
- `returns true with accessToken instead of refreshToken`
- `returns false when both accessToken and refreshToken are empty`

### `@Nested class GetTwitchEnvPresence`
- `returns map with correct keys`
- `correctly detects present/absent env vars`

### `@Nested class ParsePermission`
- `parses "broadcaster"` → Permission.BROADCASTER
- `parses "moderator"` → Permission.MODERATOR
- `parses "mod" alias` → Permission.MODERATOR
- `parses "vip"` → Permission.VIP
- `parses "subscriber"` → Permission.SUBSCRIBER
- `parses "sub" alias` → Permission.SUBSCRIBER
- `defaults to EVERYONE for unknown value`
- `defaults to EVERYONE for null`

### `@Nested class SimpleAccessors`
- `isRedeemsEnabled reads from YAML` — set redeems.enabled: true
- `shouldLogUnmatchedRedeems defaults to true`

## Notes
- The `env()` method reads `System.getenv()`. You can either:
  - Use `mockkStatic(System::class)` (fragile)
  - Set env vars in the test JVM (not easy)
  - Use reflection to override `env()` or test `hasRequiredConfig` through `getConfig` with known env state
- Existing test uses reflection to inject `configYaml` — follow that pattern for new tests.
