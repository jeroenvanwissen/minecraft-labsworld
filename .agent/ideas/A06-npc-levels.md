# Idea A06: NPC Leveling System

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A06`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/a06-npc-levels`       |

**Goal:**
NPCs gain XP from activities (duels won, quests completed, chat activity) and level up. Higher levels unlock cosmetic perks (titles, trails, auras). Level is displayed in the NPC's name tag.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcLevelService.kt         # CREATE — XP & level management
│   └── VillagerNpcKeys.kt                 # MODIFY — add XP/level PDC keys
├── twitch/commands/lw/
│   └── LevelSubcommand.kt                # CREATE — check level/XP
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Add `npc_xp` and `npc_level` PDC keys to `VillagerNpcKeys`
2. Create `VillagerNpcLevelService` with:
   - `addXp(villager, amount)` — add XP, check for level-up
   - `getLevel(villager)` — read current level from PDC
   - `getXpForNextLevel(level)` — XP thresholds (e.g., level * 100)
   - Level-up triggers: particles, sound, name tag update with level prefix
3. Hook into duel service to award XP for wins (e.g., 50 XP) and participation (10 XP)
4. Award small XP for viewer chat activity (1 XP per message, capped per minute)
5. Create `LevelSubcommand` — `!lw level` shows current level and XP progress
6. Update NPC name tag to include level: `[Lv.5] ViewerName`

**Paper API Used:**
- `PersistentDataContainer` — store XP and level values
- `Entity.setCustomName(Component)` — update name tag with level
- `World.spawnParticle()` — level-up celebration particles
- `World.playSound()` — level-up sound effect
- `Adventure API Component` — styled level text

**Acceptance Criteria:**

- [ ] NPCs start at level 1 with 0 XP
- [ ] Winning a duel awards XP
- [ ] Level-up triggers visual and audio feedback
- [ ] Name tag shows level: `[Lv.X] Username`
- [ ] `!lw level` displays current level and XP to next level
- [ ] Level and XP persist across respawns and server restarts
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
