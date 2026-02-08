# Idea B01: Battle Royale

| Field            | Value                            |
| ---------------- | -------------------------------- |
| **ID**           | `B01`                            |
| **Status**       | `[ ]`                            |
| **Dependencies** | None                             |
| **Branch**       | `feature/b01-battle-royale`      |

**Goal:**
A battle royale game mode where all spawned NPCs are teleported to an arena and fight each other. The play area shrinks over time (marked by a visible border). Last NPC standing wins. Triggered by broadcaster via `!lw battleroyale`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcBattleRoyaleService.kt  # CREATE — game loop & arena logic
├── twitch/commands/lw/
│   └── BattleRoyaleSubcommand.kt          # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcBattleRoyaleService` managing the game state:
   - **Setup phase** (10s): Teleport all linked NPCs to arena center, announce participants
   - **Combat phase**: NPCs target nearest other NPC, pathfind + attack (reuse attack logic from duel service)
   - **Shrink phase**: Every 30 seconds, reduce the play radius (push NPCs inward with damage outside zone)
   - **Elimination**: When NPC health hits 0, remove from game, announce elimination
   - **Victory**: Last NPC alive wins, fireworks + chat announcement
2. Track arena bounds as a center point + radius
3. NPCs outside bounds take 1 heart/second damage
4. Use `World.spawnParticle(Particle.DUST)` ring to visualize boundary
5. Configure arena center via spawn point or config

**Paper API Used:**
- `Mob.getPathfinder().moveTo(entity)` — NPC targeting
- `LivingEntity.damage(amount, source)` — combat damage
- `Entity.teleport(Location)` — teleport to arena
- `World.spawnParticle(Particle.DUST, ...)` — boundary visualization
- `BukkitRunnable` — game loop timer
- `World.playSound()` — elimination sounds

**Acceptance Criteria:**

- [ ] `!lw battleroyale` starts the game (broadcaster only)
- [ ] All linked NPCs are teleported to the arena
- [ ] NPCs fight each other autonomously
- [ ] Play area shrinks over time with visible boundary
- [ ] NPCs outside the zone take damage
- [ ] Eliminations are announced in Twitch chat
- [ ] Last NPC standing is declared winner with fireworks
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
