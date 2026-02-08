# Idea B05: Zombie Siege

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B05`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/b05-zombie-siege`     |

**Goal:**
A wave-based survival game where NPCs defend a point against increasingly difficult waves of zombies. Viewers can contribute resources via chat commands during waves. Survive all waves to win.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcSiegeService.kt         # CREATE — siege game loop
├── twitch/commands/lw/
│   └── SiegeSubcommand.kt                 # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcSiegeService`:
   - Define defense point (configurable or spawn point area)
   - NPCs are teleported to the defense zone
   - **Wave system** (5-10 waves):
     - Wave 1: 5 zombies, slow, low HP
     - Wave 3: Add skeletons, increase count
     - Wave 5: Add creepers, increase speed
     - Wave 7: Add witches and vindicators
     - Wave 10: Warden or Ravager mini-boss
   - Between waves (15s): announce next wave, heal NPCs partially
   - NPCs auto-target nearest hostile mob
   - Track NPC eliminations; eliminated NPCs can respawn next wave (costs points)
   - Zombie kills add to a shared team score
2. Display current wave and score via boss bar
3. `!lw heal` during siege costs viewer points but heals their NPC

**Paper API Used:**
- `World.spawn(loc, Zombie.class / Skeleton.class / etc.)` — spawn wave mobs
- `Mob.setTarget(entity)` — mobs target NPCs
- `Mob.getPathfinder().moveTo(entity)` — NPCs fight back
- `LivingEntity.setHealth()` — heal NPCs between waves
- `BossBar` — wave/score display
- `BukkitRunnable` — wave timer
- `EntityDeathEvent` — track mob kills

**Acceptance Criteria:**

- [ ] `!lw siege` starts the game (broadcaster/mod only)
- [ ] Zombie waves spawn with escalating difficulty
- [ ] NPCs fight mobs autonomously
- [ ] Between-wave breaks with partial healing
- [ ] Boss bar shows current wave and enemy count
- [ ] Win condition: survive all waves; lose condition: all NPCs eliminated
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
