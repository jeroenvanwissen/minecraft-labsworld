# Idea A03: NPC Emotes

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A03`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/a03-npc-emotes`       |

**Goal:**
Let viewers trigger emote animations on their NPC via `!lw emote <type>`. Emotes include jumping, spinning, sneaking, waving (rapid look-around), and celebrating (jump + firework). Adds personality and fun interaction.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcEmoteService.kt         # CREATE — emote animation logic
├── twitch/commands/lw/
│   └── EmoteSubcommand.kt                 # CREATE — parse & trigger emote
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcEmoteService` with a `playEmote(villager, emoteType)` method
2. Implement emotes as `BukkitRunnable` sequences:
   - **jump**: Set velocity upward + heart particles
   - **spin**: Rotate yaw 360 degrees over ~1 second using teleport
   - **wave**: Rapid head pitch changes + note particles
   - **celebrate**: Jump + spawn a small firework above
   - **sneak**: Toggle AI, set pose, wait, restore
3. Create `EmoteSubcommand` to parse emote name and trigger it
4. Add cooldown per viewer (e.g., 5 seconds) to prevent spam

**Paper API Used:**
- `Entity.setVelocity(Vector)` — jump
- `Entity.teleport(Location)` — rotation changes
- `World.spawnParticle()` — visual effects
- `World.spawn(loc, Firework::class.java)` — celebration firework
- `BukkitRunnable` — animation sequencing

**Acceptance Criteria:**

- [ ] `!lw emote jump` makes the NPC jump with heart particles
- [ ] `!lw emote spin` rotates the NPC 360 degrees
- [ ] `!lw emote celebrate` triggers jump + firework
- [ ] Cooldown prevents spam (configurable, default 5s)
- [ ] Invalid emote name lists available emotes
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
