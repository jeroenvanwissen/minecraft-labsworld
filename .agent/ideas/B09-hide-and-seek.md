# Idea B09: Hide and Seek

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B09`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/b09-hide-and-seek`    |

**Goal:**
A hide-and-seek game mode. One NPC is the "seeker" while all others scatter and hide. The seeker NPC searches for hiding NPCs. Found NPCs become seekers too. Last NPC found wins. Viewers can use `!lw hint` to get a directional hint.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcHideSeekService.kt      # CREATE — hide & seek game loop
├── twitch/commands/lw/
│   └── HideSeekSubcommand.kt              # CREATE — start command
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcHideSeekService`:
   - **Hiding phase** (30s): All NPCs scatter to random locations within arena bounds
     - NPCs pathfind to random spots, then AI is disabled (they "hide")
   - **Seeking phase**: One random NPC becomes the seeker (glowing, speed boost)
     - Seeker pathfinds toward nearest hidden NPC
     - When seeker gets within 3 blocks of a hider: "found!" — hider becomes seeker too
   - **Chain seeking**: New seekers also hunt remaining hiders
   - Last NPC found wins
   - Time limit: 3 minutes, remaining hiders all win
2. Seekers glow red, hiders are invisible (no glow)
3. `!lw hint` — the seeker's owner gets a compass direction toward nearest hider
4. Announce findings in Twitch chat: "@seeker found @hider!"

**Paper API Used:**
- `Mob.getPathfinder().moveTo(Location)` — scatter and seek
- `Entity.setAI(false)` — freeze hiders in place
- `Entity.setGlowing(true)` — mark seekers
- `PotionEffect(PotionEffectType.SPEED)` — seeker speed boost
- `Location.distance()` — proximity detection
- `Vector` math — directional hints
- `BukkitRunnable` — game loop

**Acceptance Criteria:**

- [ ] `!lw hideseek` starts the game (broadcaster/mod only)
- [ ] NPCs scatter during hiding phase
- [ ] Seeker NPC actively hunts hidden NPCs
- [ ] Found NPCs become seekers (chain seeking)
- [ ] Last NPC found wins
- [ ] `!lw hint` provides directional clue to seeker
- [ ] Time limit prevents infinite games
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
