# Idea E04: NPC Achievement System

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `E04`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/e04-achievement-system`   |

**Goal:**
An achievement system where NPCs unlock milestones for accomplishments. Achievements display as chat announcements and unlock cosmetic rewards. Encourages viewers to engage with all features of the plugin.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcAchievementService.kt   # CREATE — achievement tracking
├── twitch/commands/lw/
│   └── AchievementsSubcommand.kt          # CREATE — view achievements
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcAchievementService`:
   - Define achievements with criteria and rewards:
     - "First Steps" — spawn your first NPC → unlock title "Newcomer"
     - "Gladiator" — win your first duel → 50 coins
     - "Undefeated" — win 5 duels in a row → unlock title "Champion"
     - "Social Butterfly" — send 100 chat messages → unlock heart trail
     - "Survivor" — survive a battle royale → 100 coins
     - "Generous" — give coins to 5 different viewers → unlock title "Philanthropist"
     - "Explorer" — participate in a treasure hunt → 25 coins
     - "Veteran" — have NPC alive for 7 days → unlock aura
   - Track unlocked achievements per viewer in YAML
   - Auto-check achievement criteria on relevant events
   - On unlock: announce in Twitch chat, play sound at NPC, show particles
2. `!lw achievements` — list all achievements with unlock status
3. Reward integration with currency, titles, and cosmetics
4. Achievement progress tracking (e.g., "3/5 duels won")

**Paper API Used:**
- YAML persistence for achievement state
- `World.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE)` — unlock sound
- `World.spawnParticle(Particle.TOTEM_OF_UNDYING)` — unlock particles
- Adventure API — formatted achievement text
- Event hooks into existing services

**Acceptance Criteria:**

- [ ] At least 8 distinct achievements defined
- [ ] Achievements unlock automatically when criteria are met
- [ ] Unlock announcement in Twitch chat with sound/particles at NPC
- [ ] `!lw achievements` lists all achievements with status
- [ ] Achievement progress tracked for cumulative goals
- [ ] Unlocked achievements persist across sessions
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
