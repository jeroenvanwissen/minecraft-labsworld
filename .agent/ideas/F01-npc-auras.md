# Idea F01: NPC Auras

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `F01`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/f01-npc-auras`        |

**Goal:**
Persistent particle auras that surround NPCs based on their rank, level, or status. Different aura styles provide visual distinction. Viewers can select their aura via `!lw aura <type>` (if unlocked).

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   ├── VillagerNpcAuraService.kt          # CREATE — aura rendering
│   └── VillagerNpcKeys.kt                 # MODIFY — add aura PDC key
├── twitch/commands/lw/
│   └── AuraSubcommand.kt                  # CREATE — select aura
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcAuraService`:
   - Repeating task (every 5 ticks) renders auras for all NPCs with one set
   - Aura types:
     - **flame**: Ring of flame particles orbiting NPC
     - **frost**: Snowflake particles drifting around NPC
     - **enchant**: Enchantment table particles spiraling upward
     - **void**: Portal particles swirling at feet
     - **divine**: End rod particles floating upward (golden glow)
     - **toxic**: Poison/drip particles (green)
   - Render using parametric equations for circular/spiral motion
   - Only render for nearby players (within 30 blocks) for performance
2. `!lw aura flame` — set aura (requires unlock)
3. `!lw aura off` — disable aura
4. Store aura preference in PDC/YAML
5. Performance safeguard: skip rendering if >20 NPCs have auras

**Paper API Used:**
- `World.spawnParticle(Particle, Location, count, ...)` — all particle types
- `Particle.FLAME`, `SNOWFLAKE`, `ENCHANT`, `PORTAL`, `END_ROD`, `ENTITY_EFFECT`
- `Math.sin()` / `Math.cos()` — circular motion calculations
- `Entity.getLocation()` — NPC position
- `World.getNearbyPlayers()` — render only for nearby players
- `PersistentDataContainer` — store aura preference
- `BukkitRunnable` — render loop

**Acceptance Criteria:**

- [ ] At least 6 distinct aura styles
- [ ] Auras render as orbiting/floating particles around NPCs
- [ ] `!lw aura <type>` sets the active aura
- [ ] `!lw aura off` disables the aura
- [ ] Aura preference persists across respawns
- [ ] Performance is acceptable with multiple auras active
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
