# Idea D02: Treasure Hunt

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D02`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/d02-treasure-hunt`    |

**Goal:**
A treasure hunt event where hidden chests spawn at random locations. Clues are posted in Twitch chat to guide viewers. Viewers direct their NPCs using compass directions. First NPC to reach a chest claims the treasure.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcTreasureHuntService.kt  # CREATE — hunt game loop
├── twitch/commands/lw/
│   ├── TreasureHuntSubcommand.kt          # CREATE — start hunt
│   └── GoSubcommand.kt                    # CREATE — direct NPC movement
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcTreasureHuntService`:
   - Spawn 3-5 chests at random locations within the city bounds
   - Each chest has loot (configurable: diamonds, emeralds, special items)
   - Post riddle-style clues in Twitch chat every 30 seconds:
     - "The treasure is north of the fountain..."
     - "Look near something made of stone..."
     - Progressive hints get more specific
   - `!lw go north/south/east/west` — direct NPC to walk in that direction
   - `!lw go stop` — stop NPC movement
   - When NPC enters within 3 blocks of a chest: "treasure found!" — claim it
   - Chest despawns, loot awarded to the viewer's NPC stats
2. Mark chests with particles visible from distance (Particle.END_ROD column)
3. Announce findings: "@user found treasure chest #2!"
4. Hunt ends when all chests found or time limit (5 min) expires

**Paper API Used:**
- `World.getBlockAt().setType(Material.CHEST)` — place chest blocks
- `Chest.getInventory().addItem()` — fill chests with loot
- `Mob.getPathfinder().moveTo(Location)` — NPC movement
- `World.spawnParticle(Particle.END_ROD)` — chest beacon
- `Location.distance()` — proximity detection
- `BukkitRunnable` — clue timer and game loop

**Acceptance Criteria:**

- [ ] `!lw treasure` starts the hunt (broadcaster/mod only)
- [ ] Chests spawn at random safe locations
- [ ] Clues posted in Twitch chat periodically
- [ ] `!lw go <direction>` moves viewer's NPC
- [ ] First NPC to reach a chest claims it
- [ ] Findings announced in Twitch chat
- [ ] Hunt ends after all found or time limit
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
