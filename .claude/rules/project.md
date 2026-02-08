# Project Guidelines

## Directory Structure

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
├── LabsWorld.kt                  # Plugin entry point — wire dependencies here
├── commands/                     # In-game Minecraft commands (/labsworld)
│   ├── LabsWorldCommand.kt      # Command interface
│   └── LabsWorldPaperCommand.kt # Paper command bridge
├── npc/                          # NPC entity management and game services
│   ├── VillagerNpcKeys.kt       # All NamespacedKey definitions (centralized)
│   ├── VillagerNpcManager.kt    # Entity creation (stateless)
│   ├── VillagerNpcLinkManager.kt    # User↔NPC tracking + persistence
│   ├── VillagerNpcSpawnPointManager.kt  # Spawn point block management
│   ├── VillagerNpcDuelService.kt    # 1v1 duel game loop
│   ├── VillagerNpcSwarmService.kt   # Group chase behavior
│   ├── VillagerNpcAttackService.kt  # Group attack behavior
│   └── ChatBubbleService.kt        # TextDisplay floating chat bubbles
├── twitch/                       # Twitch integration
│   ├── TwitchContext.kt          # Unified context passed to all handlers
│   ├── TwitchAuth.kt            # Permission checks (role hierarchy)
│   ├── TwitchConfigManager.kt   # YAML config parsing + env var overrides
│   ├── TwitchClientManager.kt   # Twitch4J lifecycle + OAuth2 token refresh
│   ├── TwitchEventHandler.kt    # EventSub event routing
│   ├── commands/                 # Chat command system (!command)
│   │   ├── Command.kt           # Handler interface
│   │   ├── CommandDispatcher.kt # Router with permission checks
│   │   ├── ConfigCommand.kt     # YAML-driven dynamic commands
│   │   └── lw/                  # !lw subcommand tree
│   ├── actions/                  # Action handler registry
│   │   ├── ActionHandler.kt     # Handler interface
│   │   ├── ActionExecutor.kt    # Registry + executor
│   │   └── handlers/            # Concrete handlers (fireworks, heal, etc.)
│   └── redeems/                  # Channel point redeem system
│       ├── RedeemHandler.kt     # Handler interface
│       ├── RedeemDispatcher.kt  # Router (match by reward_id or reward_title)
│       └── handlers/            # Concrete handlers (npc spawn, aggro, etc.)
└── util/                         # Shared utilities
    ├── Coercions.kt             # Type coercion (anyToInt, anyToString, etc.)
    ├── PlayerUtils.kt           # Player selection helpers
    └── WorldStateUtils.kt       # Weather/world state helpers
```

## Module Organization

- Place new Twitch commands in `twitch/commands/` (implement `Command` interface)
- Place new action handlers in `twitch/actions/handlers/` (implement `ActionHandler` interface)
- Place new redeem handlers in `twitch/redeems/handlers/` (implement `RedeemHandler` interface)
- Place new NPC services in `npc/` with the `VillagerNpc` prefix
- Place new !lw subcommands in `twitch/commands/lw/` (implement `LwSubcommand` interface)
- Place shared utilities in `util/`
- Never import across layer boundaries (e.g., `twitch/` should not import from `commands/`)

## Naming Conventions

### Files

- Source files: Match the primary class name (`VillagerNpcDuelService.kt`)
- Test files: `{SourceClass}Test.kt` (`VillagerNpcLinkManagerTest.kt`)
- Test fixtures: `src/test/resources/fixtures/twitch/*.yml`

### Directories

- Lowercase, single word or short hyphenated (`commands/`, `actions/`, `handlers/`)
- Group by feature domain, not by type

## Configuration

- Plugin config: `src/main/resources/twitch.config.yml` (commands, redeems, actions)
- Secrets: Environment variables (`TWITCH_CLIENT_ID`, `TWITCH_CLIENT_SECRET`, `TWITCH_REFRESH_TOKEN`, `TWITCH_ACCESS_TOKEN`, `TWITCH_CHANNEL_NAME`)
- Build properties: `plugin/gradle.properties` (`jacocoMinCoverage=0.75`)
- Plugin metadata: `src/main/resources/paper-plugin.yml`
- Persistent data: `twitch-npcs.yml` and `npc-spawnpoints.yml` in plugin data folder (runtime)

## Dependencies

- Use `plugin/gradlew` (not bare `gradle`) for all build operations
- `compileOnly` for Paper API (provided by server at runtime)
- `implementation` for runtime dependencies (Twitch4J, kotlinx-coroutines, credential-manager)
- Shadow plugin bundles implementations into a single JAR
- Pin to specific versions in `build.gradle.kts`
