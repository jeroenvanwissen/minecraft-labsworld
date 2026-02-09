# Task B2: Make Duel Parameters Configurable

| Field            | Value        |
| ---------------- | ------------ |
| **ID**           | `B2`         |
| **Status**       | `[x]`        |
| **Priority**     | Medium       |
| **Dependencies** | A1           |
| **Branch**       | `feature/b2-configurable-duel-params` |

**Goal:**
Move hardcoded duel parameters (hit chance, speed, range, HP, respawn delay) into `twitch.config.yml` so they can be tuned without code changes.

**Current Problem:**

```kotlin
// VillagerNpcDuelService.kt:67-71
// All duel parameters are hardcoded constants
val hitChance = 0.65
val speed = 1.15
val attackRange = 1.9
val maxHp = 10.0
val respawnDelay = 10L // seconds
```

**Scope:**

```
plugin/src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/VillagerNpcDuelService.kt           # MODIFY — read params from config
├── twitch/TwitchConfigManager.kt           # MODIFY — add duel config parsing
plugin/src/main/resources/
└── twitch.config.yml                       # MODIFY — add duel section with defaults
```

**Implementation:**

1. Add a `duel:` section to `twitch.config.yml` with keys: `hit_chance`, `speed`, `attack_range`, `max_hp`, `respawn_delay_seconds`
2. Add a `getDuelConfig()` method to `TwitchConfigManager` that reads these values with sensible defaults
3. Update `VillagerNpcDuelService` to accept config values instead of using hardcoded constants
4. Use `Coercions.anyToDouble()` for parsing numeric config values

**Acceptance Criteria:**

- [x] Duel parameters are read from config with defaults matching current hardcoded values
- [x] Missing config values fall back to current defaults (no breaking change)
- [x] Config changes take effect on next duel (not requiring server restart)
- [x] Build passes: `plugin/gradlew compileKotlin`
- [x] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
