# Idea A01: NPC Profession Picker

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `A01`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/a01-npc-professions`      |

**Goal:**
Allow viewers to choose their NPC's villager profession via a `!lw profession <type>` chat command. This gives NPCs visual identity — a farmer NPC looks different from a librarian or blacksmith.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcManager.kt              # MODIFY — add setProfession() helper
├── twitch/commands/lw/
│   └── ProfessionSubcommand.kt            # CREATE — parse & apply profession
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register new subcommand
```

**Implementation:**

1. Create `ProfessionSubcommand` implementing `LwSubcommand`
2. Parse the profession argument against `Villager.Profession` enum values
3. Look up the viewer's linked NPC via `VillagerNpcLinkManager`
4. Set the villager's profession using `Villager.setProfession()`
5. Send confirmation message to Twitch chat
6. Register the subcommand in `LwSubcommands`

**Paper API Used:**
- `Villager.setProfession(Villager.Profession)` — set the profession
- `Villager.Profession` enum — ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER, FISHERMAN, FLETCHER, LEATHERWORKER, LIBRARIAN, MASON, NITWIT, SHEPHERD, TOOLSMITH, WEAPONSMITH

**Acceptance Criteria:**

- [ ] `!lw profession farmer` changes NPC appearance to farmer
- [ ] Invalid profession names return a helpful error with valid options
- [ ] Viewer must have a spawned NPC to use the command
- [ ] Profession persists across NPC respawns (store in PDC or YAML)
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
