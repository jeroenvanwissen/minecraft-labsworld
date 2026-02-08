# Idea D08: Block Shuffle

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D08`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/d08-block-shuffle`    |

**Goal:**
A survival mini-game where the blocks under NPCs randomly change every few seconds. Some blocks are safe (solid), some are dangerous (lava, air). NPCs must move to safe blocks to survive. Last NPC standing wins.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcBlockShuffleService.kt  # CREATE — block shuffle game loop
├── twitch/commands/lw/
│   └── BlockShuffleSubcommand.kt          # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcBlockShuffleService`:
   - Define arena as a flat platform (e.g., 15x15 grid)
   - Teleport all NPCs to random positions on the platform
   - Every 5 seconds: shuffle all blocks on the platform
     - 60% chance: safe block (stone, grass, planks)
     - 20% chance: special block (soul sand = slow, ice = slippery)
     - 10% chance: danger block (magma = damage)
     - 10% chance: void (air = fall through)
   - NPCs try to pathfind to safe blocks
   - NPCs standing on magma take 1 heart/second
   - NPCs that fall through air holes are eliminated
   - Viewers can use `!lw move left/right/forward/back` to nudge NPC direction
   - Last NPC on the platform wins
2. Restore original blocks when game ends
3. Boss bar shows NPCs remaining
4. Announce shuffles and eliminations in Twitch chat

**Paper API Used:**
- `Block.setType(Material)` — shuffle blocks
- `Block.getRelative(BlockFace.DOWN)` — check block under NPC
- `Mob.getPathfinder().moveTo(Location)` — NPC movement
- `LivingEntity.damage()` — magma damage
- `Entity.teleport()` — position NPCs
- `BukkitRunnable` — shuffle timer
- `BossBar` — status display

**Acceptance Criteria:**

- [ ] `!lw blockshuffle` starts the game (broadcaster/mod only)
- [ ] Platform blocks randomize every 5 seconds
- [ ] Dangerous blocks damage or eliminate NPCs
- [ ] NPCs attempt to reach safe blocks
- [ ] `!lw move <direction>` nudges NPC
- [ ] Last NPC standing wins
- [ ] Arena is restored after game ends
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
