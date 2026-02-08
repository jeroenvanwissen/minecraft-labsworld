# Idea D09: Supply Drops

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D09`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/d09-supply-drops`     |

**Goal:**
Periodic supply drops fall from the sky at random locations. A parachute effect (slow-falling chicken/armor stand with chest) descends, and the first NPC to reach it claims the loot. Creates exciting "race to the drop" moments.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/actions/handlers/
│   └── SupplyDropActionHandler.kt         # CREATE — supply drop logic
├── twitch/actions/
│   └── ActionExecutor.kt                  # MODIFY — register handler
```

**Implementation:**

1. Create `SupplyDropActionHandler`:
   - Announce in Twitch chat: "Supply drop incoming! Location: near the [landmark]"
   - Spawn a chicken (slow fall) with a chest minecart as passenger at Y+50
   - Or: armor stand holding a chest block, with Slow Falling effect
   - Chicken/stand descends slowly (~3 seconds to land)
   - Mark landing zone with smoke particles and beacon beam
   - When it lands: place a chest block with random loot
   - First NPC within 3 blocks claims it — loot awarded to their stats
   - Unclaimed drops despawn after 60 seconds
2. Parameters: `loot_tier` (common/rare/epic), `announce` (true/false)
3. Register as `world.supply_drop` action type
4. Can run on a timer for periodic drops during stream
5. Loot tiers:
   - Common: food, basic items
   - Rare: iron gear, potions
   - Epic: diamond gear, enchanted items

**Paper API Used:**
- `World.spawn(Chicken.class)` — slow-falling carrier
- `Entity.addPassenger(entity)` — chest on carrier
- `LivingEntity.addPotionEffect(PotionEffectType.SLOW_FALLING)` — descent
- `Entity.setGravity(false)` + manual descent — alternative approach
- `World.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE)` — landing zone smoke
- `Block.setType(Material.CHEST)` — place chest
- `Chest.getInventory().addItem()` — fill with loot
- `Location.distance()` — NPC proximity check
- `BukkitRunnable` — descent and despawn timer

**Acceptance Criteria:**

- [ ] Supply drop descends visually from the sky
- [ ] Landing zone is marked with particles
- [ ] Drop announced in Twitch chat with location hint
- [ ] First NPC to reach the drop claims it
- [ ] Multiple loot tiers with configurable contents
- [ ] Unclaimed drops auto-despawn
- [ ] Can be triggered via action system or periodic timer
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
