# Idea C01: Chat Voting

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `C01`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/c01-chat-voting`      |

**Goal:**
Broadcaster starts a poll via `!lw vote`, and viewers vote in Twitch chat to decide in-game events. Options could be weather changes, mob spawns, time of day, or custom events. Results are tallied and the winning option is executed automatically.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   └── VotingService.kt                   # CREATE — vote tracking & execution
├── twitch/commands/lw/
│   └── VoteSubcommand.kt                  # CREATE — start/cast vote
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VotingService`:
   - `startVote(question, options, durationSeconds)` — start a new poll
   - Track votes per user (one vote per viewer, can change)
   - Timer counts down, displays periodic updates in chat
   - When timer expires: tally votes, announce winner, execute action
   - Pre-configured vote templates:
     - `weather`: clear / rain / thunder
     - `time`: day / night / sunset
     - `mobs`: zombies / skeletons / creepers / endermen
     - `custom`: broadcaster defines options and actions
2. `!lw vote start weather 60` — start a 60s weather vote
3. `!lw vote 1` / `!lw vote 2` / `!lw vote 3` — cast a vote
4. Display live vote counts in Twitch chat every 15 seconds
5. Winning option triggers the corresponding action handler

**Paper API Used:**
- Reuses existing `ActionExecutor` to run the winning action
- `World.setStorm()` / `World.setThundering()` — weather actions
- `World.setTime()` — time actions
- `World.spawn()` — mob spawn actions
- `BukkitRunnable` — vote timer

**Acceptance Criteria:**

- [ ] `!lw vote start weather 60` starts a vote (broadcaster/mod only)
- [ ] Viewers can vote via `!lw vote 1/2/3`
- [ ] Each viewer gets one vote (can change before timer expires)
- [ ] Vote counts displayed periodically in chat
- [ ] Winning option is executed automatically
- [ ] Active vote prevents starting another
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
