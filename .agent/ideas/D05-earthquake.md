# Idea D05: Earthquake Event

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D05`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/d05-earthquake`       |

**Goal:**
A chaotic world event that simulates an earthquake. Screen shakes for players (via rapid small teleport offsets), blocks crack and break, mobs panic and scatter. Creates an exciting emergency moment on stream.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/actions/handlers/
│   └── EarthquakeActionHandler.kt         # CREATE — earthquake effect
├── twitch/actions/
│   └── ActionExecutor.kt                  # MODIFY — register handler
```

**Implementation:**

1. Create `EarthquakeActionHandler`:
   - **Screen shake**: Apply rapid small velocity pulses to all players (tiny Y velocity oscillation every 2 ticks)
   - **Block effects**: Spawn `Particle.BLOCK` (cracking) particles on random ground blocks in area
   - **Sound**: Play `Sound.ENTITY_GENERIC_EXPLODE` at low volume randomly across the area
   - **NPC panic**: All NPCs pathfind to random locations rapidly (scatter behavior)
   - **Falling blocks**: Occasionally spawn falling gravel/sand from exposed surfaces
   - **Duration**: Configurable (default 10 seconds)
   - **Intensity**: light (visual only), medium (+ falling blocks), heavy (+ NPC damage)
2. Parameters: `intensity` (light/medium/heavy), `duration_seconds`, `radius`
3. Register as `world.earthquake` action type
4. No permanent block damage (particles and sounds only for light/medium)

**Paper API Used:**
- `Player.setVelocity(Vector)` — screen shake via small velocity offsets
- `World.spawnParticle(Particle.BLOCK, blockData)` — cracking ground
- `World.playSound()` — rumbling sounds
- `World.spawn(FallingBlock.class)` — falling gravel/sand
- `Mob.getPathfinder().moveTo(randomLocation)` — NPC scatter
- `BukkitRunnable` — tick-based shake effect
- `LivingEntity.damage()` — heavy mode NPC damage

**Acceptance Criteria:**

- [ ] Players experience screen shake effect
- [ ] Block cracking particles appear on ground surfaces
- [ ] Rumbling sounds play during the event
- [ ] NPCs scatter in panic during earthquake
- [ ] 3 intensity levels with distinct effects
- [ ] Duration is configurable
- [ ] No permanent world damage in light/medium mode
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
