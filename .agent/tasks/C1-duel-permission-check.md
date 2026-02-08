# Task C1: Add Permission Check on Duel Command

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `C1`         |
| **Status**       | `[ ]`        |
| **Priority**     | High         |
| **Dependencies** | A1           |
| **Branch**       | `fix/c1-duel-permission` |

**Goal:**
Restrict the `!lw duel @user` command to prevent spam/griefing. Currently any viewer can challenge any other viewer without restriction.

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
└── twitch/commands/lw/DuelSubcommand.kt    # MODIFY — add permission or rate limiting
```

**Implementation:**

1. Add a configurable permission level for the duel command (default: `SUBSCRIBER`)
2. Check `TwitchAuth.isAuthorized()` before processing the duel request
3. Send a chat message if the user lacks permission

**Acceptance Criteria:**

- [ ] Viewers below the configured permission level cannot initiate duels
- [ ] Unauthorized duel attempts result in a chat message explaining the requirement
- [ ] The permission level is configurable in `twitch.config.yml`
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
