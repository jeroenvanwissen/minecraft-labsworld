# Idea C03: Sub Train Escalation

| Field            | Value                                  |
| ---------------- | -------------------------------------- |
| **ID**           | `C03`                                  |
| **Status**       | `[ ]`                                  |
| **Dependencies** | None                                   |
| **Branch**       | `feature/c03-sub-train-escalation`     |

**Goal:**
Track consecutive subscriptions and trigger escalating in-game effects as the sub train grows. The longer the train, the more dramatic the effects — from particles to weather changes to world-shaking events.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   ├── TwitchEventHandler.kt             # MODIFY — listen for sub events
│   └── SubTrainService.kt                # CREATE — train tracking & effects
```

**Implementation:**

1. Create `SubTrainService`:
   - Track consecutive subs within a time window (e.g., 5 minutes between subs)
   - Reset train if gap exceeds the window
   - Escalation tiers:
     - **1-4 subs**: Fireworks at NPC locations
     - **5-9 subs**: Rain of items (flowers, cookies) + particles
     - **10-14 subs**: Thunderstorm + all NPCs dance (jump repeatedly)
     - **15-19 subs**: Giant firework show + mob parade (passive mobs march through city)
     - **20+ subs**: Earthquake effect + every NPC gets glowing + epic sound
   - Display current train count via boss bar: "Sub Train: 12 subs!"
   - Announce each sub and current train count in Twitch chat
2. Boss bar progress fills up to next tier threshold
3. Configurable tier thresholds and effects
4. Train count resets when timer expires

**Paper API Used:**
- `World.spawn(Firework.class)` — fireworks
- `World.spawnParticle()` — particle effects
- `World.setStorm(true)` / `World.setThundering(true)` — weather
- `Entity.setVelocity(Vector(0, 0.5, 0))` — NPC jump animation
- `Entity.setGlowing(true)` — NPC glow
- `Bukkit.createBossBar()` — train progress display
- `BukkitRunnable` — train timeout timer

**Acceptance Criteria:**

- [ ] Consecutive subs within time window increment the train counter
- [ ] Escalating effects trigger at tier thresholds
- [ ] Boss bar shows current train count and progress to next tier
- [ ] Train resets after timeout (configurable, default 5 min)
- [ ] Each sub is announced with current train count
- [ ] Effects are configurable per tier
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
