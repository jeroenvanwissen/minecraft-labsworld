# Idea A05: NPC Particle Trails

| Field            | Value                               |
| ---------------- | ----------------------------------- |
| **ID**           | `A05`                               |
| **Status**       | `[ ]`                               |
| **Dependencies** | None                                |
| **Branch**       | `feature/a05-npc-particle-trails`   |

**Goal:**
NPCs leave particle effects behind them as they move. Viewers choose their trail type via `!lw trail <type>`. Available trails: hearts, flames, sparkle, smoke, rainbow. Adds visual flair and identity to NPCs.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcTrailService.kt         # CREATE — trail rendering logic
├── twitch/commands/lw/
│   └── TrailSubcommand.kt                 # CREATE — set trail type
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcTrailService` with a repeating task (every 5 ticks)
2. For each NPC with a trail, check if it moved since last tick
3. If moved, spawn the configured particle at the previous location
4. Map trail names to `Particle` enum values:
   - hearts → `Particle.HEART`
   - flames → `Particle.FLAME`
   - sparkle → `Particle.END_ROD`
   - smoke → `Particle.SMOKE`
   - rainbow → cycle through `Particle.DUST` with rotating RGB
5. Store trail preference in PDC on the NPC entity
6. `!lw trail off` disables the trail

**Paper API Used:**
- `World.spawnParticle(Particle, Location, count, offsetX, offsetY, offsetZ)` — spawn particles
- `Particle.HEART`, `Particle.FLAME`, `Particle.END_ROD`, `Particle.SMOKE`
- `Particle.DUST` with `Particle.DustOptions(Color, size)` — colored dust
- `Entity.getLocation()` — track movement
- `PersistentDataContainer` — store trail preference

**Acceptance Criteria:**

- [ ] `!lw trail hearts` enables heart particles behind the NPC
- [ ] `!lw trail off` disables the trail
- [ ] Particles only appear when NPC is moving
- [ ] Trail preference persists across respawns
- [ ] Performance is acceptable with 20+ NPCs having trails
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
