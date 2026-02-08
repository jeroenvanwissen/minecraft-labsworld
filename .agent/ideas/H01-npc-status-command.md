# Idea H01: NPC Status Command

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `H01`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/h01-npc-status-command`   |

**Goal:**
A `!lw status` command that shows viewers comprehensive information about their NPC: health, location, profession, equipment, active effects, and any ongoing activities (in a duel, idle, etc.). Essential QoL for viewers to know their NPC's state.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/commands/lw/
│   └── StatusSubcommand.kt                # CREATE — status display
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `StatusSubcommand`:
   - Look up viewer's linked NPC
   - Gather NPC information:
     - **Health**: current HP / max HP (hearts)
     - **Location**: X, Y, Z coordinates (rounded)
     - **Profession**: current villager profession
     - **Equipment**: list worn armor and held items
     - **Status**: idle, in duel, in siege, swarming, etc.
     - **Level**: if leveling system exists
     - **Balance**: if currency system exists
     - **Trail/Aura**: active cosmetics
   - Format as a concise multi-line response
   - If NPC doesn't exist: "You don't have an NPC yet! Redeem channel points to spawn one."
2. `!lw status` — show own NPC status
3. `!lw status @viewer` — show another viewer's NPC (public info only)
4. Response in Twitch chat (keep under message limit)

**Paper API Used:**
- `LivingEntity.getHealth()` / `getMaxHealth()` — health info
- `Entity.getLocation()` — position
- `Villager.getProfession()` — profession
- `LivingEntity.getEquipment()` — equipment slots
- `LivingEntity.getActivePotionEffects()` — active effects
- `PersistentDataContainer` — read custom data (level, etc.)
- `VillagerNpcLinkManager` — resolve NPC

**Acceptance Criteria:**

- [ ] `!lw status` shows comprehensive NPC info
- [ ] Shows health, location, profession, and status
- [ ] Works when NPC exists and when it doesn't (helpful error)
- [ ] `!lw status @viewer` shows public info for another NPC
- [ ] Response fits within Twitch chat message limits
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
