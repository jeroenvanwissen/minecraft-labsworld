# Idea B04: Boss Fight

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B04`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/b04-boss-fight`       |

**Goal:**
Spawn a powerful custom boss mob that all NPCs must team up to defeat. The boss has high HP, special attacks, and multiple phases. NPCs fight cooperatively, and all surviving NPCs share the victory. Triggered via `!lw boss` or channel point redeem.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcBossFightService.kt     # CREATE — boss fight game loop
├── twitch/commands/lw/
│   └── BossFightSubcommand.kt             # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcBossFightService`:
   - Spawn a Wither, Ravager, or Iron Golem as the boss (configurable)
   - Apply attributes: scaled HP (e.g., 20 HP per participating NPC), speed boost
   - Set custom name: "World Boss" with boss bar showing HP
   - All NPCs pathfind to and attack the boss
   - Boss targets random NPCs and attacks them
   - Phase 2 (50% HP): Boss gains speed, spawns minion mobs
   - Phase 3 (25% HP): Boss gets strength effect, particles intensify
   - When boss dies: fireworks, victory announcement, all surviving NPCs celebrated
   - When all NPCs die: boss despawns, failure announcement
2. Use `BossBar` to show boss HP to all online players
3. Announce phase transitions and NPC eliminations in Twitch chat

**Paper API Used:**
- `World.spawn(loc, entityClass)` — spawn boss entity
- `AttributeInstance.setBaseValue()` — set boss HP, speed, damage
- `Bukkit.createBossBar()` — HP display
- `Mob.getPathfinder().moveTo(entity)` — NPC targeting
- `Mob.setTarget(entity)` — boss targeting NPCs
- `LivingEntity.addPotionEffect()` — boss phase buffs
- `World.spawnParticle()` — phase transition effects

**Acceptance Criteria:**

- [ ] `!lw boss` spawns a boss mob with scaled HP
- [ ] All NPCs automatically engage the boss
- [ ] Boss bar shows remaining HP to all players
- [ ] Boss has at least 2 phase transitions with escalating difficulty
- [ ] Victory/failure announced in Twitch chat
- [ ] Boss despawns cleanly when fight ends
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
