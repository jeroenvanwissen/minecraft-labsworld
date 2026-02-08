# Idea C08: Viewer Gift Effects

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `C08`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/c08-viewer-gifting`   |

**Goal:**
When a viewer gifts subscriptions, trigger in-game gift box drops for all NPCs. The number of gift boxes matches the number of gifted subs. Gift boxes contain random loot (items, buffs, or cosmetics).

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   ├── TwitchEventHandler.kt             # MODIFY — listen for gift sub events
│   └── GiftSubService.kt                 # CREATE — gift drop logic
```

**Implementation:**

1. Listen for gift sub events via EventSub (`channel.subscription.gift`)
2. Create `GiftSubService`:
   - Extract gifter username and number of gifts
   - For each gift: spawn a falling "gift box" (Shulker Box or Chest) near a random NPC
   - Gift box contains random items: flowers, food, firework stars, diamonds
   - Gift boxes have a glowing TextDisplay label: "Gift from @username"
   - Items auto-drop when chest lands (or after 10 seconds)
   - Announcement: "@username gifted X subs! Gift boxes incoming!"
3. Limit max gift boxes to 25 to prevent performance issues
4. Configurable loot table for gift box contents
5. Gift boxes auto-cleanup after 30 seconds

**Paper API Used:**
- `World.spawn(FallingBlock.class)` — falling gift box visual
- `World.dropItemNaturally()` — item drops from gift
- `World.spawn(TextDisplay.class)` — gift label
- `World.playSound(Sound.ENTITY_ITEM_PICKUP)` — gift open sound
- `BukkitRunnable` — timed cleanup
- `World.spawnParticle(Particle.TOTEM_OF_UNDYING)` — celebration particles

**Acceptance Criteria:**

- [ ] Gift subs trigger in-game gift box drops
- [ ] Number of boxes scales with gifts given (capped at 25)
- [ ] Gift boxes contain random loot items
- [ ] Gift boxes are labeled with gifter's name
- [ ] Announcement sent to Twitch chat
- [ ] Gift boxes auto-cleanup after timeout
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
