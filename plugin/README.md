# LabsWorld Plugin

Minecraft plugin that integrates with Twitch to allow viewers to interact with the game through chat commands and channel-point redemptions.

## Overview

LabsWorld enables Twitch viewers to spawn and control NPC villagers in a Minecraft world. Viewers use chat commands and channel point redeems to interact with their NPCs and participate in various game modes.

## Architecture

The plugin follows a clean, modular architecture with single-responsibility classes and clear dependency injection:

- **Entry Point:** Slim plugin class handles wiring only
- **Registry Pattern:** Extensible action and handler systems
- **Service Layer:** Dedicated services for NPC management, combat, and interactions
- **Zero Duplication:** All duplicate code eliminated
- **No Runtime Casts:** Type-safe throughout

## Package Structure

```
nl.jeroenlabs.labsWorld/
├── LabsWorld.kt                        # Plugin entry: wiring only
├── util/
│   ├── Coercions.kt                    # Type coercion helpers
│   ├── PlayerUtils.kt                  # Player selection utilities
│   └── WorldStateUtils.kt              # Weather/world state utilities
├── npc/
│   ├── VillagerNpcKeys.kt              # All NPC NamespacedKeys + PDC helpers
│   ├── VillagerNpcManager.kt           # NPC creation
│   ├── VillagerNpcLinkManager.kt       # Link tracking + queries
│   ├── VillagerNpcSwarmService.kt      # Swarm NPCs on player
│   ├── VillagerNpcAttackService.kt     # Single NPC attacks player
│   ├── VillagerNpcDuelService.kt       # Duel game loop
│   ├── VillagerNpcSpawnPointManager.kt # Spawn-point management
│   ├── VillagerNpcSpawnPointListener.kt# Spawn-point events
│   └── ChatBubbleService.kt            # NPC chat-bubble display
├── commands/
│   ├── LabsWorldCommand.kt             # In-game commands
│   └── LabsWorldPaperCommand.kt        # Paper bridge
└── twitch/
    ├── TwitchContext.kt                # Unified context
    ├── TwitchAuth.kt                   # Permission checking
    ├── TwitchConfigManager.kt          # Config loading/validation
    ├── TwitchClientManager.kt          # Twitch4J client lifecycle
    ├── TwitchEventHandler.kt           # EventSub routing
    ├── commands/
    │   ├── Command.kt                  # Interface (clean, no generics)
    │   ├── CommandDispatcher.kt        # Command routing
    │   ├── CommandInvocation.kt        # Command context
    │   ├── ConfigCommand.kt            # Config-driven commands
    │   ├── LwCommand.kt                # !lw subcommand router
    │   └── lw/
    │       ├── LwSubcommand.kt         # Subcommand interface
    │       ├── LwSubcommands.kt        # Subcommand registry
    │       ├── HelpSubcommand.kt       # Help text
    │       ├── DuelSubcommand.kt       # Start duel
    │       └── ReloadSubcommand.kt     # Reload config
    ├── actions/
    │   ├── ActionConfig.kt             # Action data classes
    │   ├── ActionHandler.kt            # Handler interface
    │   ├── ActionExecutor.kt           # Handler registry
    │   ├── ActionUtils.kt              # Shared helpers
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
        ├── RedeemDispatcher.kt         # Redeem routing
        ├── RedeemHandler.kt            # Handler interface
        ├── RedeemInvocation.kt         # EventSub payload parsing
        └── handlers/                   # Individual redeem handlers
```

## Testing

Comprehensive test suite with CI enforcement:

- **Test Framework:** JUnit 5 + MockK
- **Unit Tests:** Core utilities, config parsing, auth, dispatchers
- **Bukkit Tests:** Mock server harness with NPC lifecycle tests
- **CI Integration:** GitHub Actions enforces tests on all pull requests

**Test Suites:**
- `CoercionsTest` — Value coercion helpers
- `ActionUtilsParsingTest` — Parsing utilities
- `TwitchConfigManagerBindingsTest` — Config validation
- `TwitchAuthTest` — Permission matrix
- `CommandDispatcherTest` — Command routing
- `RedeemDispatcherTest` — Redeem matching
- `VillagerNpcLinkManagerTest` — NPC lifecycle (with mock Bukkit)

## Building

```bash
cd plugin
./gradlew build
```

The compiled plugin JAR will be in `build/libs/`.

## Configuration

Configure Twitch integration and channel point redeems in `config.yml`. See example configuration for details.

## Development

For development workflows and task templates, see [../.agent/README.md](../.agent/README.md)
