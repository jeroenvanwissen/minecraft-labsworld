# Idea H02: Stream Overlay API

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `H02`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/h02-stream-overlay-api`   |

**Goal:**
Expose NPC and event data via a lightweight HTTP API running inside the plugin. Stream overlays (OBS browser sources) can poll this API to display live NPC stats, leaderboards, event status, and recent activity on the stream.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── api/
│   ├── OverlayApiServer.kt               # CREATE — embedded HTTP server
│   └── OverlayApiRoutes.kt               # CREATE — API endpoint definitions
└── LabsWorld.kt                           # MODIFY — start/stop API server
```

**Implementation:**

1. Create `OverlayApiServer`:
   - Embed a lightweight HTTP server (use `com.sun.net.httpserver.HttpServer` — JDK built-in)
   - Listen on configurable port (default: 8085)
   - Respond with JSON
   - Start on plugin enable, stop on disable
   - CORS headers for browser source access
2. Create `OverlayApiRoutes`:
   - `GET /api/npcs` — list all NPCs with name, location, health, level
   - `GET /api/npcs/{userId}` — single NPC details
   - `GET /api/leaderboard/{category}` — top 10 for a stat category
   - `GET /api/events/active` — current active events (duels, siege, etc.)
   - `GET /api/events/recent` — last 10 events with results
   - `GET /api/stats` — server stats (NPC count, uptime, TPS)
3. JSON serialization: simple manual JSON builder or kotlinx.serialization
4. Rate limiting: max 10 requests/second per IP
5. Configurable enable/disable and port in config

**Paper API Used:**
- `com.sun.net.httpserver.HttpServer` — JDK built-in HTTP server
- `Bukkit.getTPS()` — server performance
- `Bukkit.getOnlinePlayers()` — online player count
- All existing service classes — read NPC data, stats, events
- `BukkitScheduler` — ensure data reads happen on main thread

**Acceptance Criteria:**

- [ ] HTTP server starts on plugin enable
- [ ] `GET /api/npcs` returns JSON list of all NPCs
- [ ] `GET /api/leaderboard/wins` returns top 10 duel winners
- [ ] `GET /api/events/active` shows current event status
- [ ] `GET /api/stats` returns server stats
- [ ] CORS headers present for browser source access
- [ ] Server stops cleanly on plugin disable
- [ ] Port is configurable
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
# After running server:
# curl http://localhost:8085/api/npcs
# curl http://localhost:8085/api/stats
```
