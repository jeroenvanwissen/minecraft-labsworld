# Idea C10: Viewer Quests

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `C10`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | A06 (NPC Levels, optional)     |
| **Branch**       | `feature/c10-viewer-quests`    |

**Goal:**
A quest system where viewers accept and complete quests via Twitch chat. Quests include objectives like "win a duel", "survive a siege", or "chat 50 messages today". Completing quests earns XP/currency. Refreshes daily.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcQuestService.kt         # CREATE — quest management
├── twitch/commands/lw/
│   ├── QuestSubcommand.kt                 # CREATE — view/accept quests
│   └── QuestsListSubcommand.kt            # CREATE — list available quests
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcQuestService`:
   - Define quest templates with objectives and rewards:
     - "Chatterbox" — send 50 chat messages today → 50 XP
     - "Gladiator" — win 1 duel → 100 XP
     - "Survivor" — survive a siege/battle royale → 75 XP
     - "Social Butterfly" — use 3 emotes → 30 XP
     - "Explorer" — have NPC visit 5 different locations → 40 XP
   - Pick 3 random quests per day (rotate at midnight UTC)
   - Track quest progress per viewer in YAML
   - Auto-check completion when relevant events occur
2. `!lw quest` — show active quests and progress
3. `!lw quest accept <id>` — accept a quest (max 3 active)
4. Announce quest completion in Twitch chat with reward
5. Daily quest refresh announcement

**Paper API Used:**
- YAML persistence for quest progress
- Event hooks into existing services (duels, siege, chat)
- `BukkitRunnable` — daily quest rotation timer
- Adventure API — formatted quest descriptions

**Acceptance Criteria:**

- [ ] 3 random quests available daily
- [ ] `!lw quest` shows available quests with progress
- [ ] Quest objectives tracked automatically
- [ ] Quest completion awards XP/currency
- [ ] Quests refresh daily
- [ ] Progress persists across sessions
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
