# Task A06: NPC Leveling System

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A06`                          |
| **Status**       | `[x]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/a06-npc-levels`       |

**Goal:**
NPCs gain XP from activities (duels won, quests completed, chat activity) and level up. Higher levels unlock cosmetic perks (titles, trails, auras). Level is displayed in the NPC's name tag.

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcLevelService.kt         # CREATE — XP & level management
│   ├── VillagerNpcKeys.kt                 # MODIFY — add XP/level PDC keys
│   └── VillagerNpcDuelService.kt          # MODIFY — award XP on duel end
├── twitch/commands/lw/
│   ├── LevelSubcommand.kt                # CREATE — check level/XP
│   ├── LwSubcommands.kt                  # MODIFY — register subcommand
│   └── HelpSubcommand.kt                 # MODIFY — update help text
└── LabsWorld.kt                           # MODIFY — wire level service
```

**Acceptance Criteria:**

- [x] NPCs start at level 1 with 0 XP
- [x] Winning a duel awards XP
- [x] Level-up triggers visual and audio feedback
- [x] Name tag shows level: `[Lv.X] Username`
- [x] `!lw level` displays current level and XP to next level
- [x] Level and XP persist across respawns and server restarts
- [x] Build passes: `plugin/gradlew compileKotlin`
- [x] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
