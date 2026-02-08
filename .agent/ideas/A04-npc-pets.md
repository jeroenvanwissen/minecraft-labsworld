# Idea A04: NPC Pets

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A04`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/a04-npc-pets`         |

**Goal:**
Allow viewers to spawn a pet that follows their NPC via `!lw pet <type>`. Supported pets: wolf, cat, parrot, fox, axolotl. The pet pathfinds to and stays near the NPC. One pet per NPC.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── VillagerNpcPetService.kt           # CREATE — pet spawning & tracking
├── twitch/commands/lw/
│   └── PetSubcommand.kt                   # CREATE — parse & spawn pet
└── twitch/commands/lw/
    └── LwSubcommands.kt                   # MODIFY — register subcommand
```

**Implementation:**

1. Create `VillagerNpcPetService` managing pet-to-NPC associations
2. Spawn the pet entity near the NPC's location
3. Mark the pet with PDC keys linking it to the NPC's UUID
4. Use a repeating `BukkitRunnable` to pathfind the pet toward its NPC (every 20 ticks)
5. If the viewer already has a pet, remove the old one before spawning a new one
6. When NPC is removed/respawned, also remove/respawn its pet
7. Store pet type in NPC data for persistence

**Paper API Used:**
- `World.spawn(location, entityClass)` — spawn pet entity
- `Mob.getPathfinder().moveTo(entity)` — pathfind to NPC
- `Tameable.setTamed(true)` — for wolves/cats
- `Animals` subclasses — Wolf, Cat, Parrot, Fox, Axolotl
- `PersistentDataContainer` — link pet to NPC

**Acceptance Criteria:**

- [ ] `!lw pet wolf` spawns a wolf that follows the viewer's NPC
- [ ] Only one pet per NPC — spawning a new one removes the old
- [ ] Pet follows NPC using pathfinding
- [ ] Pet is removed when NPC is removed
- [ ] Pet respawns when NPC respawns
- [ ] Invalid pet type lists available options
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
