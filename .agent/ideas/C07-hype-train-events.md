# Idea C07: Hype Train World Events

| Field            | Value                                |
| ---------------- | ------------------------------------ |
| **ID**           | `C07`                                |
| **Status**       | `[ ]`                                |
| **Dependencies** | None                                 |
| **Branch**       | `feature/c07-hype-train-events`      |

**Goal:**
Twitch Hype Train milestones trigger escalating in-game world events. Each level of the hype train unlocks a more dramatic effect, encouraging viewers to keep the hype going.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   ├── TwitchEventHandler.kt             # MODIFY — listen for hype train events
│   └── HypeTrainService.kt               # CREATE — hype train effect logic
```

**Implementation:**

1. Listen for Twitch EventSub hype train events:
   - `channel.hype_train.begin` — train started
   - `channel.hype_train.progress` — level advanced
   - `channel.hype_train.end` — train ended
2. Create `HypeTrainService`:
   - Track current hype train level
   - Per-level effects:
     - **Level 1**: Fireworks at city center + announcement
     - **Level 2**: All NPCs get speed boost for 60 seconds
     - **Level 3**: Rain of items (diamonds, emeralds, gold) near NPCs
     - **Level 4**: All NPCs glow + thunderstorm
     - **Level 5**: Massive firework show + spawn beacon beam at city center
   - On train end: display final level achieved, celebrate contributors
3. Show hype train progress via boss bar with level colors
4. Each level announced in Twitch chat with in-game effect description

**Paper API Used:**
- EventSub hype train events — Twitch4J event listeners
- `World.spawn(Firework.class)` — fireworks
- `LivingEntity.addPotionEffect(PotionEffectType.SPEED)` — speed boost
- `World.dropItemNaturally()` — item rain
- `Entity.setGlowing(true)` — NPC glow
- `World.setThundering(true)` — storm
- `Bukkit.createBossBar()` — hype train display
- `World.spawnParticle(Particle.END_ROD)` — beacon beam effect

**Acceptance Criteria:**

- [ ] Hype train start triggers in-game announcement
- [ ] Each level triggers a distinct, escalating effect
- [ ] Boss bar shows current hype train level
- [ ] Effects are configurable per level
- [ ] Hype train end shows summary
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
