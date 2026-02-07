# PROJECT PLAN — Code Quality & Restructuring

> Analysis of the LabsWorld Minecraft/Twitch plugin codebase.
> Goal: reduce cognitive load, eliminate duplication, and group logic where it belongs.

---

## Agent Execution

**For automated execution, see [.agent/TASKS.md](.agent/TASKS.md)** — structured tasks with:

- Goals, scope, and acceptance criteria
- Verification commands
- Dependency graph
- Guardrails (max diff size, stop-and-ask triggers)

**For setup and CLI usage, see [.agent/HOWTO.md](.agent/HOWTO.md)** — installation, sandboxing, and running the workflow.

**For Docker-based sandboxing, see [.agent/README.md](.agent/README.md)**.

---

## Summary of Findings

The plugin works and has a clear domain: Twitch viewers interact with a Minecraft world via chat commands and channel-point redeems, spawning and controlling NPC villagers. The code is functional but has grown organically, leading to several structural issues:

| Issue Category           | Count | Severity |
| ------------------------ | ----- | -------- |
| Code duplication         | 6     | High     |
| God class / long methods | 3     | High     |
| Tight coupling           | 3     | Medium   |
| Inconsistent patterns    | 4     | Medium   |
| Dead / unnecessary code  | 2     | Low      |

> **Note:** This document uses `VillagerNpc*` naming (e.g., `VillagerNpcManager.kt`) which is the target state.
> The current codebase uses `Npc*` naming. Task A0 in [.agent/TASKS.md](.agent/TASKS.md) performs this rename first.

---

## Step 1 — Centralise NPC NamespacedKeys

**Problem:** The key `NamespacedKey(plugin, "npc_twitch_user_id")` is defined independently in **four** classes:

- `VillagerNpcManager.twitchUserIdKey`
- `VillagerNpcLinkManager.linkedUserIdKey`
- `VillagerNpcSwarmService.linkedUserIdKey`
- `VillagerNpcAttackService.linkedUserIdKey`

If one changes, the others silently break. Each class creates its own instance to do the same PDC lookups.

**Fix:**

1. Create a `VillagerNpcKeys` object in the `npc` package that holds all NPC-related `NamespacedKey` instances.
2. Provide utility functions like `isLinkedVillagerNpc(villager)` and `getLinkedUserId(villager)` on this object.
3. Replace all four private key definitions with references to `VillagerNpcKeys`.

**Files affected:** `VillagerNpcManager.kt`, `VillagerNpcLinkManager.kt`, `VillagerNpcSwarmService.kt`, `VillagerNpcAttackService.kt`
**Risk:** Low — mechanical rename, no behaviour change.

---

## Step 2 — Deduplicate NPC creation methods in VillagerNpcManager

**Problem:** `createCustomVillagerNpc()` and `createLinkedVillagerNpc()` share ~80% of their body (spawn entity, set AI, invulnerability, silence, persistence, name generation). They differ only in which PDC tag is written (`npc_owner` vs `npc_twitch_user_id`).

**Fix:**

1. Extract a private `spawnBaseVillagerNpc(location, name, profession)` method with all shared setup.
2. Have `createCustomVillagerNpc()` and `createLinkedVillagerNpc()` call it and then add their specific PDC tag.

**Files affected:** `VillagerNpcManager.kt`
**Risk:** Low.

---

## Step 3 — Move `findAllLinkedVillagerNpcs()` into VillagerNpcLinkManager

**Problem:** `VillagerNpcSwarmService` and `VillagerNpcAttackService` have their own `findAllLinkedVillagerNpcs()` that duplicates the world-scanning + PDC filtering logic already present in `VillagerNpcLinkManager.findLoadedVillagerNpcByUserId()`. These services shouldn't need to know about `NamespacedKey` or `PersistentDataType` at all.

**Fix:**

1. Add `fun findAllLinkedVillagerNpcs(): List<Villager>` to `VillagerNpcLinkManager` (using the shared keys from Step 1).
2. Inject `VillagerNpcLinkManager` into `VillagerNpcSwarmService` instead of the raw plugin.
3. Inject `VillagerNpcLinkManager` into `VillagerNpcAttackService` instead of the raw plugin.
4. Remove the private `findAllLinkedVillagerNpcs()` and `linkedUserIdKey` from both service classes.

**Files affected:** `VillagerNpcSwarmService.kt`, `VillagerNpcAttackService.kt`, `VillagerNpcLinkManager.kt`, `LabsWorld.kt` (constructor wiring)
**Risk:** Low.

---

## Step 4 — Deduplicate `pickTargetPlayer()`

**Problem:** The function `pickTargetPlayer(preferred, allowRandom)` is defined **identically** in two places:

- `twitch/commands/lw/LwSubcommand.kt` (extension on `LabsWorld`)
- `twitch/redeems/handlers/RedeemHandlerUtils.kt` (extension on `LabsWorld`)

Both have the exact same body.

**Fix:**

1. Move the single canonical version to `util/PlayerUtils.kt` (or keep it as an extension on `LabsWorld` in one place).
2. Delete the duplicate from both current locations.
3. Update all call sites.

Additionally, `ActionExecutor.resolveTargetPlayer()` has a third, slightly different implementation of the same idea. Investigate whether it can also delegate to the shared helper after normalising the parameter mapping.

**Files affected:** `LwSubcommand.kt`, `RedeemHandlerUtils.kt`, `ActionExecutor.kt`, new `PlayerUtils.kt`
**Risk:** Low — purely mechanical.

---

## Step 5 — Extract duel logic from LabsWorld into `VillagerNpcDuelService`

**Problem:** `LabsWorld.startVillagerNpcDuel()` is **~140 lines** of complex game state management (HP tracking, hit-chance rolls, pathfinding, respawn timers) embedded directly in the plugin's main class. This is the single biggest contributor to `LabsWorld.kt` being hard to read.

**Fix:**

1. Create `npc/VillagerNpcDuelService.kt` with the full duel loop.
2. The service takes `VillagerNpcLinkManager`, `VillagerNpcSpawnPointManager`, and `JavaPlugin` (for the scheduler).
3. Move the `twitchVillagerNpcDuelTask` field and all duel logic into it.
4. `LabsWorld.startVillagerNpcDuel()` becomes a one-liner delegation.

**Files affected:** `LabsWorld.kt`, new `VillagerNpcDuelService.kt`
**Risk:** Medium — careful testing needed around the scheduler task lifecycle.

---

## Step 6 — Extract spawn-point logic from LabsWorld

**Problem:** `LabsWorld` contains several spawn-point helper methods (`pickVillagerNpcSpawnPointSpawnLocation`, `ensureVillagerNpcAtSpawnPoint`, `createVillagerNpcSpawnPointItem`, `villagerNpcSpawnPointCount`) that are thin wrappers but add clutter and make the main class a grab bag.

**Fix:**

1. Move `pickVillagerNpcSpawnPointSpawnLocation()` into `VillagerNpcSpawnPointManager` (it already calls `reconcileStoredSpawnPoints` and `getSpawnPointLocations` on it).
2. Move `ensureVillagerNpcAtSpawnPoint()` logic into `VillagerNpcLinkManager` (it already delegates to `villagerNpcLinkManager.ensureVillagerNpcAt()`; the chunk-loading preamble can go there too).
3. Leave only trivial one-liner delegations in `LabsWorld` if callers outside the `npc` package need them.

**Files affected:** `LabsWorld.kt`, `VillagerNpcSpawnPointManager.kt`, `VillagerNpcLinkManager.kt`
**Risk:** Low-medium.

---

## Step 7 — Unify the three context / invocation objects

**Problem:** Three structurally identical "context" classes exist:

- `CommandContext(plugin, twitchClient, twitchConfigManager)`
- `RedeemHandlerContext(plugin, twitchClient, twitchConfigManager)`
- `ActionContext(plugin, twitchClient, twitchConfigManager)`

They hold the same three fields and exist only because commands, redeems, and actions evolved independently.

**Fix:**

1. Create a single `TwitchContext` class (or rename `CommandContext` and use it everywhere).
2. Keep `RedeemHandlerContext.say()` helper as an extension function on `TwitchContext`.
3. Update `ActionExecutor`, `CommandDispatcher`, `RedeemDispatcher`, all handlers, and all commands.

**Files affected:** `Command.kt`, `ActionConfig.kt`, `RedeemHandler.kt`, `CommandDispatcher.kt`, `RedeemDispatcher.kt`, all subcommands & handlers
**Risk:** Medium — many files touched, but each change is trivial.

---

## Step 8 — Break up ActionExecutor (430-line God Object)

**Problem:** `ActionExecutor` is a single `object` with a giant `when` block and 10+ private methods, each implementing a completely different game action (fireworks, heal, spawn mob, drop items, weather, loot chest, NPC operations). Adding a new action means editing this one file and mentally parsing 400+ lines.

**Fix:**

1. Define an `ActionHandler` interface: `fun handle(context, invocation, params)` with a `val type: String`.
2. Create individual handler classes: `FireworksActionHandler`, `HealActionHandler`, `SpawnMobActionHandler`, `LootChestActionHandler`, `WeatherActionHandler`, etc.
3. Move shared private helpers (`resolveTargetPlayer`, `randomOffset`, `parseColors`, etc.) to a shared `ActionUtils` or the `util` package.
4. `ActionExecutor` becomes a registry that looks up handlers by `type` string.

This mirrors the existing `RedeemHandler` / `RedeemHandlers` pattern, which is already well-structured.

**Files affected:** `ActionExecutor.kt`, new handler files in `twitch/actions/handlers/`
**Risk:** Medium — many new files, but each is small and testable.

---

## Step 8b — Commands should delegate to Actions (not reimplement them)

**Problem:** Several `!lw` subcommands duplicate behaviour that already exists as config-driven actions in `ActionExecutor`. Specifically:

| Subcommand                                                         | Duplicates Action                          |
| ------------------------------------------------------------------ | ------------------------------------------ |
| `SpawnSubcommand` — calls `plugin.ensureVillagerNpcAtSpawnPoint()` | `npc.spawn` action — calls the same method |

`SwarmSubcommand` and `AttackSubcommand` are **removed entirely** — swarm and attack are only available as Twitch channel-point redeems (via `npc.swarm_player` / `npc.attack_player` actions), not as `!lw` subcommands.

For the remaining `SpawnSubcommand`, the code path duplicates the `npc.spawn` action. Both end up calling the same `LabsWorld` method, but each has its own parameter parsing, error handling, and reply formatting.

**Principle:** Commands are a user-facing trigger; **actions** are the actual behaviour. A command should parse its arguments, build an action invocation, and delegate. The action system is already designed to be composable (a config command can run multiple actions), so chat commands should use the same pipeline.

**Fix:**

1. **Delete** `SwarmSubcommand.kt` and `AttackSubcommand.kt`. Remove them from `LwSubcommands.all`.
2. After Step 8 (action handler registry), each action has a clean `ActionHandler.handle(context, invocation, params)` entry point.
3. Refactor `SpawnSubcommand` so it:
    - Parses its `!lw` arguments into an `ActionConfig` params map.
    - Calls the corresponding `ActionHandler` (or goes through `ActionExecutor.executeAction()`) directly.
    - Maps the action result back to a Twitch chat reply.
4. Any future chat command that maps 1:1 to an existing action follows the same pattern — the subcommand becomes a thin "argument parser → action caller → reply formatter".
5. `DuelSubcommand` stays as-is (duels have no action equivalent and are a distinct game mode, not a reusable action).

**Result:** One place for spawn logic. Swarm/attack logic lives only in the action/redeem system — no duplicate code paths.

**Files affected:** `SpawnSubcommand.kt`, `SwarmSubcommand.kt` (delete), `AttackSubcommand.kt` (delete), `LwSubcommands.kt`, action handlers from Step 8
**Risk:** Low — the underlying `LabsWorld` methods don't change; only the call chain is shortened.

---

## Step 9 — Consolidate permission / auth checking

**Problem:**

- `CommandDispatcher.isAuthorized()` does full badge-based permission checking.
- `TwitchChatAuth` has similar but separate `isBroadcaster`, `isModerator`, `isBroadcasterOrModerator` checks.
- `ReloadSubcommand` manually calls `TwitchChatAuth.isBroadcasterOrModerator()` **instead of** using the `permission` field on the `Command` interface.

This means the `permission` field on `LwCommand` is set to `EVERYONE`, but its subcommands internally re-check permissions with different logic.

**Fix:**

1. Add a `permission` field to `LwSubcommand` interface.
2. Have `LwCommand.handle()` check `subcommand.permission` using the existing dispatcher auth logic (or the `TwitchChatAuth` utility), **before** calling the subcommand.
3. Remove the manual `isBroadcasterOrModerator` check from `ReloadSubcommand` — it just declares its required permission instead.
4. Merge `CommandDispatcher.isAuthorized()` and `TwitchChatAuth` into a single auth utility (since `isAuthorized` already uses `TwitchChatAuth` internally).

**Files affected:** `LwSubcommand.kt`, `ReloadSubcommand.kt`, `LwCommand.kt`, `CommandDispatcher.kt`, `TwitchChatAuth.kt`
**Risk:** Low-medium.

---

## Step 10 — Add `runOnMainThread` to LwSubcommand

**Problem:** The `Command` interface has a `runOnMainThread` property, and `CommandDispatcher` uses it to schedule the handler on the right thread. But `LwSubcommand` has no such field. Every subcommand that needs Bukkit API access (`SpawnSubcommand`, `DuelSubcommand`) manually wraps its body in `plugin.server.scheduler.runTask(plugin, Runnable { ... })`.

This is repetitive boilerplate and easy to get wrong (forgetting the wrapper = crash from async thread).

**Fix:**

1. Add `val runOnMainThread: Boolean get() = true` to `LwSubcommand`.
2. Have `LwCommand.handle()` schedule the subcommand call on the main thread when `subcommand.runOnMainThread` is true.
3. Remove the manual `scheduler.runTask` wrappers from all subcommands.

**Files affected:** `LwSubcommand.kt`, `LwCommand.kt`, all subcommands in `lw/`
**Risk:** Low.

---

## Step 11 — Remove unused `Command.storage` field

**Problem:** The `Command` interface requires `var storage: T` with a generic type parameter. Every implementation sets it to `Unit`. Nothing ever reads it.

**Fix:**

1. Remove `storage` from the `Command` interface.
2. Remove the type parameter `T` from `Command<T>`.
3. Update `ConfigCommand`, `LwCommand`, and `CommandDispatcher`.

**Files affected:** `Command.kt`, `ConfigCommand.kt`, `LwCommand.kt`, `CommandDispatcher.kt`
**Risk:** Low — compile errors guide you.

---

## Step 12 — Eliminate scattered `plugin as LabsWorld` casts

**Problem:** At least 6 places cast `plugin` (`JavaPlugin`) to `LabsWorld` at runtime:

- `CommandDispatcher.handle()`: `plugin as? nl.jeroenlabs.labsWorld.LabsWorld`
- `ActionExecutor` (3 methods): `context.plugin as nl.jeroenlabs.labsWorld.LabsWorld`
- `RedeemHandlerUtils.pluginAsLabsWorld()`
- All `LwSubcommand` implementations via `ctx.labsWorld()`

These casts are fragile and make it hard to test individual components.

**Fix — option A (simple):**
Make the context classes hold a typed `LabsWorld` reference instead of `JavaPlugin`, since this plugin will never be used as a library:

```kotlin
data class TwitchContext(
    val plugin: LabsWorld,  // not JavaPlugin
    val twitchClient: TwitchClient,
    val twitchConfigManager: TwitchConfigManager,
)
```

**Fix — option B (if testability matters later):**
Define a `LabsWorldApi` interface with the methods that commands/actions/redeems need (`pickVillagerNpcSpawnPointSpawnLocation`, `ensureVillagerNpcAtSpawnPoint`, `startSwarmAllVillagerNpcs`, etc.), implement it in `LabsWorld`, and pass the interface.

**Files affected:** context classes, all command implementations, all action/redeem handlers
**Risk:** Medium — many call sites, but straightforward.

---

## Step 13 — Consolidate duplicate weather/world-state logic

**Problem:** Weather and world-state changes are handled in two places:

- `ActionExecutor.runWeather()` — handles clear/rain/storm with duration
- `WorldStateHandler` (redeem handler) — handles day/night/clear/rain/thunder without duration

They overlap but neither is a superset of the other.

**Fix:**

1. Create a single `WorldActionUtils` (or merge into the future `WeatherActionHandler` from Step 8).
2. Support all states (day, night, clear, rain, storm/thunder) with optional duration.
3. Have both the action system and the redeem handler delegate to this shared utility.

**Files affected:** `ActionExecutor.kt`, `WorldStateHandler.kt`, new shared utility
**Risk:** Low.

---

## Step 14 — Reduce reflection usage

**Problem:** Reflection is used in three places:

- `TwitchChatAuth.getIrcTags()` — reflection to get IRC tags from Twitch4J events
- `RedeemInvocation.fromEvent()` — 60+ lines of reflection to extract fields from EventSub payloads
- `ActionExecutor.setChestNameBestEffort()` — reflection for Paper vs Spigot API compat

**Fix:**

1. **`RedeemInvocation.fromEvent()`**: Given that `build.gradle.kts` already pins Twitch4J to `1.25.0` and targets Paper specifically, most of this reflection can be replaced with direct API calls. Add a comment explaining why reflection was used and which version first supported direct access.
2. **`TwitchChatAuth.getIrcTags()`**: Similarly, check if the pinned Twitch4J version supports `event.messageEvent.tags` directly.
3. **`setChestNameBestEffort()`**: Since `paper-plugin.yml` targets Paper, `chest.customName(Component)` is always available. Remove the reflection fallback.

**Files affected:** `TwitchChatAuth.kt`, `RedeemInvocation.kt`, `ActionExecutor.kt`
**Risk:** Medium — requires testing with the actual Twitch4J version.

---

## Step 15 — Centralise data-folder initialisation

**Problem:** Three classes independently check and create `plugin.dataFolder`:

- `VillagerNpcLinkManager.init()` — `if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()`
- `TwitchConfigManager.init()` — same
- `VillagerNpcSpawnPointManager.init()` — same

**Fix:**

1. Do the `dataFolder.mkdirs()` once in `LabsWorld.onEnable()` before any `init()` calls.
2. Remove the checks from the three manager classes.

**Files affected:** `LabsWorld.kt`, `VillagerNpcLinkManager.kt`, `TwitchConfigManager.kt`, `VillagerNpcSpawnPointManager.kt`
**Risk:** Very low.

---

## Step 16 — Clean up `CommandDispatcher.handle()` chat-bubble logic

**Problem:** `CommandDispatcher.handle()` is 75+ lines and mixes two unrelated concerns:

1. Dispatching `!commands` to registered handlers.
2. Showing a chat bubble above a viewer's NPC when they send a non-command message.

The chat-bubble logic includes NPC lookup, proximity checks, `TextDisplay` spawning, and a scheduled cleanup timer — all inline in the dispatcher.

**Fix:**

1. Extract the chat-bubble logic into a `ChatBubbleService` or a `VillagerNpcChatBubbleListener` class.
2. `CommandDispatcher.handle()` simply calls the service for non-command messages.

**Files affected:** `CommandDispatcher.kt`, new `ChatBubbleService.kt`
**Risk:** Low.

---

## Recommended Execution Order

The steps are ordered to minimise merge conflicts and build on each other:

| Phase                 | Steps        | Theme                                                   |
| --------------------- | ------------ | ------------------------------------------------------- |
| **A — Foundation**    | 1, 2, 15     | Centralise keys, deduplicate NPC creation, init cleanup |
| **B — NPC Layer**     | 3, 5, 6      | Clean NPC services, extract duel & spawn-point logic    |
| **C — Deduplication** | 4, 13, 11    | Remove duplicated helpers, weather logic, dead code     |
| **D — Twitch Layer**  | 7, 9, 10, 12 | Unify context objects, auth, thread scheduling, casts   |
| **E — Action System** | 8, 8b        | Break up ActionExecutor, commands delegate to actions   |
| **F — Polish**        | 14, 16       | Reduce reflection, extract chat-bubble service          |

Each step is independently shippable and testable. Complete one step, build, test on a dev server, and commit before moving to the next.

---

## Resulting Package Structure (Target)

```
nl.jeroenlabs.labsWorld/
├── LabsWorld.kt                        # Slim plugin entry: wiring only
├── util/
│   ├── Coercions.kt                    # (existing)
│   └── PlayerUtils.kt                  # pickTargetPlayer, randomOffset
├── npc/
│   ├── VillagerNpcKeys.kt                      # All NPC NamespacedKeys + PDC helpers
│   ├── VillagerNpcManager.kt                   # NPC creation (deduplicated)
│   ├── VillagerNpcLinkManager.kt               # Link tracking + findAllLinkedVillagerNpcs
│   ├── VillagerNpcSwarmService.kt              # Swarm NPCs on player (delegates to VillagerNpcLinkManager)
│   ├── VillagerNpcAttackService.kt             # Single NPC attacks player (delegates to VillagerNpcLinkManager)
│   ├── VillagerNpcDuelService.kt               # Duel game loop (extracted from LabsWorld)
│   ├── VillagerNpcSpawnPointManager.kt         # Spawn-point items/blocks + pickSpawnLocation
│   ├── VillagerNpcSpawnPointListener.kt        # (existing)
│   └── ChatBubbleService.kt            # NPC chat-bubble display logic
├── commands/
│   ├── LabsWorldCommand.kt             # (existing, in-game commands)
│   └── LabsWorldPaperCommand.kt        # (existing, Paper bridge)
└── twitch/
    ├── TwitchContext.kt                 # Unified context (replaces 3 context classes)
    ├── TwitchAuth.kt                    # Unified permission checking
    ├── TwitchConfigManager.kt           # (existing)
    ├── TwitchClientManager.kt           # (existing)
    ├── TwitchEventHandler.kt            # (existing)
    ├── commands/
    │   ├── Command.kt                   # Interface (no generic, no storage)
    │   ├── CommandDispatcher.kt         # Dispatch only (no chat-bubble)
    │   ├── CommandInvocation.kt         # (existing)
    │   ├── ConfigCommand.kt             # (existing)
    │   ├── LwCommand.kt                 # Handles runOnMainThread + permission for subs
    │   └── lw/
    │       ├── LwSubcommand.kt          # + permission + runOnMainThread fields
    │       ├── LwSubcommands.kt         # (existing)
    │       ├── HelpSubcommand.kt        # (existing)
    │       ├── SpawnSubcommand.kt       # No manual scheduler wrapping
    │       ├── DuelSubcommand.kt        # (existing, delegates to VillagerNpcDuelService)
    │       └── ReloadSubcommand.kt      # No manual auth check
    ├── actions/
    │   ├── ActionConfig.kt              # Data classes only
    │   ├── ActionHandler.kt             # Interface: type + handle()
    │   ├── ActionExecutor.kt            # Registry + dispatch (slim)
    │   ├── ActionUtils.kt              # Shared helpers (resolveTarget, randomOffset, etc.)
    │   └── handlers/
    │       ├── FireworksActionHandler.kt
    │       ├── HealActionHandler.kt
    │       ├── SpawnMobActionHandler.kt
    │       ├── DropItemsActionHandler.kt
    │       ├── WeatherActionHandler.kt
    │       ├── LootChestActionHandler.kt
    │       ├── VillagerNpcSpawnActionHandler.kt
    │       ├── VillagerNpcSwarmActionHandler.kt
    │       └── VillagerNpcAttackActionHandler.kt
    └── redeems/
        ├── RedeemDispatcher.kt          # (existing)
        ├── RedeemHandler.kt             # (existing, uses TwitchContext)
        ├── RedeemInvocation.kt          # (less reflection)
        └── handlers/                    # (existing, unchanged)
```

---

## Metrics (Estimated Impact)

| Metric                           | Before          | After (est.)          |
| -------------------------------- | --------------- | --------------------- |
| `LabsWorld.kt` lines             | 365             | ~80                   |
| `ActionExecutor.kt` lines        | 432             | ~40 (registry)        |
| Duplicate `pickTargetPlayer`     | 2 (+ 1 variant) | 1                     |
| Duplicate NamespacedKey defs     | 3               | 1 (`VillagerNpcKeys`) |
| Context/invocation classes       | 3               | 1 (`TwitchContext`)   |
| Max method length                | ~140 (duel)     | ~40                   |
| Files with `plugin as LabsWorld` | 6               | 0                     |

---

## Unit Testing Plan (New)

### Current State (as of February 7, 2026)

- No `src/test/kotlin` source set exists.
- `build.gradle.kts` has no test dependencies (`junit`, `mockk`, etc.).
- No `tasks.test { useJUnitPlatform() }` configuration is present.
- Most logic is in service classes with Bukkit/Twitch dependencies; some utility code is already pure and testable.

### Testing Strategy

Start with deterministic unit tests for pure utility/parsing code, then move to dispatcher/service tests with mocks, and finally add Bukkit-backed tests for world/entity behavior.

### Phase T0 - Test Framework Bootstrap

1. Add baseline test stack in `build.gradle.kts`:
   - `testImplementation(kotlin("test"))`
   - `testImplementation("org.junit.jupiter:junit-jupiter:<version>")`
   - `testImplementation("io.mockk:mockk:<version>")`
2. Enable JUnit 5:
   - `tasks.test { useJUnitPlatform() }`
3. Create initial structure:
   - `src/test/kotlin/nl/jeroenlabs/labsWorld/...`
4. Add a smoke test (`CoercionsTest`) and verify `./gradlew test`.

**Acceptance criteria**
- `./gradlew test` runs locally and exits successfully.
- One passing test class is committed.

### Phase T1 - Fast, Pure Unit Tests (Highest ROI)

Target files:

- `src/main/kotlin/nl/jeroenlabs/labsWorld/util/Coercions.kt`
- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/actions/ActionUtils.kt` (pure parse helpers only)

Test cases:

- Numeric/string coercion defaults and edge cases.
- Boolean coercion for `"true"/"false"/"yes"/"no"/"1"/"0"`.
- `anyToStringList` for CSV and mixed list input.
- `parseFireworkType`, `parseEntityType`, `parseItemStacks`, `pickDefaultWorld`.

**Acceptance criteria**
- Core coercion + parse behavior covered with deterministic tests.
- No Bukkit server boot required for this phase.

### Phase T2 - Config Parsing and Binding Tests

Target file:

- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchConfigManager.kt`

Focus:

- `getRedeemBindings()` validation rules (matcher required, handler/actions required).
- `getCommandBindings()` parsing of name, permission, run-on-main-thread, action list.
- `hasRequiredConfig()` behavior with env-var presence/absence.

Notes:

- Use temporary test data folders.
- Prefer small fixture YAML files under `src/test/resources`.

**Acceptance criteria**
- Valid and invalid config shapes are both asserted.
- Reload version increment behavior is tested.

### Phase T3 - Dispatcher and Authorization Unit Tests

Target files:

- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/TwitchAuth.kt`
- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/commands/CommandDispatcher.kt`
- `src/main/kotlin/nl/jeroenlabs/labsWorld/twitch/redeems/RedeemDispatcher.kt`

Focus:

- Permission matrix for `EVERYONE`, `MODERATOR`, `VIP`, `SUBSCRIBER`, `BROADCASTER`.
- Command dispatch path: command found/not found, unauthorized reply, init-once behavior.
- Redeem matching path: unmatched, missing handler, action execution path.

**Acceptance criteria**
- Permission behavior is codified with table-style tests.
- Dispatcher branching logic is covered without requiring a full Minecraft world.

### Phase T4 - Bukkit-Backed Tests (Mock Server)

Target files:

- `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcLinkManager.kt`
- `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcSpawnPointManager.kt`
- `src/main/kotlin/nl/jeroenlabs/labsWorld/npc/VillagerNpcDuelService.kt` (limited scenarios)

Focus:

- NPC linking persistence behavior (`spawned` vs `teleported` paths).
- Spawn point reconciliation and selection rules.
- Duel precondition failures (same user, missing spawn points) and single-duel guard behavior.

Notes:

- Run a compatibility spike first to validate the chosen mock Bukkit framework against Paper API usage.
- Keep time/scheduler assertions narrow and deterministic.

**Acceptance criteria**
- At least one end-to-end NPC lifecycle test passes in a mocked server environment.

### Phase T5 - CI and Quality Gates

1. Add CI job running `./gradlew test`.
2. Enforce tests on PRs before merge.
3. Track coverage trends (start informational, no hard threshold initially).

**Acceptance criteria**
- Failing tests block merges.
- Coverage report is visible in CI artifacts or summary.

### Initial Test Backlog (Suggested Order)

1. `CoercionsTest`
2. `ActionUtilsParsingTest`
3. `TwitchConfigManagerBindingsTest`
4. `TwitchAuthTest`
5. `CommandDispatcherTest`
6. `RedeemDispatcherTest`
7. `VillagerNpcLinkManagerTest` (mock server)

### Risks and Mitigations

- Static Bukkit access (`Bukkit.getServer()`) complicates isolation.
  - Mitigation: test pure helpers first; introduce thin wrappers/facades where needed.
- Randomness in game logic (`Random`) can make tests flaky.
  - Mitigation: isolate random decisions behind injectable providers when adding duel tests.
- File/env coupling in config manager.
  - Mitigation: fixture-based tests with temp dirs and controlled env assumptions.
