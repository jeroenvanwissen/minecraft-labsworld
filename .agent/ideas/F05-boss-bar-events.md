# Idea F05: Boss Bar Event Tracker

| Field            | Value                              |
| ---------------- | ---------------------------------- |
| **ID**           | `F05`                              |
| **Status**       | `[ ]`                              |
| **Dependencies** | None                               |
| **Branch**       | `feature/f05-boss-bar-events`      |

**Goal:**
Use boss bars as a universal event progress tracker. Show duel HP, siege wave progress, battle royale remaining, race positions, and other event states to all online players. Provides at-a-glance event status on screen.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── npc/
│   └── EventBossBarService.kt            # CREATE — centralized boss bar manager
```

**Implementation:**

1. Create `EventBossBarService`:
   - Central manager for all event-related boss bars
   - `createEventBar(title, color, style): BossBar` — create and register
   - `updateProgress(bar, progress)` — update fill (0.0 to 1.0)
   - `updateTitle(bar, title)` — update displayed text
   - `removeBar(bar)` — cleanup when event ends
   - Auto-add all online players to active bars
   - Handle player join/leave (add/remove from bars)
   - Support multiple concurrent bars (max 3 to avoid clutter)
2. Integration points:
   - **Duel**: Show both NPCs' HP bars
   - **Battle Royale**: "NPCs remaining: 12/20"
   - **Siege**: "Wave 3/10 — Enemies: 8"
   - **Race**: "Leader: @username — Checkpoint 4/8"
   - **Voting**: "Vote: Weather — 30s left"
3. Color coding: RED for danger, GREEN for progress, BLUE for info, YELLOW for warning
4. Bars auto-cleanup when event ends

**Paper API Used:**
- `Bukkit.createBossBar(title, BarColor, BarStyle)` — create bars
- `BossBar.setProgress(double)` — set fill level
- `BossBar.setTitle(String)` — update text
- `BossBar.addPlayer(Player)` — show to players
- `BossBar.removeAll()` — cleanup
- `BarColor` — RED, GREEN, BLUE, YELLOW, PURPLE, PINK, WHITE
- `BarStyle` — SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20
- `PlayerJoinEvent` — add new players to active bars

**Acceptance Criteria:**

- [ ] Boss bars display during active events
- [ ] Progress updates in real-time
- [ ] Bars shown to all online players
- [ ] New players joining see active bars
- [ ] Bars cleanup when events end
- [ ] Maximum 3 concurrent bars enforced
- [ ] Different colors for different event types
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
