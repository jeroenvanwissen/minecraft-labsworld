# Test Coverage Tasks

**Goal:** Raise overall line coverage from ~25% to 75%.
**Current coverage:** 466/1715 lines (27.2%)
**Target:** jacocoMinCoverage=0.75 (1286/1715 lines needed, ~820 more lines to cover)

> GUARDRAIL: Do NOT modify source files. Only create/edit test files.

## Testing Patterns (from existing tests)

- JUnit 5 with `@Nested`, `@DisplayName`, `@Test`
- MockK (`mockk`, `relaxed = true`, `mockkStatic`, `every`, `verify`)
- `@AfterEach` with `unmockkAll()` or `clearAllMocks()`
- Factory helper methods for mocked events
- `BukkitScheduler` mocked to execute `Runnable` synchronously: `answers { secondArg<Runnable>().run() }`
- YAML fixtures under `src/test/resources/fixtures/twitch/`
- Real temp dirs via `createTempDir()` for file I/O tests
- `TwitchContext` as a real data class wrapping mocked dependencies
- Reflection for injecting private fields when needed

## Task Files (ordered by coverage impact)

### Priority 1 — High line count, 0% coverage (biggest wins)

| # | Task File | Source File | Lines | Current |
|---|-----------|------------|-------|---------|
| 1 | [T01-twitch-client-manager.md](T01-twitch-client-manager.md) | TwitchClientManager.kt | 119 | ✅ Done (PR #41) |
| 2 | [T02-villager-npc-duel-service.md](T02-villager-npc-duel-service.md) | VillagerNpcDuelService.kt | 117 | ✅ Done (PR #42) |
| 3 | [T03-loot-chest-action-handler.md](T03-loot-chest-action-handler.md) | LootChestActionHandler.kt | 106 | ✅ Done (PR #43) |
| 4 | [T04-labs-world.md](T04-labs-world.md) | LabsWorld.kt | 82 | 0% |
| 5 | [T05-villager-npc-spawn-point-manager.md](T05-villager-npc-spawn-point-manager.md) | VillagerNpcSpawnPointManager.kt | 71 | 0% |
| 6 | [T06-villager-npc-attack-service.md](T06-villager-npc-attack-service.md) | VillagerNpcAttackService.kt | 61 | 0% |
| 7 | [T07-villager-npc-swarm-service.md](T07-villager-npc-swarm-service.md) | VillagerNpcSwarmService.kt | 43 | 0% |
| 8 | [T08-villager-npc-manager.md](T08-villager-npc-manager.md) | VillagerNpcManager.kt | 41 | 0% |
| 9 | [T09-labs-world-command.md](T09-labs-world-command.md) | LabsWorldCommand.kt | 44 | 0% |

### Priority 2 — Medium line count, 0% coverage

| # | Task File | Source File | Lines | Current |
|---|-----------|------------|-------|---------|
| 10 | [T10-twitch-event-handler.md](T10-twitch-event-handler.md) | TwitchEventHandler.kt | 35 | 0% |
| 11 | [T11-villager-npc-spawn-point-listener.md](T11-villager-npc-spawn-point-listener.md) | VillagerNpcSpawnPointListener.kt | 30 | 0% |
| 12 | [T12-lw-command.md](T12-lw-command.md) | LwCommand.kt | 28 | 0% |
| 13 | [T13-duel-subcommand.md](T13-duel-subcommand.md) | DuelSubcommand.kt | 19 | 0% |
| 14 | [T14-reload-subcommand.md](T14-reload-subcommand.md) | ReloadSubcommand.kt | 11 | 0% |
| 15 | [T15-world-state-utils.md](T15-world-state-utils.md) | WorldStateUtils.kt | 19 | 0% |
| 16 | [T16-player-utils.md](T16-player-utils.md) | PlayerUtils.kt | 9 | 0% |

### Priority 3 — Redeem handlers, 0% coverage

| # | Task File | Source File | Lines | Current |
|---|-----------|------------|-------|---------|
| 17 | [T17-redeem-handler-utils.md](T17-redeem-handler-utils.md) | RedeemHandlerUtils.kt | 6 | 0% |
| 18 | [T18-player-action-handler.md](T18-player-action-handler.md) | PlayerActionHandler.kt | 12 | 0% |
| 19 | [T19-npc-attack-handler.md](T19-npc-attack-handler.md) | NpcAttackHandler.kt | 9 | 0% |
| 20 | [T20-npc-spawn-handler.md](T20-npc-spawn-handler.md) | NpcSpawnHandler.kt | 9 | 0% |
| 21 | [T21-npc-aggro-handler.md](T21-npc-aggro-handler.md) | NpcAggroHandler.kt (NpcSwarmHandler) | 8 | 0% |
| 22 | [T22-chat-say-handler.md](T22-chat-say-handler.md) | ChatSayHandler.kt | 5 | 0% |
| 23 | [T23-world-state-handler.md](T23-world-state-handler.md) | WorldStateHandler.kt | 6 | 0% |

### Priority 4 — Action handlers, low coverage

| # | Task File | Source File | Lines | Current |
|---|-----------|------------|-------|---------|
| 24 | [T24-fireworks-action-handler.md](T24-fireworks-action-handler.md) | FireworksActionHandler.kt | 20 | 10% |
| 25 | [T25-drop-items-action-handler.md](T25-drop-items-action-handler.md) | DropItemsActionHandler.kt | 15 | 13.3% |
| 26 | [T26-spawn-mob-action-handler.md](T26-spawn-mob-action-handler.md) | SpawnMobActionHandler.kt | 13 | 15.4% |
| 27 | [T27-weather-action-handler.md](T27-weather-action-handler.md) | WeatherActionHandler.kt | 9 | 22.2% |
| 28 | [T28-heal-action-handler.md](T28-heal-action-handler.md) | HealActionHandler.kt | 9 | 22.2% |
| 29 | [T29-npc-action-handlers.md](T29-npc-action-handlers.md) | VillagerNpc*ActionHandler.kt | 9-16 | 12-22% |

### Priority 5 — Improve partially-covered files

| # | Task File | Source File | Lines | Current |
|---|-----------|------------|-------|---------|
| 30 | [T30-villager-npc-link-manager.md](T30-villager-npc-link-manager.md) | VillagerNpcLinkManager.kt | 122 | 36.1% |
| 31 | [T31-action-utils.md](T31-action-utils.md) | ActionUtils.kt | 36 | 41.7% |
| 32 | [T32-twitch-config-manager.md](T32-twitch-config-manager.md) | TwitchConfigManager.kt | 112 | 60.7% |
| 33 | [T33-action-executor.md](T33-action-executor.md) | ActionExecutor.kt | 31 | 80.6% |

### Priority 6 — Small/interface files (minimal impact, pick up if needed)

| # | Task File | Source File | Lines | Current |
|---|-----------|------------|-------|---------|
| 34 | [T34-small-files.md](T34-small-files.md) | Various (see file) | various | 0% |

## Estimated Coverage After All Tasks

Completing Priority 1-4 should bring coverage above 75%. Priority 5 provides buffer.
