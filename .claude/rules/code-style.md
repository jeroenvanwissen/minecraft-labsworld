# Code Style

## Formatting

- Indentation: 4 spaces
- No established line length limit
- Braces: K&R style (opening brace on same line)
- Trailing commas: used in multi-line parameter lists
- Semicolons: none (Kotlin)
- Quote style: double quotes for strings

## Naming

### Variables and Functions

- camelCase: `userId`, `ensureNpcAt()`, `pickSpawnLocation()`
- Boolean prefixes: `is`, `has`, `can` (`isAuthorized`, `hasNpc`)
- Builder-style: `createVillagerNpc()`, `findBinding()`, `getRedeemBindings()`

### Types / Classes / Interfaces

- PascalCase: `VillagerNpcManager`, `CommandDispatcher`, `TwitchContext`
- Prefix domain-specific classes: `VillagerNpc*` for NPC classes, `Twitch*` for Twitch classes
- Interfaces: no `I` prefix; use descriptive names (`Command`, `ActionHandler`, `RedeemHandler`)
- Data classes for immutable configs: `ActionConfig`, `RedeemBindingConfig`, `CommandBindingConfig`

### Constants

- UPPER_SNAKE_CASE inside companion objects or top-level
- NamespacedKeys defined centrally in `VillagerNpcKeys` object

## Imports

- Standard library first (`org.bukkit.*`, `kotlin.*`)
- Third-party second (`com.github.twitch4j.*`, `net.kyori.*`)
- Project imports last (`nl.jeroenlabs.*`)
- No wildcard imports

## Patterns

### Error Handling

- Return `Result<T>` from service methods (not exceptions)
- Use `runCatching { }` to wrap exception-prone operations
- Log errors with `plugin.logger.warning()` or `plugin.logger.severe()`
- Use Kotlin null safety (`?.`, `?:`, `let`, `takeIf`) instead of null checks

### Logging

- Logger: `plugin.logger` (Bukkit's built-in Java logger)
- Levels: `info` for normal operations, `warning` for recoverable issues, `severe` for critical errors
- Include context in messages: `"Failed to load NPC for user $userId"`
- Use `plugin.logger.log(Level.WARNING, message, exception)` when logging with throwables

### Async

- Bukkit scheduler for game thread operations (`scheduler.runTask(plugin, runnable)`)
- Async scheduler for I/O (`scheduler.runTaskAsynchronously(plugin, runnable)`)
- Handlers declare `runOnMainThread` property; dispatchers schedule accordingly
- kotlinx-coroutines available but used sparingly

### Registration Pattern

- Handlers registered in maps: `Map<String, ActionHandler>`, `Map<String, Command>`
- Register in constructor or `init` block
- Lookup by string key (action type, command name, reward ID)

## Do / Don't

| Do                                            | Don't                                       |
| --------------------------------------------- | ------------------------------------------- |
| `Result.success(value)` / `Result.failure(e)` | Throw exceptions from service methods       |
| `player?.let { ... }`                         | `if (player != null) { ... }`               |
| `val config = ActionConfig(...)` (data class) | Mutable POJOs with setters                  |
| `plugin.logger.warning("msg")`                | `println("msg")` or `System.out`            |
| Centralize keys in `VillagerNpcKeys`          | Create NamespacedKey instances inline        |
| Constructor injection                         | Static singletons or service locator pattern |
