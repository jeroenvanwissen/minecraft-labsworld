# LabsWorld

> Minecraft Paper Plugin that integrates Twitch chat with a Minecraft server, enabling Twitch viewers to spawn and control NPC villagers in-game.

## Quick Reference

| Action      | Command                                    |
| ----------- | ------------------------------------------ |
| Build       | `plugin/gradlew build`                     |
| Test        | `plugin/gradlew test`                      |
| Compile     | `plugin/gradlew compileKotlin`             |
| Shadow JAR  | `plugin/gradlew shadowJar`                 |
| Run server  | `plugin/gradlew runServer`                 |
| Install     | `plugin/gradlew installPlugin`             |
| Coverage    | `plugin/gradlew jacocoTestReport`          |

## Project Structure

```
plugin/                              # Main plugin module
├── build.gradle.kts                 # Build config (Kotlin 2.2.0, Java 21, Shadow, JaCoCo)
├── gradle.properties                # Coverage threshold (75%)
├── src/main/kotlin/nl/jeroenlabs/labsWorld/
│   ├── LabsWorld.kt                 # Plugin entry point (onEnable/onDisable)
│   ├── commands/                    # In-game admin commands (/labsworld)
│   ├── npc/                         # NPC management, duels, swarm, attack, chat bubbles
│   ├── twitch/                      # Twitch integration layer
│   │   ├── commands/                # Twitch chat command dispatch (!command)
│   │   ├── actions/                 # Action handlers (fireworks, heal, spawn mob, etc.)
│   │   └── redeems/                 # Channel point redeem handlers
│   └── util/                        # Coercions, player utils, world state utils
├── src/main/resources/
│   ├── paper-plugin.yml             # Plugin metadata + permissions
│   └── twitch.config.yml            # Example Twitch config
├── src/test/kotlin/                 # JUnit 5 + MockK tests
└── src/test/resources/fixtures/     # YAML test fixtures
.github/workflows/test.yml          # CI: test on push/PR to main
.agent/                              # Agent task tracking
```

## Architecture

LabsWorld connects Twitch chat events to Minecraft game actions through a layered dispatch system. Twitch4J receives EventSub events (chat messages, channel point redeems), which are routed through `TwitchEventHandler` to either `CommandDispatcher` (for `!commands`) or `RedeemDispatcher` (for channel points). Both dispatchers resolve permissions via `TwitchAuth`, then delegate to registered handlers. Handlers interact with NPC services (`VillagerNpcLinkManager`, `VillagerNpcDuelService`, etc.) that manage villager entities with persistent data via Paper's PDC system and YAML storage files.

## Key Decisions

- **Paper API only** — No NMS or reflection; all entity operations use Paper's public API and PDC (PersistentDataContainer) for entity-level data
- **Constructor injection** — All dependencies wired in `LabsWorld.initializeComponents()`; no service locator pattern
- **Result<T> for error handling** — Services return `Result<T>` instead of throwing exceptions
- **Config-driven commands** — Twitch commands and redeems are defined in `twitch.config.yml`, not hardcoded; `ConfigCommand` reads action definitions at runtime
- **Environment variables for secrets** — Twitch credentials come from `TWITCH_CLIENT_ID`, `TWITCH_CLIENT_SECRET`, `TWITCH_REFRESH_TOKEN`, `TWITCH_ACCESS_TOKEN` (not stored in YAML)
- **Main thread enforcement** — Bukkit entity operations must run on the main thread; handlers declare `runOnMainThread` and dispatchers schedule accordingly
- **One NPC per viewer** — `VillagerNpcLinkManager` enforces a single villager per Twitch user ID

## Rules

Detailed conventions are in `.claude/rules/`:

| File                                       | Covers                               |
| ------------------------------------------ | ------------------------------------ |
| [rules/project.md](rules/project.md)       | Project structure and architecture   |
| [rules/code-style.md](rules/code-style.md) | Code style and formatting            |
| [rules/testing.md](rules/testing.md)       | Testing conventions and requirements |
| [rules/security.md](rules/security.md)     | Security requirements and practices  |

## Documentation Maintenance

When making changes to the codebase, update all affected documentation files to stay in sync:

| File                                       | Update when...                                     |
| ------------------------------------------ | -------------------------------------------------- |
| [README.md](../README.md)                  | Features, architecture, config, or usage changes   |
| [.agent/TASKS.md](../.agent/TASKS.md)      | Task status changes (started, completed, blocked)  |
| [.agent/README.md](../.agent/README.md)    | Workflow or tooling changes                        |
| [.claude/CLAUDE.md](./)                    | Architecture, key decisions, or structure changes  |
| [rules/code-style.md](rules/code-style.md) | New patterns, naming conventions, or style changes |
| [rules/project.md](rules/project.md)       | New files, directories, or dependency changes      |
| [rules/security.md](rules/security.md)     | Security-relevant changes or new validations       |
| [rules/testing.md](rules/testing.md)       | New test files, patterns, or framework changes     |

## Tasks

See [.agent/TASKS.md](../.agent/TASKS.md) for the project backlog and task details.
