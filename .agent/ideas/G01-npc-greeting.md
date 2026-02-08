# Idea G01: NPC Greeting

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `G01`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/g01-npc-greeting`     |

**Goal:**
When a viewer sends their first chat message of the stream session, their NPC performs a greeting animation — a jump, wave particles, and a brief welcome bubble. Makes NPCs feel alive and responsive to their viewer's presence.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcGreetingService.kt      # CREATE — greeting detection & animation
├── twitch/
│   └── TwitchEventHandler.kt             # MODIFY — hook into chat for greeting check
```

**Implementation:**

1. Create `VillagerNpcGreetingService`:
   - Track which viewers have been "greeted" this session (reset on plugin reload)
   - On first chat message from a viewer with a linked NPC:
     - Make NPC jump (set velocity upward)
     - Spawn heart particles around NPC
     - Show chat bubble: "Hi @username!"
     - Play `Sound.ENTITY_VILLAGER_CELEBRATE` at NPC location
   - Only trigger once per session per viewer
   - Configurable toggle to enable/disable
2. Hook into `CommandDispatcher` chat message handling
3. Check if message sender has a linked NPC and hasn't been greeted yet
4. Greeting animation runs on main thread (entity manipulation)

**Paper API Used:**
- `Entity.setVelocity(Vector(0, 0.4, 0))` — jump animation
- `World.spawnParticle(Particle.HEART)` — love particles
- `World.playSound(Sound.ENTITY_VILLAGER_CELEBRATE)` — celebration sound
- Existing `ChatBubbleService.showBubble()` — greeting text
- `Set<String>` — track greeted viewers (reset on reload)

**Acceptance Criteria:**

- [ ] NPC greets on viewer's first message of the session
- [ ] Greeting includes jump, particles, sound, and chat bubble
- [ ] Only triggers once per session per viewer
- [ ] Viewer must have a linked NPC for greeting to fire
- [ ] Feature can be toggled on/off in config
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
