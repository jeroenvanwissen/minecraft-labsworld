# Idea B02: King of the Hill

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `B02`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/b02-king-of-the-hill`     |

**Goal:**
A competitive game mode where NPCs race to occupy and hold a central "hill" point. The NPC that stays on the hill longest (cumulative) wins. Other NPCs try to knock the current king off. Triggered via `!lw koth`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcKothService.kt          # CREATE — KOTH game loop
├── twitch/commands/lw/
│   └── KothSubcommand.kt                  # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcKothService`:
   - Define hill as a center block + 3-block radius
   - Teleport all NPCs to random positions around the hill
   - Every tick: check which NPC is closest to hill center
   - The "king" NPC gets 1 point per second on the hill
   - Other NPCs pathfind toward the hill, pushing the king away
   - NPCs attack the current king with knockback (low damage, high knockback)
   - First NPC to reach target score (e.g., 60 points = 60 seconds on hill) wins
2. Display current scores via boss bar showing top 3 NPCs
3. Announce king changes in Twitch chat
4. Mark the hill with glowing particles (beacon-like column)
5. Game duration cap: 5 minutes, highest score wins if no one reaches target

**Paper API Used:**
- `Mob.getPathfinder().moveTo(Location)` — pathfind to hill
- `Entity.setVelocity(Vector)` — knockback effect
- `BossBar` — display scores for online players
- `World.spawnParticle(Particle.END_ROD, ...)` — hill marker particles
- `BukkitRunnable` — game tick loop
- `Location.distance(Location)` — proximity check

**Acceptance Criteria:**

- [ ] `!lw koth` starts the game (broadcaster/mod only)
- [ ] Hill is visually marked with particles
- [ ] NPCs pathfind to the hill and compete for control
- [ ] Score tracking is accurate (1 point per second on hill)
- [ ] King changes are announced in Twitch chat
- [ ] Winner is declared when score target is reached or time expires
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
