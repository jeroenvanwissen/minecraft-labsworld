# Idea D06: Rising Lava

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D06`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/d06-lava-rising`      |

**Goal:**
A "the floor is lava" event in a designated arena. Lava slowly rises from the bottom, and NPCs must climb higher to survive. Last NPC above the lava wins. Viewers can use `!lw jump` to make their NPC leap upward.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcLavaRiseService.kt      # CREATE — rising lava game loop
├── twitch/commands/lw/
│   ├── LavaRiseSubcommand.kt              # CREATE — start event
│   └── JumpSubcommand.kt                  # CREATE — NPC jump command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcLavaRiseService`:
   - Define arena bounds (configurable or preset)
   - Teleport all NPCs to arena floor level
   - Every 5 seconds: raise lava level by 1 block
     - Replace air blocks at current lava level with lava source blocks
   - NPCs pathfind upward — seek higher ground (stairs, platforms)
   - NPCs touching lava take 2 hearts damage per tick
   - `!lw jump` — gives NPC a velocity boost upward (3 uses max)
   - Last NPC above lava wins
   - When game ends: remove all placed lava blocks (restore arena)
2. Track which blocks were placed as lava for cleanup
3. Boss bar shows current lava level: "Lava Level: Y=65 / Y=80"
4. Announce eliminations in Twitch chat

**Paper API Used:**
- `Block.setType(Material.LAVA)` — place lava
- `Block.setType(Material.AIR)` — cleanup
- `Entity.setVelocity(Vector(0, 1.0, 0))` — jump boost
- `Mob.getPathfinder().moveTo(Location)` — seek higher ground
- `EntityDamageEvent` — lava damage tracking
- `BukkitRunnable` — lava rise timer
- `BossBar` — lava level display

**Acceptance Criteria:**

- [ ] `!lw lavarise` starts the event (broadcaster/mod only)
- [ ] Lava visually rises over time
- [ ] NPCs try to climb to higher ground
- [ ] `!lw jump` launches NPC upward (limited uses)
- [ ] NPCs in lava take damage and are eliminated
- [ ] Last NPC alive wins
- [ ] Arena is fully restored after the event
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
