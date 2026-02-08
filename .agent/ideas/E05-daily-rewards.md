# Idea E05: Daily Rewards

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `E05`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | E01 (NPC Currency)             |
| **Branch**       | `feature/e05-daily-rewards`    |

**Goal:**
Viewers receive daily login bonuses when they first chat during a stream. Consecutive daily check-ins earn bonus multipliers. Rewards include coins, temporary effects, and rare cosmetics on streak milestones.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcDailyRewardService.kt   # CREATE — daily reward logic
├── twitch/commands/lw/
│   └── DailySubcommand.kt                 # CREATE — claim/check daily
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcDailyRewardService`:
   - Track per-viewer: last_claim_date, current_streak, longest_streak
   - Store in YAML: `npc-daily-rewards.yml`
   - `claimDaily(userId): DailyReward` — claim if not yet claimed today
   - Reward structure:
     - Day 1: 10 coins
     - Day 2: 15 coins
     - Day 3: 20 coins + speed boost
     - Day 5: 50 coins + rare title
     - Day 7: 100 coins + unique particle trail
     - Day 14: 200 coins + exclusive aura
     - Day 30: 500 coins + legendary title
   - Streak breaks if viewer misses a day
   - On claim: firework at NPC, chat announcement
2. `!lw daily` — claim daily reward or show streak info
3. Auto-claim on first chat message of the day (configurable)
4. Streak milestone announcements in Twitch chat

**Paper API Used:**
- `java.time.LocalDate` — date tracking
- YAML persistence for reward state
- `World.spawn(Firework.class)` — claim celebration
- `LivingEntity.addPotionEffect()` — temporary effect rewards
- `VillagerNpcCurrencyService` — coin rewards
- Adventure API — formatted reward text

**Acceptance Criteria:**

- [ ] Viewers can claim one reward per day
- [ ] Consecutive days increase the reward
- [ ] Streak breaks on missed days
- [ ] `!lw daily` claims reward or shows status
- [ ] Milestone rewards at 5, 7, 14, 30 days
- [ ] Claim triggers celebration effect at NPC
- [ ] Reward data persists across restarts
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
