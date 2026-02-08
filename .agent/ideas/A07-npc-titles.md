# Idea A07: NPC Custom Titles

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A07`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | A06 (NPC Levels, optional)     |
| **Branch**       | `feature/a07-npc-titles`       |

**Goal:**
Viewers earn or select titles displayed above their NPC using a TextDisplay entity. Titles can be earned through achievements (e.g., "Duelist" after 5 duel wins) or selected from unlocked options via `!lw title <name>`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcTitleService.kt         # CREATE — title management & display
│   └── VillagerNpcKeys.kt                 # MODIFY — add title PDC key
├── twitch/commands/lw/
│   └── TitleSubcommand.kt                 # CREATE — select/list titles
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Define a set of available titles with unlock conditions:
   - "Rookie" — default title for all NPCs
   - "Duelist" — win 5 duels
   - "Champion" — win 20 duels
   - "Veteran" — NPC alive for 7+ days
   - "Socialite" — send 100+ chat messages
2. Create `VillagerNpcTitleService` to manage title display:
   - Spawn a `TextDisplay` entity as a passenger on the NPC
   - Show title text above the NPC name in a different color
   - Update when title changes
3. Store active title and unlocked titles in YAML per user
4. `!lw title` lists unlocked titles; `!lw title <name>` equips one

**Paper API Used:**
- `TextDisplay` entity — floating text above NPC
- `Entity.addPassenger(textDisplay)` — attach to NPC
- `Adventure Component` — styled title text with colors
- `PersistentDataContainer` — store active title key

**Acceptance Criteria:**

- [ ] New NPCs get the "Rookie" title by default
- [ ] Title renders as colored text above NPC name
- [ ] `!lw title` lists all unlocked titles
- [ ] `!lw title duelist` sets the active title
- [ ] Locked titles show requirements
- [ ] Title persists across NPC respawns
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
