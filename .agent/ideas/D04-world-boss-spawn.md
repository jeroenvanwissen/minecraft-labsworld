# Idea D04: World Boss

| Field            | Value                            |
| ---------------- | -------------------------------- |
| **ID**           | `D04`                            |
| **Status**       | `[ ]`                            |
| **Dependencies** | None                             |
| **Branch**       | `feature/d04-world-boss-spawn`   |

**Goal:**
Spawn a massive, custom-attributed boss mob that roams the city. All NPCs and players can fight it. The boss has amplified stats, custom name, and a boss bar. Defeating it awards all participating viewers. Can be triggered on a schedule or via redeem.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/actions/handlers/
│   └── WorldBossActionHandler.kt          # CREATE — boss spawn & management
├── twitch/actions/
│   └── ActionExecutor.kt                  # MODIFY — register handler
```

**Implementation:**

1. Create `WorldBossActionHandler`:
   - Spawn a configurable mob type (default: Warden) at city center
   - Apply custom attributes:
     - HP: 500 hearts (configurable, scales with NPC count)
     - Name: "World Boss" (configurable) with TextDisplay showing HP
     - Speed: 0.8x normal (slow but deadly)
     - Attack damage: 4 hearts per hit
   - Create boss bar showing HP percentage
   - Boss targets nearest NPC, switches every 10 seconds
   - NPCs auto-aggro on the boss (pathfind + attack)
   - When boss dies: massive firework show, loot explosion, victory announcement
   - Track damage per NPC for MVP announcement
2. Parameters: `mob_type`, `hp_multiplier`, `name`, `target_player` (to spawn near)
3. Register as `world.boss` action type
4. Only one boss at a time (prevent stacking)

**Paper API Used:**
- `World.spawn(loc, entityClass)` — spawn boss mob
- `AttributeInstance.setBaseValue()` — HP, speed, damage
- `Bukkit.createBossBar()` — HP display
- `LivingEntity.setCustomName(Component)` — boss name
- `Mob.setTarget(entity)` — target switching
- `EntityDamageByEntityEvent` — track damage per attacker
- `EntityDeathEvent` — trigger victory sequence
- `World.spawn(Firework.class)` — celebration

**Acceptance Criteria:**

- [ ] Boss mob spawns with amplified stats and custom name
- [ ] Boss bar shows HP to all online players
- [ ] NPCs engage the boss automatically
- [ ] Boss switches targets periodically
- [ ] Defeating the boss triggers celebration and MVP announcement
- [ ] Only one boss can be active at a time
- [ ] Boss stats are configurable
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
