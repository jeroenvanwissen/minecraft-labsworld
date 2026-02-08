# Idea C11: NPC Celebration Dance

| Field            | Value                                |
| ---------------- | ------------------------------------ |
| **ID**           | `C11`                                |
| **Status**       | `[ ]`                                |
| **Dependencies** | None                                 |
| **Branch**       | `feature/c11-npc-celebration-dance`  |

**Goal:**
When the channel receives a follow, sub, or raid, all NPCs gather around the player in a circle at a comfortable distance and perform a synchronized dance animation for 30 seconds. Random fireworks launch above the dancing NPCs throughout the celebration. This creates a visually spectacular, community-driven celebration moment on stream.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   ├── TwitchEventHandler.kt             # MODIFY — trigger celebration on follow/sub/raid
│   └── CelebrationDanceService.kt        # CREATE — dance circle orchestration
```

**Implementation:**

1. Listen for Twitch events: `channel.follow`, `channel.subscribe`, `channel.raid`
2. Create `CelebrationDanceService`:
   - Find the player's current location
   - Calculate circle positions around the player (evenly spaced, ~5 block radius)
   - Path all NPCs to their assigned circle positions
   - Once NPCs arrive, start the dance sequence:
     - NPCs rotate/look around, swing arms, jump, and crouch at random intervals
     - Vary animations per NPC so it looks natural, not robotic
   - Launch random fireworks above the circle:
     - Random colors, shapes (ball, star, burst, creeper)
     - Staggered timing (every 1-3 seconds) throughout the 30 seconds
     - Fireworks spawn at varied heights above the NPC circle
   - After 30 seconds, NPCs stop dancing and return to their original positions
3. Scale effect by event type:
   - **Follow**: Standard celebration (all NPCs dance, moderate fireworks)
   - **Sub**: Enhanced celebration (more fireworks, particles around NPCs)
   - **Raid**: Maximum celebration (fireworks storm, NPCs glow, lightning effects)
4. Prevent overlapping celebrations with a cooldown or queue system
5. Configurable: circle radius, dance duration, firework frequency, enable/disable per event type

**Paper API Used:**
- EventSub `channel.follow` / `channel.subscribe` / `channel.raid` — trigger events
- `Villager.getPathfinder().moveTo()` — path NPCs to circle positions
- `Villager.swingMainHand()` / `Villager.jump()` — dance animations
- `Entity.setSneaking()` / `Entity.setPose()` — crouch/pose animations
- `Entity.setRotation()` — spin NPCs during dance
- `World.spawn(Firework.class)` — celebration fireworks
- `FireworkMeta` — random colors, shapes, effects
- `World.spawnParticle()` — extra particles during dance
- `Entity.setGlowing()` — NPC glow for raids
- `BukkitRunnable` — timed dance sequence and cleanup
- `Location.add(cos/sin * radius)` — calculate circle positions

**Acceptance Criteria:**

- [ ] Follow, sub, and raid events trigger the celebration dance
- [ ] All NPCs path to evenly-spaced circle positions around the player
- [ ] NPCs perform varied dance animations for 30 seconds
- [ ] Random fireworks launch above the circle throughout the celebration
- [ ] Effect intensity scales by event type (follow < sub < raid)
- [ ] NPCs return to original positions after the celebration ends
- [ ] Overlapping celebrations are handled gracefully (cooldown/queue)
- [ ] Circle radius, duration, and firework settings are configurable
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
