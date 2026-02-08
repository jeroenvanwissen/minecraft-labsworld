# Idea C05: Follow Alert

| Field            | Value                          |
| ---------------- | ------------------------------ |
| **ID**           | `C05`                          |
| **Status**       | `[ ]`                          |
| **Dependencies** | None                           |
| **Branch**       | `feature/c05-follow-alert`     |

**Goal:**
When someone follows the channel, trigger a personalized in-game effect. If the new follower has an NPC, the effect centers on their NPC. Otherwise, it plays at the city center. Includes a firework and chat announcement.

**Scope:**

```
src/main/kotlin/nl/jeroenlabs/labsWorld/
├── twitch/
│   ├── TwitchEventHandler.kt             # MODIFY — listen for follow events
│   └── FollowAlertService.kt             # CREATE — follow alert logic
```

**Implementation:**

1. Listen for Twitch follow events via EventSub (`channel.follow`)
2. Create `FollowAlertService`:
   - Extract follower username and user ID
   - Check if follower has a linked NPC
   - If NPC exists: spawn firework at NPC location, show particles around NPC
   - If no NPC: spawn firework at city center / spawn point
   - Play `Sound.ENTITY_PLAYER_LEVELUP` for all nearby players
   - Send chat message: "Welcome @username! Thanks for following!"
3. Optional: auto-spawn an NPC for new followers (configurable toggle)
4. Cooldown to prevent follow/unfollow spam (configurable, default 60s per user)
5. Configurable enable/disable toggle

**Paper API Used:**
- EventSub `channel.follow` — detect new followers
- `World.spawn(Firework.class)` — celebration firework
- `World.spawnParticle(Particle.HEART)` — love particles at NPC
- `World.playSound(Sound.ENTITY_PLAYER_LEVELUP)` — alert sound
- `VillagerNpcLinkManager` — check for existing NPC

**Acceptance Criteria:**

- [ ] New Twitch follows trigger in-game alert
- [ ] Effect centers on follower's NPC if it exists
- [ ] Firework and sound play for nearby players
- [ ] Welcome message sent to Twitch chat
- [ ] Cooldown prevents spam from follow/unfollow
- [ ] Feature can be enabled/disabled in config
- [ ] Build passes: `plugin/gradlew compileKotlin`
- [ ] Tests pass: `plugin/gradlew test`

**Verification Commands:**

```bash
plugin/gradlew compileKotlin
plugin/gradlew test
```
