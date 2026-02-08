# Idea A08: NPC Idle Behaviors

| Field            | Value                                |
| ---------------- | ------------------------------------ |
| **ID**           | `A08`                                |
| **Status**       | `[ ]`                                |
| **Dependencies** | None                                 |
| **Branch**       | `feature/a08-npc-idle-animations`    |

**Goal:**
NPCs perform idle behaviors when their viewer hasn't chatted recently. Instead of standing still, they wander, look around, or sit. Makes the city feel alive. Behaviors change based on time of day.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcIdleService.kt          # CREATE — idle behavior logic
└── npc/
    └── VillagerNpcLinkManager.kt          # MODIFY — track last chat time
```

**Implementation:**

1. Track last chat timestamp per user in `VillagerNpcLinkManager`
2. Create `VillagerNpcIdleService` with a repeating task (every 100 ticks / 5 sec)
3. For NPCs idle > 5 minutes, randomly pick an idle behavior:
   - **Wander**: Pathfind to random nearby location within 10 blocks
   - **Look around**: Rotate head to look at nearby entities
   - **Sit**: Remove AI, spawn a stair block below, "sit" (teleport into block)
   - **Daytime**: Prefer wander and look around
   - **Nighttime**: Prefer standing near light sources or "sleeping" (lying down)
4. When viewer chats again, cancel idle behavior and return NPC to normal
5. Configurable idle timeout in config

**Paper API Used:**
- `Mob.getPathfinder().moveTo(Location)` — wander pathfinding
- `Entity.setAI(boolean)` — disable movement during sit
- `LivingEntity.lookAt(Entity)` — look at entities (Paper API)
- `World.getTime()` — check day/night cycle
- `World.getNearbyEntities()` — find nearby entities to look at
- `BukkitRunnable` — repeating task scheduler

**Acceptance Criteria:**

- [ ] NPCs start idle behavior after 5 minutes of viewer inactivity
- [ ] At least 3 distinct idle behaviors are implemented
- [ ] Viewer chatting cancels idle behavior immediately
- [ ] Idle NPCs don't interfere with duels or other game modes
- [ ] Idle timeout is configurable
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
