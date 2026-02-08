# Idea B07: Capture the Flag

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `B07`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/b07-capture-the-flag`     |

**Goal:**
A team-based CTF game mode. Two teams, each with a flag (banner on an armor stand). NPCs must grab the enemy flag and bring it back to their base. First team to capture 3 flags wins. Viewers can direct their NPC with `!lw attack` or `!lw defend`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcCtfService.kt           # CREATE — CTF game loop
├── twitch/commands/lw/
│   ├── CtfSubcommand.kt                   # CREATE — start game
│   └── CtfRoleSubcommand.kt              # CREATE — attack/defend role
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommands
```

**Implementation:**

1. Create `VillagerNpcCtfService`:
   - Two bases at configurable locations, each with a banner (flag)
   - Split NPCs into two teams (random or pre-selected)
   - **Attacker NPCs**: Pathfind to enemy flag, pick it up (banner on head), run back to base
   - **Defender NPCs**: Patrol near own flag, attack enemy NPCs in range
   - Flag carrier: NPC carries banner on head, moves slower (0.8x speed)
   - If flag carrier is killed: flag drops at death location, resets after 30s if not picked up
   - Capture: Carrier reaches own base with enemy flag → score + flag resets
   - First to 3 captures wins
2. `!lw attack` / `!lw defend` — set NPC role during game
3. Announce captures, kills, and flag status in Twitch chat
4. Boss bar shows score: "Red 1 - 2 Blue"

**Paper API Used:**
- `EntityEquipment.setHelmet(bannerItem)` — flag on NPC head
- `Mob.getPathfinder().moveTo(Location/Entity)` — pathfinding
- `LivingEntity.damage()` — combat between NPCs
- `Entity.teleport()` — flag reset
- `BossBar` — score display
- `Scoreboard teams` — team glow colors
- `World.spawnParticle()` — flag location markers

**Acceptance Criteria:**

- [ ] `!lw ctf` starts the game (broadcaster/mod only)
- [ ] Two teams with colored armor and glow
- [ ] Flag carriers visually carry a banner on their head
- [ ] `!lw attack` / `!lw defend` changes NPC behavior
- [ ] Captures are tracked and announced
- [ ] First to 3 captures wins
- [ ] Flags reset properly when dropped
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
