# Idea B08: Tournament Bracket

| Field            | Value                                |
| ---------------- | ------------------------------------ |
| **ID**           | `B08`                                |
| **Status**       | `[ ]`                                |
| **Dependencies** | None (uses existing duel system)     |
| **Branch**       | `feature/b08-tournament-bracket`     |

**Goal:**
An automated multi-round duel tournament. All participating NPCs are seeded into a bracket, and duels are played automatically in sequence. Winners advance until a champion is crowned. Leverages the existing `VillagerNpcDuelService`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcTournamentService.kt    # CREATE — bracket & scheduling
├── twitch/commands/lw/
│   └── TournamentSubcommand.kt            # CREATE — start/join commands
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcTournamentService`:
   - **Registration phase** (60s): `!lw tournament join` to enter
   - **Bracket generation**: Shuffle participants, create single-elimination bracket
   - If not power-of-2, some NPCs get byes in round 1
   - **Round execution**: Run duels sequentially using existing `VillagerNpcDuelService`
   - Wait for each duel to finish, then start the next
   - **Between rounds**: 15s break, announce upcoming matchups
   - **Finals**: Special announcement, extra fireworks for winner
   - **Champion**: Announced in Twitch chat with title "Tournament Champion"
2. Display bracket progress in chat (text-based bracket visualization)
3. Announce each matchup before it starts
4. Track tournament history (winner, runner-up) in YAML

**Paper API Used:**
- Reuses `VillagerNpcDuelService.startDuel()` for individual matches
- `BukkitRunnable` — scheduling between rounds
- `World.spawn(Firework.class)` — champion celebration
- `World.playSound()` — round start sounds
- Adventure API — bracket text formatting

**Acceptance Criteria:**

- [ ] `!lw tournament` starts registration phase (broadcaster/mod only)
- [ ] `!lw tournament join` enters the viewer's NPC
- [ ] Bracket is generated with proper seeding and byes
- [ ] Duels are played automatically in sequence
- [ ] Round results are announced in Twitch chat
- [ ] Champion is declared with celebration effects
- [ ] Tournament history is persisted
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
