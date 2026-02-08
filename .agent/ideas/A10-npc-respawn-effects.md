# Idea A10: NPC Respawn Effects

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `A10`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/a10-npc-respawn-effects`  |

**Goal:**
When an NPC respawns (after a duel loss, redeem, or reconnect), play a dramatic entrance with particles, sound effects, and a brief invincibility glow. Makes respawns feel exciting rather than just appearing silently.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcRespawnEffectService.kt # CREATE — respawn animation
└── npc/
    └── VillagerNpcManager.kt              # MODIFY — trigger effect on spawn
```

**Implementation:**

1. Create `VillagerNpcRespawnEffectService` with `playRespawnEffect(villager)`:
   - Phase 1 (0-20 ticks): Spiral particle effect at spawn location (PORTAL particles)
   - Phase 2 (20 ticks): Spawn the NPC, play `Sound.ENTITY_ENDERMAN_TELEPORT`
   - Phase 3 (20-60 ticks): Glowing effect on NPC (`setGlowing(true)`) + TOTEM particles
   - Phase 4 (60 ticks): Remove glow, NPC is ready
2. Use `BukkitRunnable` to sequence the phases
3. During glow phase, NPC is visually marked as "just spawned"
4. Hook into `VillagerNpcManager.createLinkedNpc()` to trigger the effect

**Paper API Used:**
- `World.spawnParticle(Particle.PORTAL / TOTEM_OF_UNDYING, ...)` — visual effects
- `World.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, ...)` — sound effect
- `Entity.setGlowing(true/false)` — glow outline
- `BukkitRunnable` — animation sequencing
- `PotionEffect(PotionEffectType.GLOWING, duration, amplifier)` — alternative glow

**Acceptance Criteria:**

- [ ] Respawning NPC triggers particle spiral before appearing
- [ ] Teleport sound plays at spawn location
- [ ] NPC glows for ~2 seconds after spawning
- [ ] Effect plays on duel respawn, redeem spawn, and server restart recovery
- [ ] Effect doesn't block or delay NPC functionality
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
