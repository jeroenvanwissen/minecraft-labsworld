# Security Requirements

## Secrets Management

- Store Twitch credentials in environment variables, never in YAML or source code:
  - `TWITCH_CLIENT_ID` — OAuth2 client ID
  - `TWITCH_CLIENT_SECRET` — OAuth2 client secret
  - `TWITCH_REFRESH_TOKEN` — Long-lived refresh token (recommended)
  - `TWITCH_ACCESS_TOKEN` — Short-lived access token (fallback)
  - `TWITCH_CHANNEL_NAME` — Optional channel name override
- Never commit `twitch.config.yml` with real credentials
- The `.gitignore` must exclude server runtime data, credential files, and build artifacts

## Input Validation

- Validate at system boundaries: Twitch events, config file parsing, in-game commands
- Use `Coercions.kt` utilities (`anyToInt`, `anyToString`, etc.) for type-safe config parsing — they return defaults instead of throwing
- Trim and null-check all config values before use
- Filter empty strings from config lists

## Authentication & Authorization

- Twitch permissions follow a strict hierarchy: `BROADCASTER > MODERATOR > VIP > SUBSCRIBER > EVERYONE`
- Use `TwitchAuth.isAuthorized(permission, event)` for all permission checks
- Role detection reads IRC badge tags and mod/vip/subscriber flags
- Broadcaster detected by matching `user.id == channel.id`
- In-game commands require Paper permissions (`labsworld.admin`, `labsworld.npcspawnpoint`)

## Data Protection

- NPC link data (`twitch-npcs.yml`) contains Twitch user IDs — treat as user data
- OAuth2 tokens refreshed automatically every 30 minutes via `TwitchClientManager`
- Never log access tokens, refresh tokens, or client secrets
- Log Twitch user IDs and display names only at `info` level for operational tracking

## Dependencies

- Pin dependency versions explicitly in `build.gradle.kts`
- GitHub Actions CI runs on every PR to main
- Review Twitch4J and credential-manager updates for security patches

## Do / Don't

| Do                                           | Don't                                         |
| -------------------------------------------- | --------------------------------------------- |
| `System.getenv("TWITCH_CLIENT_SECRET")`      | Hardcode secrets in source or config files    |
| `TwitchAuth.isAuthorized(permission, event)` | Skip permission checks on Twitch commands     |
| Return `Result.failure()` on invalid input   | Throw exceptions with secret values in message|
| Validate config values with Coercions utils  | Trust raw YAML values without type checking   |
| Use Paper permissions for in-game commands   | Allow all players to run admin commands        |
