# Idea B06: NPC Racing

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B06`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/b06-npc-racing`       |

**Goal:**
A racing game mode where NPCs pathfind through a series of checkpoints. First NPC to reach the finish line wins. Viewers can use `!lw boost` to give their NPC a temporary speed boost (limited uses).

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcRaceService.kt          # CREATE — race game loop
├── twitch/commands/lw/
│   ├── RaceSubcommand.kt                  # CREATE — start race
│   └── BoostSubcommand.kt                 # CREATE — speed boost command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcRaceService`:
   - Define race track as ordered list of checkpoint locations (from config or placed markers)
   - Teleport all NPCs to start line
   - Countdown: 3... 2... 1... GO! (with sounds)
   - NPCs pathfind to next checkpoint in sequence
   - When NPC reaches checkpoint (within 2 blocks), advance to next
   - Track position (1st, 2nd, 3rd) and announce lead changes
   - Random speed variation per NPC (0.9-1.1x) for unpredictability
   - First to cross finish line wins
2. `!lw boost` — gives viewer's NPC a 3-second speed buff (max 2 uses per race)
3. Use particles at checkpoints to make them visible
4. Announce positions in Twitch chat every 2 checkpoints
5. Boss bar shows race progress for leader

**Paper API Used:**
- `Mob.getPathfinder().moveTo(Location, speed)` — pathfind with speed
- `LivingEntity.addPotionEffect(PotionEffectType.SPEED)` — boost
- `AttributeInstance.setBaseValue()` — base speed adjustment
- `Location.distance(Location)` — checkpoint proximity
- `World.spawnParticle()` — checkpoint markers
- `World.playSound(Sound.BLOCK_NOTE_BLOCK_PLING)` — countdown
- `BossBar` — race progress

**Acceptance Criteria:**

- [ ] `!lw race` starts the race (broadcaster/mod only)
- [ ] NPCs pathfind through checkpoints in order
- [ ] `!lw boost` grants temporary speed (limited uses)
- [ ] Positions are tracked and announced
- [ ] Winner is declared with fireworks at finish line
- [ ] Checkpoints are visually marked
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
