# Idea D10: Seasonal Events

| Field            | Value                            |
| ---------------- | -------------------------------- |
| **ID**           | `D10`                            |
| **Status**       | `[ ]`                            |
| **Dependencies** | None                             |
| **Branch**       | `feature/d10-seasonal-events`    |

**Goal:**
Automatic themed decorations and effects that activate during real-world holidays. Halloween brings spooky mobs and pumpkins, Christmas brings snow and presents, etc. Adds seasonal freshness to the city without manual setup.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── SeasonalEventService.kt            # CREATE — seasonal detection & effects
└── twitch/actions/
    └── ActionExecutor.kt                  # MODIFY — register seasonal actions
```

**Implementation:**

1. Create `SeasonalEventService`:
   - Check real-world date on server startup and daily
   - Activate season-appropriate effects:
   - **Halloween** (Oct 25 - Nov 1):
     - Spawn bats around city
     - Replace some lights with jack-o-lanterns
     - NPCs get pumpkin helmets
     - Spooky sound effects at night
   - **Christmas** (Dec 20 - Dec 26):
     - Snow particles everywhere
     - NPCs get red leather armor (Santa outfit)
     - Present chests spawn near NPCs daily
     - Jingle bell sounds
   - **New Year** (Dec 31 - Jan 1):
     - Midnight firework show (at Minecraft midnight)
     - Countdown text display
   - **Valentine's** (Feb 14):
     - Heart particles around all NPCs
     - Pink/red colored chat bubbles
2. Seasonal effects configurable and can be toggled off
3. Each season has an enable/disable in config
4. Clean up decorations when season ends

**Paper API Used:**
- `java.time.LocalDate` — check current date
- `World.spawn(Bat.class)` — Halloween bats
- `Block.setType(Material.JACK_O_LANTERN)` — Halloween decor
- `EntityEquipment.setHelmet(pumpkinItem)` — NPC pumpkin heads
- `World.spawnParticle(Particle.SNOW_SHOVEL)` — Christmas snow
- `World.playSound()` — seasonal sounds
- `World.spawn(Firework.class)` — New Year fireworks
- `BukkitRunnable` — daily season check

**Acceptance Criteria:**

- [ ] Seasonal events activate automatically based on real-world dates
- [ ] At least 4 seasons implemented (Halloween, Christmas, New Year, Valentine's)
- [ ] Each season has distinct visual effects
- [ ] NPCs get seasonal cosmetics automatically
- [ ] Seasons are configurable and can be disabled
- [ ] Decorations clean up when season ends
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
