# Task C3: Add Duel Accept/Timeout Flow

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `C3`         |
| **Status**       | `[x]`        |
| **Priority**     | Medium       |
| **Dependencies** | A1           |
| **Branch**       | `feature/c3-duel-accept-timer` |

**Goal:**
When a viewer uses `!lw duel @user`, the challenged user must type `!accept` within 60 seconds for the duel to start. If they don't accept in time, the challenge expires and a timeout message is sent to chat.

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcDuelService.kt                   # MODIFY — add pending challenge state, accept/timeout logic
├── twitch/commands/lw/DuelSubcommand.kt             # MODIFY — create pending challenge instead of starting duel immediately
├── twitch/commands/lw/AcceptSubcommand.kt           # CREATE — !lw accept command handler
├── twitch/commands/lw/LwSubcommands.kt              # MODIFY — register AcceptSubcommand

plugin/src/test/kotlin/nl/jeroenlabs/labsWorld/
└── npc/VillagerNpcDuelServiceTest.kt                # CREATE or MODIFY — test accept/timeout flow
```

**Implementation:**

1. Add a `PendingChallenge` data class to `VillagerNpcDuelService` holding: `challengerId`, `challengerName`, `challengedId`, `challengedName`, `expiresAtMillis`
2. Add a `pendingChallenge` property to `VillagerNpcDuelService` (nullable, only one pending challenge at a time)
3. Modify `DuelSubcommand.handle()` to create a `PendingChallenge` instead of starting the duel immediately, and send a chat message like `"@challenged, you have been challenged to a duel by @challenger! Type !lw accept within 60 seconds"`
4. Schedule a Bukkit delayed task (60 seconds = 1200 ticks) that clears `pendingChallenge` if still present and sends a timeout message to chat (e.g., `"@challenger's duel challenge to @challenged has expired"`)
5. Create `AcceptSubcommand` implementing `LwSubcommand`:
   - Check that the sender's Twitch user ID matches `pendingChallenge.challengedId`
   - If match: clear pendingChallenge, cancel the timeout task, and call `startDuel()` with both NPCs
   - If no pending challenge or wrong user: send a chat message explaining there's no challenge to accept
6. Register `AcceptSubcommand` in `LwSubcommands` under the key `"accept"`
7. Add unit tests covering: challenge created, accept starts duel, timeout clears challenge, wrong user cannot accept, no duplicate challenges while one is pending

**Acceptance Criteria:**

- [x] `!lw duel @user` creates a pending challenge and sends a chat message to the challenged user
- [x] `!lw accept` by the challenged user starts the duel within the 60-second window
- [x] Challenge expires after 60 seconds with a timeout message if not accepted
- [x] Only the challenged user can accept (other users get a rejection message)
- [x] A new challenge cannot be created while one is already pending
- [x] The timeout task is cancelled when the challenge is accepted
- [x] Build passes: `plugin/gradlew compileKotlin`
- [x] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
