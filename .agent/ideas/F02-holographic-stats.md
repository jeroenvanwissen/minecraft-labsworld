# Idea F02: Holographic Stats Display

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `F02`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/f02-holographic-stats`    |

**Goal:**
Display live server and stream statistics as holographic TextDisplay entities at key locations in the city. Show NPC count, active viewers, duel stats, and stream info. Acts as an information hub and visual centerpiece.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── HolographicDisplayService.kt      # CREATE — display management
```

**Implementation:**

1. Create `HolographicDisplayService`:
   - Spawn TextDisplay entities at configurable locations
   - Display panels:
     - **City Stats**: "NPCs: 24 | Active: 12 | Duels Today: 8"
     - **Top Fighter**: "Champion: @username (15 wins)"
     - **Server Info**: "Uptime: 3h 45m | TPS: 20.0"
     - **Stream Status**: "LIVE | Viewers: 342" (if Twitch API provides it)
   - Update displays every 10 seconds
   - Styled with Adventure API: colored headers, separator lines
   - Each display is a `TextDisplay` with colored background
   - Displays face the nearest player (Billboard.CENTER)
2. Configurable display locations via in-game command or config
3. Displays auto-spawn on server start and cleanup on shutdown
4. Admin command to add/remove display locations

**Paper API Used:**
- `World.spawn(TextDisplay.class)` — create holographic text
- `TextDisplay.text(Component)` — set styled content
- `TextDisplay.setBackgroundColor(Color)` — panel background
- `TextDisplay.setBillboard(Billboard.CENTER)` — face players
- `TextDisplay.setTransformation()` — scale for readability
- `BukkitRunnable` — periodic update
- `Bukkit.getTPS()` — server TPS
- Adventure API — rich text formatting

**Acceptance Criteria:**

- [ ] At least 3 different stat display panels
- [ ] Displays update automatically every 10 seconds
- [ ] Stats are accurate (NPC count, duel stats, uptime)
- [ ] Displays face players (billboard mode)
- [ ] Display locations are configurable
- [ ] Displays spawn on server start and cleanup on shutdown
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
