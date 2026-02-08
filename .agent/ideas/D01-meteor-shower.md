# Idea D01: Meteor Shower

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D01`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/d01-meteor-shower`    |

**Goal:**
A dramatic world event where fiery meteors rain from the sky in a targeted area. Meteors are falling blocks with fire trails and explosion effects on impact. Triggered via chat command or channel point redeem. NPCs must dodge or take damage.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/actions/handlers/
│   └── MeteorShowerActionHandler.kt       # CREATE — meteor effect logic
├── twitch/actions/
│   └── ActionExecutor.kt                  # MODIFY — register handler
```

**Implementation:**

1. Create `MeteorShowerActionHandler`:
   - Target area: configurable radius around a player or fixed location
   - Spawn falling blocks (MAGMA_BLOCK or NETHERRACK) at Y+40 above random positions
   - Attach fire particles trailing behind each meteor
   - On impact: small explosion effect (particles + sound, no block damage)
   - Damage NPCs within 3 blocks of impact (configurable, default 2 hearts)
   - Spawn 1 meteor every 0.5 seconds for the duration
   - Duration: configurable (default 15 seconds = ~30 meteors)
2. Parameters: `radius`, `duration_seconds`, `damage`, `target_player`
3. Register as `world.meteor_shower` action type
4. Clean up fallen blocks after event ends (remove placed blocks)

**Paper API Used:**
- `World.spawn(FallingBlock.class)` — falling meteor blocks
- `FallingBlock.setDropItem(false)` — no item drops
- `FallingBlock.setHurtEntities(true)` — damage on impact
- `World.spawnParticle(Particle.FLAME / LAVA)` — fire trail
- `World.createExplosion()` or `World.spawnParticle(Particle.EXPLOSION)` — impact
- `World.playSound(Sound.ENTITY_GENERIC_EXPLODE)` — impact sound
- `BukkitRunnable` — meteor spawn timer
- `EntityChangeBlockEvent` — track landed blocks for cleanup

**Acceptance Criteria:**

- [ ] Meteors visually fall from the sky with fire trails
- [ ] Impact creates explosion particles and sound
- [ ] NPCs in blast radius take configurable damage
- [ ] Duration and intensity are configurable
- [ ] Fallen blocks are cleaned up after the event
- [ ] Can be triggered via action system (commands/redeems)
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
