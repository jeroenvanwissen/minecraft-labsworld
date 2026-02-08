# Idea E03: In-Game Leaderboard

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `E03`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/e03-leaderboard`      |

**Goal:**
A holographic in-game leaderboard displayed via TextDisplay entities showing top NPC stats. Categories: duel wins, coins earned, level, kill count. Visible in the city center and updates in real-time.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcStatsService.kt         # CREATE — stats tracking
│   └── VillagerNpcLeaderboardService.kt   # CREATE — display management
├── twitch/commands/lw/
│   └── StatsSubcommand.kt                 # CREATE — personal stats command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcStatsService`:
   - Track per-viewer stats in YAML: `npc-stats.yml`
   - Stats: duel_wins, duel_losses, kills, deaths, coins_earned, messages_sent
   - Increment methods: `recordDuelWin(userId)`, `recordKill(userId)`, etc.
   - Hook into existing duel/combat services to record stats
2. Create `VillagerNpcLeaderboardService`:
   - Spawn `TextDisplay` entities at a configured location (city center)
   - Show top 10 for default category (duel wins)
   - Update every 30 seconds
   - Format: rank, username, stat value with colors
   - Multiple boards for different categories (spawn side by side)
3. `!lw stats` — show viewer's personal stats in Twitch chat
4. `!lw top` — show top 5 in Twitch chat

**Paper API Used:**
- `World.spawn(TextDisplay.class)` — holographic text
- `TextDisplay.text(Component)` — formatted leaderboard text
- `TextDisplay.setBackgroundColor(Color)` — board background
- `TextDisplay.setBillboard(Billboard.CENTER)` — face players
- `BukkitRunnable` — periodic update
- YAML persistence for stats
- Adventure API — colored ranking text

**Acceptance Criteria:**

- [ ] Stats tracked for: duel wins, kills, deaths, coins, messages
- [ ] Holographic leaderboard visible at city center
- [ ] Top 10 displayed with rank, name, and value
- [ ] Leaderboard updates every 30 seconds
- [ ] `!lw stats` shows personal stats
- [ ] `!lw top` shows top 5 in Twitch chat
- [ ] Stats persist across server restarts
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
