# Idea C04: Raid Welcome Event

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `C04`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/c04-raid-welcome`     |

**Goal:**
When the channel receives a Twitch raid, trigger a dramatic welcome event in-game. Spawn friendly mobs, launch fireworks, play celebration sounds, and display a welcome message. The number of raiders scales the effect intensity.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   ├── TwitchEventHandler.kt             # MODIFY — listen for raid events
│   └── RaidWelcomeService.kt             # CREATE — raid effect logic
```

**Implementation:**

1. Listen for Twitch raid events via EventSub (`channel.raid`)
2. Create `RaidWelcomeService`:
   - Extract raider count and raiding channel name
   - Scale effects by raider count:
     - **1-10 raiders**: 3 fireworks + chat welcome
     - **11-50 raiders**: 10 fireworks + spawn chickens (1 per 5 raiders)
     - **51-100 raiders**: Firework show + spawn parade of animals + note block fanfare
     - **100+ raiders**: All of the above + lightning show + all NPCs jump and glow
   - Display welcome text via TextDisplay entity at city center: "Welcome @raider's community!"
   - Text display auto-removes after 30 seconds
3. Send welcome message to Twitch chat: "Welcome raiders from @channel! (X viewers)"
4. Configurable effects and thresholds

**Paper API Used:**
- EventSub `channel.raid` — detect incoming raids
- `World.spawn(Firework.class)` — fireworks
- `World.spawn(Chicken.class / Pig.class)` — friendly mob parade
- `World.spawn(TextDisplay.class)` — welcome text
- `World.strikeLightningEffect()` — cosmetic lightning
- `World.playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE)` — fanfare
- `BukkitRunnable` — timed cleanup of text display

**Acceptance Criteria:**

- [ ] Incoming Twitch raids trigger in-game welcome effects
- [ ] Effect intensity scales with raider count
- [ ] Welcome text displays at city center and auto-removes
- [ ] Friendly mobs spawn and don't cause chaos
- [ ] Welcome message sent to Twitch chat
- [ ] Effects and thresholds are configurable
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
