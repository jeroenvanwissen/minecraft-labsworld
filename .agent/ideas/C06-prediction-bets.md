# Idea C06: Prediction Bets

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `C06`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | E01 (NPC Currency, optional)       |
| **Branch**       | `feature/c06-prediction-bets`      |

**Goal:**
Before a duel or game mode starts, viewers can predict the outcome via `!lw bet @npcname`. Correct predictions earn points/currency. Adds a spectator engagement layer to existing combat features.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   └── PredictionService.kt              # CREATE — bet tracking & payout
├── twitch/commands/lw/
│   └── BetSubcommand.kt                  # CREATE — place bets
└── twitch/commands/lw/
    └── LwSubcommands.kt                  # MODIFY — register subcommand
```

**Implementation:**

1. Create `PredictionService`:
   - `openPrediction(options: List<String>)` — open betting window
   - `placeBet(userId, optionIndex)` — record a viewer's prediction
   - `closeBetting()` — stop accepting bets
   - `resolvePrediction(winningOption)` — determine winners, announce results
   - Track: bettor user ID → chosen option
2. Integrate with `VillagerNpcDuelService`:
   - When duel starts: auto-open prediction with both NPC names as options
   - 15-second betting window before combat begins
   - When duel ends: resolve prediction with winner
3. `!lw bet @npcname` or `!lw bet 1/2` to place a bet
4. Announce: "Betting open! !lw bet @npc1 or !lw bet @npc2 — 15 seconds!"
5. Results: "Correct predictions: @user1, @user2, @user3 — X total bettors"

**Paper API Used:**
- `BukkitRunnable` — betting window timer
- Integration with existing duel/tournament services
- Adventure API — formatted announcements

**Acceptance Criteria:**

- [ ] Betting opens automatically before duels
- [ ] `!lw bet @name` places a prediction
- [ ] Betting closes after time window
- [ ] Correct predictions are announced after outcome
- [ ] One bet per viewer per prediction
- [ ] Viewers can't bet on themselves (optional)
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
