# AGENT EXECUTION PLAN — Code Quality & Restructuring

> **Purpose:** Agent-executable task list for refactoring the LabsWorld Minecraft/Twitch plugin.
> Each task is designed to be completed in a single PR with clear acceptance criteria.

**See also:** [HOWTO.md](HOWTO.md) — Setup guide, CLI commands, sandboxing instructions.

---

## Agent Workflow

```
loop:
  1. Pick next task with status "not-started" (respect dependencies)
  2. Create branch: `refactor/<task-id>-<short-name>`
  3. Implement changes within scope
  4. Run verification commands
  5. If all pass → open PR with task description
  6. Mark task as "completed"
  7. Repeat
```

---

## Guardrails

| Rule                           | Value                             | Action on Violation                 |
| ------------------------------ | --------------------------------- | ----------------------------------- |
| Max files changed per PR       | 8                                 | Split task into subtasks            |
| Max diff lines (added+removed) | 500                               | Split task into subtasks            |
| Compile check                  | `./gradlew compileKotlin`         | Must pass before PR                 |
| Shadow JAR build               | `./gradlew shadowJar`             | Must pass before PR                 |
| Plugin load test               | `./gradlew runServer` (manual)    | Recommended                         |
| Docs update required           | When public API changes           | Update PROJECT_PLAN.md or README.md |
| Ambiguity detected             | Missing info, unclear requirement | **STOP and ask user**               |

### Stop-and-Ask Triggers

The agent MUST stop and ask the user when:

- A file referenced in scope does not exist
- A method/class to modify cannot be found
- The change would affect more files than the scope specifies
- A dependency task is marked "blocked" or "needs-discussion"
- Test failures occur that aren't obviously related to the change
- The implementation approach has multiple valid options with trade-offs

---

## Task Status Legend

| Status | Meaning                    |
| ------ | -------------------------- |
| `[ ]`  | not-started                |
| `[~]`  | in-progress                |
| `[x]`  | completed                  |
| `[!]`  | blocked (needs discussion) |

---

## Phase A — Foundation

### Task A0: Rename Npc* to VillagerNpc*

| Field            | Value                    |
| ---------------- | ------------------------ |
| **ID**           | `A0`                     |
| **Status**       | `[x]`                    |
| **Dependencies** | None                     |
| **Branch**       | `refactor/a0-npc-rename` |

**Goal:**
Rename all NPC-related files and classes from `Npc*` to `VillagerNpc*` to make the naming explicit (these are Villager entities, not a generic NPC system).

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/npc/
├── NpcManager.kt           → VillagerNpcManager.kt
├── NpcLinkManager.kt       → VillagerNpcLinkManager.kt
├── NpcAggroService.kt      → VillagerNpcAggroService.kt
├── NpcSpawnPointManager.kt → VillagerNpcSpawnPointManager.kt
└── NpcSpawnPointListener.kt → VillagerNpcSpawnPointListener.kt
```

**Implementation:**

1. Rename each file and its class:
    - `NpcManager` → `VillagerNpcManager`
    - `NpcLinkManager` → `VillagerNpcLinkManager`
    - `NpcAggroService` → `VillagerNpcAggroService`
    - `NpcSpawnPointManager` → `VillagerNpcSpawnPointManager`
    - `NpcSpawnPointListener` → `VillagerNpcSpawnPointListener`
2. Update all imports across the codebase to use new names
3. Update any string references (e.g., in logs or messages) if applicable

**Acceptance Criteria:**

- [ ] All 5 files renamed to `VillagerNpc*` pattern
- [ ] All class names match their file names
- [ ] All imports updated throughout codebase
- [ ] No references to old `Npc*` class names remain
- [ ] Build passes with no errors

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
# Verify no old names remain:
grep -r "class Npc[A-Z]" src/main/kotlin --include="*.kt" | wc -l  # Should be 0
grep -r "import.*\.npc\.Npc[A-Z]" src/main/kotlin --include="*.kt" | wc -l  # Should be 0
```

---

### Task A1: Centralise NPC NamespacedKeys

| Field            | Value                  |
| ---------------- | ---------------------- |
| **ID**           | `A1`                   |
| **Status**       | `[x]`                  |
| **Dependencies** | `A0`                   |
| **Branch**       | `refactor/a1-npc-keys` |

**Goal:**
Create a single source of truth for all NPC-related `NamespacedKey` instances. The key `NamespacedKey(plugin, "npc_twitch_user_id")` is currently defined in 3 places.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/npc/
├── VillagerNpcKeys.kt          # CREATE
├── VillagerNpcManager.kt       # MODIFY (remove twitchUserIdKey)
├── VillagerNpcLinkManager.kt   # MODIFY (remove linkedUserIdKey)
└── VillagerNpcAggroService.kt  # MODIFY (remove linkedUserIdKey)
```

**Implementation:**

1. Create `VillagerNpcKeys.kt` as an object:

    ```kotlin
    object VillagerNpcKeys {
        fun twitchUserId(plugin: Plugin) = NamespacedKey(plugin, "npc_twitch_user_id")
        fun owner(plugin: Plugin) = NamespacedKey(plugin, "npc_owner")

        fun isLinkedVillagerNpc(villager: Villager, plugin: Plugin): Boolean
        fun getLinkedUserId(villager: Villager, plugin: Plugin): String?
    }
    ```

2. Replace all 3 private key definitions with calls to `VillagerNpcKeys`
3. Move common PDC lookup logic to helper functions in `VillagerNpcKeys`

**Acceptance Criteria:**

- [ ] `VillagerNpcKeys.kt` exists with all key definitions
- [ ] No other file defines `NamespacedKey(plugin, "npc_twitch_user_id")`
- [ ] No other file defines `NamespacedKey(plugin, "npc_owner")`
- [ ] All existing functionality preserved (no behavior change)
- [ ] Grep for `NamespacedKey.*npc_` returns only `VillagerNpcKeys.kt`

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
# Verify no duplicate key definitions:
grep -r "NamespacedKey.*npc_" src/main/kotlin --include="*.kt" | grep -v "VillagerNpcKeys.kt" | wc -l  # Should be 0
```

---

### Task A2: Deduplicate NPC Creation Methods

| Field            | Value                      |
| ---------------- | -------------------------- |
| **ID**           | `A2`                       |
| **Status**       | `[x]`                      |
| **Dependencies** | `A1`                       |
| **Branch**       | `refactor/a2-npc-creation` |

**Goal:**
Extract shared NPC setup logic from `createCustomVillagerNpc()` and `createLinkedVillagerNpc()` into a common base method.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/npc/
└── VillagerNpcManager.kt       # MODIFY
```

**Implementation:**

1. Create private method:
    ```kotlin
    private fun spawnBaseVillagerNpc(
        location: Location,
        name: String,
        profession: Villager.Profession
    ): Villager
    ```
2. Move shared setup (spawn, AI disable, invulnerability, silence, persistence, name) to base method
3. Have `createCustomVillagerNpc()` call base + add `npc_owner` PDC tag
4. Have `createLinkedVillagerNpc()` call base + add `npc_twitch_user_id` PDC tag

**Acceptance Criteria:**

- [ ] `spawnBaseVillagerNpc()` exists and contains all shared setup logic
- [ ] `createCustomVillagerNpc()` is ≤15 lines
- [ ] `createLinkedVillagerNpc()` is ≤15 lines
- [ ] No duplicate code between the two public methods
- [ ] NPC creation behavior unchanged (verify manually with runServer)

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
```

---

### Task A3: Centralise Data Folder Initialisation

| Field            | Value                         |
| ---------------- | ----------------------------- |
| **ID**           | `A3`                          |
| **Status**       | `[x]`                         |
| **Dependencies** | `A0`                          |
| **Branch**       | `refactor/a3-datafolder-init` |

**Goal:**
Remove duplicate `dataFolder.mkdirs()` calls from 3 manager classes; do it once in `LabsWorld.onEnable()`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── LabsWorld.kt                        # MODIFY (add single mkdirs call)
├── npc/VillagerNpcLinkManager.kt       # MODIFY (remove mkdirs)
├── npc/VillagerNpcSpawnPointManager.kt # MODIFY (remove mkdirs)
└── twitch/TwitchConfigManager.kt       # MODIFY (remove mkdirs)
```

**Implementation:**

1. Add `dataFolder.mkdirs()` at start of `LabsWorld.onEnable()`, before any manager init
2. Remove `if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()` from all 3 managers

**Acceptance Criteria:**

- [ ] `dataFolder.mkdirs()` appears exactly once in codebase (in `LabsWorld.kt`)
- [ ] Grep for `dataFolder.exists()` returns 0 results
- [ ] Grep for `dataFolder.mkdirs()` returns exactly 1 result

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "dataFolder.mkdirs" src/main/kotlin --include="*.kt" | wc -l  # Should be 1
grep -r "dataFolder.exists" src/main/kotlin --include="*.kt" | wc -l  # Should be 0
```

---

### Task A4: Remove UUID-owned NPC Code

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `A4`                           |
| **Status**       | `[x]`                          |
| **Dependencies** | `A2`                           |
| **Branch**       | `refactor/a4-remove-owner-npc` |

**Goal:**
Remove `createCustomNpc()` and related owner-based NPC code. All NPCs will be Twitch-user-linked only.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/npc/
├── VillagerNpcManager.kt   # MODIFY (remove createCustomNpc, getOwnerOfNpc)
└── VillagerNpcKeys.kt      # MODIFY (remove owner key)
```

**Implementation:**

1. Remove `createCustomNpc()` method from `VillagerNpcManager`
2. Remove `getOwnerOfNpc()` method from `VillagerNpcManager`
3. Remove `owner()` function from `VillagerNpcKeys`
4. Search codebase for any callers and remove/update them

**Acceptance Criteria:**

- [ ] No `createCustomNpc` method exists
- [ ] No `getOwnerOfNpc` method exists
- [ ] No `npc_owner` key definition exists
- [ ] Grep for `npc_owner` returns 0 results
- [ ] Build passes

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "createCustomNpc\|getOwnerOfNpc\|npc_owner" src/main/kotlin --include="*.kt" | wc -l  # Should be 0
```

---

## Phase B — NPC Layer

### Task B1: Move findAllLinkedVillagerNpcs to VillagerNpcLinkManager

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B1`                           |
| **Status**       | `[x]`                          |
| **Dependencies** | `A1`                           |
| **Branch**       | `refactor/b1-find-linked-npcs` |

**Goal:**
Eliminate duplicate world-scanning logic in `VillagerNpcAggroService` by delegating to `VillagerNpcLinkManager`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcLinkManager.kt   # MODIFY (add findAllLinkedVillagerNpcs)
├── npc/VillagerNpcAggroService.kt  # MODIFY (remove private method, inject LinkManager)
└── LabsWorld.kt                    # MODIFY (update constructor wiring)
```

**Implementation:**

1. Add to `VillagerNpcLinkManager`:
    ```kotlin
    fun findAllLinkedVillagerNpcs(): List<Villager>
    ```
2. Inject `VillagerNpcLinkManager` into `VillagerNpcAggroService` constructor
3. Delete private `findAllLinkedVillagerNpcs()` from `VillagerNpcAggroService`
4. Update `VillagerNpcAggroService` to call the injected manager
5. Update `LabsWorld.kt` to pass the link manager when constructing the aggro service

**Acceptance Criteria:**

- [ ] `VillagerNpcAggroService` has no `findAllLinkedVillagerNpcs` method
- [ ] `VillagerNpcAggroService` has no `linkedUserIdKey` property
- [ ] `VillagerNpcAggroService` constructor accepts `VillagerNpcLinkManager`
- [ ] `VillagerNpcLinkManager` has public `findAllLinkedVillagerNpcs()` method

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "linkedUserIdKey" src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcAggroService.kt | wc -l  # Should be 0
```

---

### Task B2: Extract Duel Logic to VillagerNpcDuelService

| Field            | Value                      |
| ---------------- | -------------------------- |
| **ID**           | `B2`                       |
| **Status**       | `[x]`                      |
| **Dependencies** | `B1`                       |
| **Branch**       | `refactor/b2-duel-service` |

**Goal:**
Extract the ~140-line `startVillagerNpcDuel()` method from `LabsWorld.kt` into a dedicated service class.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcDuelService.kt       # CREATE
└── LabsWorld.kt                        # MODIFY (delegate to service)
```

**Implementation:**

1. Create `VillagerNpcDuelService.kt` with:
    - Constructor taking: `VillagerNpcLinkManager`, `VillagerNpcSpawnPointManager`, `JavaPlugin`
    - Property: `twitchVillagerNpcDuelTask: BukkitTask?`
    - Method: `startDuel(...): Boolean`
    - All duel-related logic (HP tracking, hit-chance rolls, pathfinding, respawn timers)
2. In `LabsWorld`:
    - Instantiate `VillagerNpcDuelService` in `onEnable()`
    - Replace `startVillagerNpcDuel()` body with one-liner delegation:
        ```kotlin
        fun startVillagerNpcDuel(...) = duelService.startDuel(...)
        ```
    - Remove `twitchVillagerNpcDuelTask` field

**Acceptance Criteria:**

- [ ] `VillagerNpcDuelService.kt` exists with all duel logic
- [ ] `LabsWorld.startVillagerNpcDuel()` is ≤5 lines
- [ ] `LabsWorld.kt` has no `twitchVillagerNpcDuelTask` property
- [ ] Duel functionality works (manual test with runServer)
- [ ] `LabsWorld.kt` reduced by ~130 lines

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
wc -l src/main/kotlin/nl/jeroenlabs/labsWorld/LabsWorld.kt  # Should be ~230 or less
```

---

### Task B3: Extract Spawn-Point Logic from LabsWorld

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `B3`                           |
| **Status**       | `[x]`                          |
| **Dependencies** | `B1`                           |
| **Branch**       | `refactor/b3-spawnpoint-logic` |

**Goal:**
Move spawn-point helper methods from `LabsWorld` to appropriate manager classes.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcSpawnPointManager.kt # MODIFY (add pickSpawnLocation)
├── npc/VillagerNpcLinkManager.kt       # MODIFY (add ensureVillagerNpcAtWithChunkLoad)
└── LabsWorld.kt                        # MODIFY (delegate or remove methods)
```

**Implementation:**

1. Move `pickVillagerNpcSpawnPointSpawnLocation()` into `VillagerNpcSpawnPointManager`
2. Move `ensureVillagerNpcAtSpawnPoint()` logic into `VillagerNpcLinkManager` (include chunk-loading preamble)
3. In `LabsWorld.kt`:
    - Convert to one-liner delegations if callers outside `npc` package need them
    - Or remove entirely if only used internally

**Acceptance Criteria:**

- [ ] `VillagerNpcSpawnPointManager` has `pickSpawnLocation()` method
- [ ] `VillagerNpcLinkManager` has `ensureVillagerNpcAtWithChunkLoad()` or similar
- [ ] `LabsWorld.pickVillagerNpcSpawnPointSpawnLocation()` is ≤3 lines (delegation)
- [ ] `LabsWorld.ensureVillagerNpcAtSpawnPoint()` is ≤3 lines (delegation)

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
```

---

## Phase C — Deduplication

### Task C1: Deduplicate pickTargetPlayer

| Field            | Value                            |
| ---------------- | -------------------------------- |
| **ID**           | `C1`                             |
| **Status**       | `[x]`                            |
| **Dependencies** | None                             |
| **Branch**       | `refactor/c1-pick-target-player` |

**Goal:**
Create single canonical implementation of `pickTargetPlayer()` and remove all duplicates.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── util/PlayerUtils.kt                             # CREATE
├── twitch/commands/lw/LwSubcommand.kt              # MODIFY (remove duplicate)
├── twitch/redeems/handlers/RedeemHandlerUtils.kt   # MODIFY (remove duplicate)
└── twitch/actions/ActionExecutor.kt                # MODIFY (use shared util)
```

**Implementation:**

1. Create `util/PlayerUtils.kt` with:
    ```kotlin
    object PlayerUtils {
        fun pickTargetPlayer(
            server: Server,
            preferred: String?,
            allowRandom: Boolean
        ): Player?
    }
    ```
2. Delete `pickTargetPlayer` from `LwSubcommand.kt` extension
3. Delete `pickTargetPlayer` from `RedeemHandlerUtils.kt`
4. Update `ActionExecutor.resolveTargetPlayer()` to delegate to `PlayerUtils` (if compatible)
5. Update all call sites

**Acceptance Criteria:**

- [ ] `PlayerUtils.kt` exists with single implementation
- [ ] Grep for `fun pickTargetPlayer` returns exactly 1 result (in PlayerUtils.kt)
- [ ] All existing call sites compile and work

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "fun pickTargetPlayer" src/main/kotlin --include="*.kt" | wc -l  # Should be 1
```

---

### Task C2: Consolidate Weather/World-State Logic

| Field            | Value                             |
| ---------------- | --------------------------------- |
| **ID**           | `C2`                              |
| **Status**       | `[x]`                             |
| **Dependencies** | None                              |
| **Branch**       | `refactor/c2-weather-consolidate` |

**Goal:**
Merge overlapping weather/world-state logic between `ActionExecutor` and `WorldStateHandler`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── util/WorldStateUtils.kt                         # CREATE
├── twitch/actions/ActionExecutor.kt                # MODIFY
└── twitch/redeems/handlers/WorldStateHandler.kt    # MODIFY
```

**Implementation:**

1. Create `WorldStateUtils.kt` with:
    ```kotlin
    object WorldStateUtils {
        fun setWorldState(
            world: World,
            state: String,  // "day", "night", "clear", "rain", "storm"
            durationTicks: Int? = null
        )
    }
    ```
2. Have `ActionExecutor.runWeather()` delegate to `WorldStateUtils`
3. Have `WorldStateHandler` delegate to `WorldStateUtils`

**Acceptance Criteria:**

- [ ] `WorldStateUtils.kt` exists with unified implementation
- [ ] Supports: day, night, clear, rain, storm/thunder
- [ ] Supports optional duration parameter
- [ ] Both `ActionExecutor` and `WorldStateHandler` delegate to it
- [ ] No duplicate weather-setting logic remains

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
```

---

### Task C3: Remove Unused Command.storage Field

| Field            | Value                        |
| ---------------- | ---------------------------- |
| **ID**           | `C3`                         |
| **Status**       | `[x]`                        |
| **Dependencies** | None                         |
| **Branch**       | `refactor/c3-remove-storage` |

**Goal:**
Remove the unused `storage` field and type parameter from the `Command` interface.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/
├── Command.kt              # MODIFY (remove storage, remove type param)
├── CommandDispatcher.kt    # MODIFY (update references)
├── ConfigCommand.kt        # MODIFY (remove storage)
└── LwCommand.kt            # MODIFY (remove storage)
```

**Implementation:**

1. In `Command.kt`:
    - Remove `var storage: T` property
    - Remove type parameter `<T>`
    - interface becomes `interface Command` (not `interface Command<T>`)
2. Update all implementations to remove `override var storage = Unit`
3. Update `CommandDispatcher` to remove any generic handling

**Acceptance Criteria:**

- [ ] `Command` interface has no type parameter
- [ ] `Command` interface has no `storage` property
- [ ] No file contains `override var storage`
- [ ] Grep for `storage` in commands folder returns 0 results

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "storage" src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands --include="*.kt" | wc -l  # Should be 0
```

---

## Phase D — Twitch Layer

### Task D1: Unify Context Classes

| Field            | Value                        |
| ---------------- | ---------------------------- |
| **ID**           | `D1`                         |
| **Status**       | `[x]`                        |
| **Dependencies** | None                         |
| **Branch**       | `refactor/d1-twitch-context` |

**Goal:**
Replace the 3 identical context classes with a single `TwitchContext`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/
├── TwitchContext.kt                    # CREATE
├── commands/Command.kt                 # MODIFY (use TwitchContext)
├── commands/CommandDispatcher.kt       # MODIFY
├── commands/ConfigCommand.kt           # MODIFY
├── commands/LwCommand.kt               # MODIFY
├── actions/ActionConfig.kt             # MODIFY (remove ActionContext)
├── actions/ActionExecutor.kt           # MODIFY
├── redeems/RedeemHandler.kt            # MODIFY (remove RedeemHandlerContext)
├── redeems/RedeemDispatcher.kt         # MODIFY
└── redeems/handlers/*.kt               # MODIFY (all handlers)
```

**Implementation:**

1. Create `TwitchContext.kt`:
    ```kotlin
    data class TwitchContext(
        val plugin: LabsWorld,  // Typed, not JavaPlugin
        val twitchClient: TwitchClient,
        val twitchConfigManager: TwitchConfigManager,
    )
    ```
2. Add extension functions for handler-specific helpers (e.g., `say()`)
3. Replace `CommandContext`, `RedeemHandlerContext`, `ActionContext` with `TwitchContext`
4. Update all usages

**Acceptance Criteria:**

- [ ] `TwitchContext.kt` exists
- [ ] No file contains `CommandContext` class definition
- [ ] No file contains `RedeemHandlerContext` class definition
- [ ] No file contains `ActionContext` class definition
- [ ] All handlers/commands use `TwitchContext`

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "class CommandContext\|class RedeemHandlerContext\|class ActionContext" src/main/kotlin --include="*.kt" | wc -l  # Should be 0
```

**Note:** This task may touch more than 8 files. If so, split into:

- D1a: Create TwitchContext and update commands
- D1b: Update actions to use TwitchContext
- D1c: Update redeems to use TwitchContext

---

### Task D2: Consolidate Permission/Auth Checking

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `D2`                           |
| **Status**       | `[x]`                          |
| **Dependencies** | `D1`                           |
| **Branch**       | `refactor/d2-auth-consolidate` |

**Goal:**
Merge `CommandDispatcher.isAuthorized()` and `TwitchChatAuth` into single auth utility. Add `permission` field to `LwSubcommand`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/
├── TwitchAuth.kt                           # CREATE (or MODIFY TwitchChatAuth)
├── commands/CommandDispatcher.kt           # MODIFY (use unified auth)
├── commands/lw/LwSubcommand.kt             # MODIFY (add permission field)
├── commands/lw/LwCommand.kt                # MODIFY (check subcommand permission)
├── commands/lw/ReloadSubcommand.kt         # MODIFY (remove manual auth check)
└── TwitchChatAuth.kt                       # DELETE or merge into TwitchAuth
```

**Implementation:**

1. Create/update `TwitchAuth.kt` with all permission logic
2. Add `val permission: Permission get() = Permission.EVERYONE` to `LwSubcommand`
3. Have `LwCommand.handle()` check `subcommand.permission` before calling
4. Remove manual `isBroadcasterOrModerator` from `ReloadSubcommand`
5. Have it declare `override val permission = Permission.MODERATOR`

**Acceptance Criteria:**

- [ ] `LwSubcommand` has `permission` property
- [ ] `ReloadSubcommand` has no manual permission check
- [ ] `ReloadSubcommand` declares its required permission level
- [ ] `LwCommand.handle()` checks permission before dispatch
- [ ] Single source of truth for permission checking

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "isBroadcasterOrModerator" src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/ReloadSubcommand.kt | wc -l  # Should be 0
```

---

### Task D3: Add runOnMainThread to LwSubcommand

| Field            | Value                     |
| ---------------- | ------------------------- |
| **ID**           | `D3`                      |
| **Status**       | `[x]`                     |
| **Dependencies** | `D2`                      |
| **Branch**       | `refactor/d3-main-thread` |

**Goal:**
Add `runOnMainThread` property to `LwSubcommand` and remove manual scheduler wrapping from subcommands.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/
├── LwSubcommand.kt         # MODIFY (add runOnMainThread)
├── LwCommand.kt            # MODIFY (schedule based on property)
├── SpawnSubcommand.kt      # MODIFY (remove scheduler.runTask wrapper)
├── DuelSubcommand.kt       # MODIFY (remove scheduler.runTask wrapper)
└── *.kt                    # CHECK all other subcommands
```

**Implementation:**

1. Add to `LwSubcommand`:
    ```kotlin
    val runOnMainThread: Boolean get() = true
    ```
2. In `LwCommand.handle()`, wrap subcommand call in scheduler when `runOnMainThread` is true
3. Remove `plugin.server.scheduler.runTask(plugin, Runnable { ... })` wrappers from all subcommands

**Acceptance Criteria:**

- [ ] `LwSubcommand` has `runOnMainThread` property (default true)
- [ ] `LwCommand.handle()` handles thread scheduling
- [ ] No subcommand contains `scheduler.runTask` or `Runnable`
- [ ] Grep for `runTask` in lw/ folder returns 0 results

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "runTask\|Runnable" src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw --include="*.kt" | wc -l  # Should be 0
```

---

### Task D4: Eliminate plugin as LabsWorld Casts

| Field            | Value                      |
| ---------------- | -------------------------- |
| **ID**           | `D4`                       |
| **Status**       | `[ ]`                      |
| **Dependencies** | `D1`                       |
| **Branch**       | `refactor/d4-remove-casts` |

**Goal:**
Remove all `plugin as LabsWorld` runtime casts by making context hold typed `LabsWorld` reference.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/
├── TwitchContext.kt                            # VERIFY (plugin: LabsWorld)
├── commands/CommandDispatcher.kt               # MODIFY
├── commands/lw/*.kt                            # MODIFY (remove casts)
├── actions/ActionExecutor.kt                   # MODIFY (remove casts)
├── redeems/handlers/RedeemHandlerUtils.kt      # MODIFY (remove pluginAsLabsWorld)
└── redeems/handlers/*.kt                       # MODIFY as needed
```

**Implementation:**

1. Ensure `TwitchContext.plugin` is typed as `LabsWorld` (not `JavaPlugin`)
2. Remove all `plugin as LabsWorld` casts
3. Remove `pluginAsLabsWorld()` utility function
4. Update all usages to access `.plugin` directly

**Acceptance Criteria:**

- [ ] `TwitchContext.plugin` is typed as `LabsWorld`
- [ ] Grep for `as LabsWorld` returns 0 results
- [ ] Grep for `as? LabsWorld` returns 0 results
- [ ] Grep for `pluginAsLabsWorld` returns 0 results

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "as LabsWorld\|as? LabsWorld\|pluginAsLabsWorld" src/main/kotlin --include="*.kt" | wc -l  # Should be 0
```

---

## Phase E — Action System

### Task E1: Define ActionHandler Interface

| Field            | Value                                  |
| ---------------- | -------------------------------------- |
| **ID**           | `E1`                                   |
| **Status**       | `[ ]`                                  |
| **Dependencies** | `D1`                                   |
| **Branch**       | `refactor/e1-action-handler-interface` |

**Goal:**
Create the `ActionHandler` interface and `ActionUtils` for shared helpers.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/
├── ActionHandler.kt        # CREATE
├── ActionUtils.kt          # CREATE
└── ActionExecutor.kt       # MODIFY (extract shared helpers to ActionUtils)
```

**Implementation:**

1. Create `ActionHandler.kt`:
    ```kotlin
    interface ActionHandler {
        val type: String
        fun handle(context: TwitchContext, invocation: ActionInvocation, params: Map<String, Any?>)
    }
    ```
2. Create `ActionUtils.kt` with shared helpers:
    - `resolveTargetPlayer()`
    - `randomOffset()`
    - `parseColors()`
    - Any other reusable logic from `ActionExecutor`
3. Move helper methods from `ActionExecutor` to `ActionUtils`

**Acceptance Criteria:**

- [ ] `ActionHandler.kt` exists with interface definition
- [ ] `ActionUtils.kt` exists with shared helpers
- [ ] `ActionExecutor` private helpers moved to `ActionUtils`
- [ ] Existing functionality preserved

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
```

---

### Task E2: Extract Fireworks + Heal Action Handlers

| Field            | Value                                 |
| ---------------- | ------------------------------------- |
| **ID**           | `E2`                                  |
| **Status**       | `[ ]`                                 |
| **Dependencies** | `E1`                                  |
| **Branch**       | `refactor/e2-fireworks-heal-handlers` |

**Goal:**
Extract first batch of action handlers from `ActionExecutor`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/
├── handlers/                           # CREATE directory
│   ├── FireworksActionHandler.kt       # CREATE
│   └── HealActionHandler.kt            # CREATE
└── ActionExecutor.kt                   # MODIFY (remove extracted logic)
```

**Implementation:**

1. Create `handlers/FireworksActionHandler.kt` with firework logic from `ActionExecutor`
2. Create `handlers/HealActionHandler.kt` with heal logic
3. Remove corresponding case from `ActionExecutor.when` block
4. Register handlers in `ActionExecutor` (temporary, until full registry in E4)

**Acceptance Criteria:**

- [ ] `FireworksActionHandler.kt` exists and implements `ActionHandler`
- [ ] `HealActionHandler.kt` exists and implements `ActionHandler`
- [ ] `ActionExecutor` no longer contains fireworks/heal logic inline
- [ ] Fireworks and heal actions work (manual test)

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
```

---

### Task E3: Extract Remaining Action Handlers

| Field            | Value                            |
| ---------------- | -------------------------------- |
| **ID**           | `E3`                             |
| **Status**       | `[ ]`                            |
| **Dependencies** | `E2`                             |
| **Branch**       | `refactor/e3-remaining-handlers` |

**Goal:**
Extract all remaining action handlers from `ActionExecutor`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/handlers/
├── SpawnMobActionHandler.kt            # CREATE
├── DropItemsActionHandler.kt           # CREATE
├── WeatherActionHandler.kt             # CREATE
├── LootChestActionHandler.kt           # CREATE
├── VillagerNpcSpawnActionHandler.kt    # CREATE
├── VillagerNpcSwarmActionHandler.kt    # CREATE
└── VillagerNpcAttackActionHandler.kt   # CREATE
```

**Implementation:**

1. Create each handler file with logic extracted from `ActionExecutor`
2. Each handler implements `ActionHandler` interface
3. Use `ActionUtils` for shared logic
4. Update `ActionExecutor` to delegate to handlers

**Note:** If this touches more than 8 files, split into multiple subtasks (E3a, E3b, etc.)

**Acceptance Criteria:**

- [ ] All action types have dedicated handler classes
- [ ] `ActionExecutor.kt` is ≤50 lines (registry + dispatch only)
- [ ] All actions work (manual test with runServer)

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
wc -l src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/ActionExecutor.kt  # Should be ≤50
```

---

### Task E4: Convert ActionExecutor to Registry

| Field            | Value                         |
| ---------------- | ----------------------------- |
| **ID**           | `E4`                          |
| **Status**       | `[ ]`                         |
| **Dependencies** | `E3`                          |
| **Branch**       | `refactor/e4-action-registry` |

**Goal:**
Convert `ActionExecutor` from giant when-block to handler registry.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/
└── ActionExecutor.kt       # MODIFY (complete refactor)
```

**Implementation:**

1. Create registry map: `Map<String, ActionHandler>`
2. Register all handlers in init block or lazy initialization
3. Replace `when` block with registry lookup:
    ```kotlin
    fun executeAction(type: String, ...) {
        val handler = handlers[type] ?: throw UnknownActionException(type)
        handler.handle(context, invocation, params)
    }
    ```
4. Remove all action-specific logic from `ActionExecutor`

**Acceptance Criteria:**

- [ ] `ActionExecutor` has handler registry (Map or similar)
- [ ] No `when` block based on action type remains
- [ ] Adding new action = creating new handler class only
- [ ] `ActionExecutor.kt` is ≤50 lines

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "when.*type\|\"fireworks\"\|\"heal\"\|\"spawn_mob\"" src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/ActionExecutor.kt | wc -l  # Should be 0 or minimal
```

---

### Task E5: Commands Delegate to Actions

| Field            | Value                           |
| ---------------- | ------------------------------- |
| **ID**           | `E5`                            |
| **Status**       | `[ ]`                           |
| **Dependencies** | `E4`                            |
| **Branch**       | `refactor/e5-commands-delegate` |

**Goal:**
Refactor `SpawnSubcommand` to delegate to action handler. Delete unused `AggroSubcommand` and `AttackSubcommand`.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/
├── SpawnSubcommand.kt      # MODIFY (delegate to action)
├── AggroSubcommand.kt      # DELETE
├── AttackSubcommand.kt     # DELETE
└── LwSubcommands.kt        # MODIFY (remove deleted commands)
```

**Implementation:**

1. Delete `AggroSubcommand.kt` (aggro is redeem-only)
2. Delete `AttackSubcommand.kt` (attack is redeem-only)
3. Update `LwSubcommands.all` to remove deleted commands
4. Refactor `SpawnSubcommand`:
    - Parse `!lw spawn` arguments into action params map
    - Call `ActionExecutor.executeAction("npc.spawn", ...)`
    - Format action result as Twitch chat reply

**Acceptance Criteria:**

- [ ] `AggroSubcommand.kt` does not exist
- [ ] `AttackSubcommand.kt` does not exist
- [ ] `LwSubcommands.all` does not reference deleted commands
- [ ] `SpawnSubcommand` delegates to action handler
- [ ] `!lw spawn` command works (manual test)

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
ls src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/lw/  # Should not list Aggro or Attack
```

---

## Phase F — Polish

### Task F1: Extract Chat Bubble Service

| Field            | Value                     |
| ---------------- | ------------------------- |
| **ID**           | `F1`                      |
| **Status**       | `[ ]`                     |
| **Dependencies** | `B1`                      |
| **Branch**       | `refactor/f1-chat-bubble` |

**Goal:**
Extract chat-bubble display logic from `CommandDispatcher` into dedicated service.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/ChatBubbleService.kt            # CREATE
└── twitch/commands/CommandDispatcher.kt # MODIFY (delegate to service)
```

**Implementation:**

1. Create `ChatBubbleService.kt` with:
    - NPC lookup by viewer name
    - Proximity check to nearby players
    - `TextDisplay` spawning
    - Scheduled cleanup timer
2. In `CommandDispatcher.handle()`:
    - For non-command messages, call `chatBubbleService.showBubble(viewer, message)`
    - Remove all inline chat-bubble logic

**Acceptance Criteria:**

- [ ] `ChatBubbleService.kt` exists with all bubble logic
- [ ] `CommandDispatcher.handle()` is ≤30 lines
- [ ] No `TextDisplay` references in `CommandDispatcher`
- [ ] Chat bubbles work (manual test)

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
grep -r "TextDisplay" src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/CommandDispatcher.kt | wc -l  # Should be 0
```

---

### Task F2: Reduce Reflection Usage

| Field            | Value                           |
| ---------------- | ------------------------------- |
| **ID**           | `F2`                            |
| **Status**       | `[ ]`                           |
| **Dependencies** | None                            |
| **Branch**       | `refactor/f2-reduce-reflection` |

**Goal:**
Replace reflection with direct API calls where Twitch4J 1.25.0 and Paper support it.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/
├── TwitchChatAuth.kt               # MODIFY (check if direct API available)
├── redeems/RedeemInvocation.kt     # MODIFY (use direct API if available)
└── actions/ActionExecutor.kt       # MODIFY (remove reflection for chest name)
```

**Implementation:**

1. `RedeemInvocation.fromEvent()`:
    - Check Twitch4J 1.25.0 API for direct field access
    - Replace reflection with direct calls where possible
    - Add comments explaining any remaining reflection
2. `TwitchChatAuth.getIrcTags()`:
    - Check if `event.messageEvent.tags` is directly accessible
    - Replace reflection if possible
3. `ActionExecutor.setChestNameBestEffort()`:
    - Since targeting Paper, `chest.customName(Component)` is always available
    - Remove reflection fallback

**Acceptance Criteria:**

- [ ] `setChestNameBestEffort` uses direct Paper API (no reflection)
- [ ] Reflection reduced where direct API is available
- [ ] Comments explain any remaining reflection usage
- [ ] All functionality preserved

**Commands:**

```bash
./gradlew compileKotlin
./gradlew shadowJar
```

**Note:** This task requires investigation. If direct API is not available, document findings and mark as partially complete. **STOP and ask** if unsure about Twitch4J API capabilities.

---

## Summary

| Phase             | Tasks              | Est. PRs           |
| ----------------- | ------------------ | ------------------ |
| A — Foundation    | A1, A2, A3         | 3                  |
| B — NPC Layer     | B1, B2, B3         | 3                  |
| C — Deduplication | C1, C2, C3         | 3                  |
| D — Twitch Layer  | D1, D2, D3, D4     | 4-6 (D1 may split) |
| E — Action System | E1, E2, E3, E4, E5 | 5-7 (E3 may split) |
| F — Polish        | F1, F2             | 2                  |
| **Total**         | **17 tasks**       | **20-24 PRs**      |

---

## Quick Reference: Verification Commands

```bash
# Compile check
./gradlew compileKotlin

# Build JAR
./gradlew shadowJar

# Run test server (manual verification)
./gradlew runServer

# Full build
./gradlew build

# Install to server
./gradlew installPlugin
```

---

## Dependency Graph

```
A1 ─────┬──> B1 ──┬──> B2
        │         │
A2 ─────┤         └──> B3 ──> F1
        │
A3 ─────┘

C1, C2, C3 ──> (independent, can run parallel with A/B)

D1 ──┬──> D2 ──> D3
     │
     └──> D4
     │
     └──> E1 ──> E2 ──> E3 ──> E4 ──> E5

F2 ──> (independent)
```

**Parallelizable groups:**

- `{A1, A3, C1, C2, C3}` can start immediately
- `{A2}` after A1
- `{B1}` after A1
- `{D1, F2}` can start immediately
- `{B2, B3}` after B1
- `{D2, D4, E1}` after D1
